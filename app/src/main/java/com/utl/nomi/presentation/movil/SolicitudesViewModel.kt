package com.utl.nomi.presentation.movil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.utl.nomi.data.model.NuevaSolicitudRequest
import com.utl.nomi.data.model.RespuestaSolicitud
import com.utl.nomi.data.model.SolicitudMovil
import com.utl.nomi.data.model.TipoPermiso
import com.utl.nomi.data.remote.MovilApi
import com.utl.nomi.data.remote.RetrofitClient
import com.utl.nomi.data.remote.leerError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

// Lista de solicitudes del empleado con sesión
sealed class EstadoSolicitudes {
    object Cargando : EstadoSolicitudes()
    data class Listo(val solicitudes: List<SolicitudMovil>) : EstadoSolicitudes()
    data class Error(val mensaje: String) : EstadoSolicitudes()
}

// Formulario de nueva solicitud
data class FormularioSolicitud(
    val tipos: List<TipoPermiso> = emptyList(),
    val cargandoTipos: Boolean = true,
    val tipo: TipoPermiso? = null,
    val fechaInicio: LocalDate? = null,
    val fechaFin: LocalDate? = null,
    val motivo: String = "",
    val enviando: Boolean = false,
    val error: String? = null
) {
    // Días que abarca la solicitud (los dos días cuentan)
    val dias: Int?
        get() = if (fechaInicio != null && fechaFin != null && !fechaFin.isBefore(fechaInicio))
            (fechaFin.toEpochDay() - fechaInicio.toEpochDay()).toInt() + 1 else null
}

class SolicitudesViewModel(
    private val api: MovilApi = RetrofitClient.movilApi
) : ViewModel() {

    private val _lista = MutableStateFlow<EstadoSolicitudes>(EstadoSolicitudes.Cargando)
    val lista: StateFlow<EstadoSolicitudes> = _lista

    // null = el formulario está cerrado
    private val _formulario = MutableStateFlow<FormularioSolicitud?>(null)
    val formulario: StateFlow<FormularioSolicitud?> = _formulario

    // Mensaje corto después de enviar ("Tu solicitud se envió a Recursos Humanos.")
    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso

    fun cargarLista() {
        if (_lista.value !is EstadoSolicitudes.Listo) _lista.value = EstadoSolicitudes.Cargando
        viewModelScope.launch {
            _lista.value = try {
                val r = api.misSolicitudes()
                when {
                    r.isSuccessful -> EstadoSolicitudes.Listo(r.body().orEmpty())
                    r.code() == 401 -> EstadoSolicitudes.Error("Tu sesión expiró. Inicia sesión de nuevo.")
                    else -> EstadoSolicitudes.Error(r.leerError()?.mensaje ?: "No se pudieron cargar tus solicitudes.")
                }
            } catch (e: Exception) {
                EstadoSolicitudes.Error("No hay conexión con el servidor. Revisa tu internet.")
            }
        }
    }

    fun abrirFormulario() {
        _formulario.value = FormularioSolicitud()
        viewModelScope.launch {
            val tipos = try {
                api.tiposPermiso().body().orEmpty()
            } catch (e: Exception) {
                emptyList()
            }
            actualizar {
                it.copy(
                    tipos = tipos,
                    cargandoTipos = false,
                    tipo = tipos.firstOrNull(),
                    error = if (tipos.isEmpty()) "No se pudieron cargar los tipos de solicitud." else null
                )
            }
        }
    }

    fun cerrarFormulario() {
        _formulario.value = null
    }

    fun elegirTipo(tipo: TipoPermiso) = actualizar { it.copy(tipo = tipo, error = null) }

    fun elegirInicio(fecha: LocalDate) = actualizar {
        // Si la fecha final quedó antes de la nueva inicial, se mueve al mismo día
        val fin = it.fechaFin?.takeIf { f -> !f.isBefore(fecha) } ?: fecha
        it.copy(fechaInicio = fecha, fechaFin = fin, error = null)
    }

    fun elegirFin(fecha: LocalDate) = actualizar { it.copy(fechaFin = fecha, error = null) }

    fun escribirMotivo(texto: String) = actualizar { it.copy(motivo = texto.take(255), error = null) }

    fun enviar() {
        val f = _formulario.value ?: return

        // Mismas reglas que el servidor, para avisar antes de enviar
        val tipo = f.tipo
        val inicio = f.fechaInicio
        val fin = f.fechaFin
        val maximo = tipo?.diasMaximos
        if (tipo == null) return avisar("Elige el tipo de solicitud.")
        if (inicio == null || fin == null) return avisar("Elige las fechas.")
        if (fin.isBefore(inicio)) return avisar("La fecha final no puede ser antes que la inicial.")
        if (maximo != null && (f.dias ?: 0) > maximo) return avisar("${tipo.nombre} permite máximo $maximo días por solicitud.")
        if (f.motivo.trim().length < 5) return avisar("Escribe el motivo (mínimo 5 letras).")

        actualizar { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            try {
                val r = api.crearSolicitud(
                    NuevaSolicitudRequest(
                        tipoPermisoId = tipo.id,
                        fechaInicio = inicio.toString(),   // yyyy-MM-dd
                        fechaFin = fin.toString(),
                        motivo = f.motivo.trim()
                    )
                )
                if (r.isSuccessful) {
                    _aviso.value = r.body()?.mensaje ?: "Tu solicitud se envió a Recursos Humanos."
                    _formulario.value = null
                    cargarLista()
                } else {
                    // Los rechazos traen { codigo, mensaje }
                    val mensaje = try {
                        r.errorBody()?.string()?.let { Gson().fromJson(it, RespuestaSolicitud::class.java)?.mensaje }
                    } catch (e: Exception) {
                        null
                    }
                    actualizar {
                        it.copy(
                            enviando = false,
                            error = if (r.code() == 401) "Tu sesión expiró. Inicia sesión de nuevo."
                            else mensaje ?: "No se pudo enviar la solicitud."
                        )
                    }
                }
            } catch (e: Exception) {
                actualizar { it.copy(enviando = false, error = "No hay conexión con el servidor. Revisa tu internet.") }
            }
        }
    }

    private fun avisar(error: String) = actualizar { it.copy(error = error) }

    fun limpiarAviso() {
        _aviso.value = null
    }

    private fun actualizar(cambio: (FormularioSolicitud) -> FormularioSolicitud) {
        _formulario.value?.let { _formulario.value = cambio(it) }
    }
}
