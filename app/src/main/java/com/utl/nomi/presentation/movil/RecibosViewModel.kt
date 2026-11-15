package com.utl.nomi.presentation.movil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.model.ReciboDetalleMovil
import com.utl.nomi.data.model.ReciboMovil
import com.utl.nomi.data.remote.MovilApi
import com.utl.nomi.data.remote.RetrofitClient
import com.utl.nomi.data.remote.leerError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Lista de recibos del empleado con sesión
sealed class EstadoRecibos {
    object Cargando : EstadoRecibos()
    data class Listo(val recibos: List<ReciboMovil>) : EstadoRecibos()
    data class Error(val mensaje: String) : EstadoRecibos()
}

// Detalle de un recibo (null = no hay ninguno abierto)
sealed class EstadoDetalle {
    object Cargando : EstadoDetalle()
    data class Listo(val detalle: ReciboDetalleMovil) : EstadoDetalle()
    data class Error(val mensaje: String) : EstadoDetalle()
}

class RecibosViewModel(
    private val api: MovilApi = RetrofitClient.movilApi
) : ViewModel() {

    private val _lista = MutableStateFlow<EstadoRecibos>(EstadoRecibos.Cargando)
    val lista: StateFlow<EstadoRecibos> = _lista

    private val _detalle = MutableStateFlow<EstadoDetalle?>(null)
    val detalle: StateFlow<EstadoDetalle?> = _detalle

    fun cargarLista() {
        if (_lista.value !is EstadoRecibos.Listo) _lista.value = EstadoRecibos.Cargando
        viewModelScope.launch {
            _lista.value = try {
                val r = api.misRecibos()
                when {
                    r.isSuccessful -> EstadoRecibos.Listo(r.body().orEmpty())
                    r.code() == 401 -> EstadoRecibos.Error("Tu sesión expiró. Inicia sesión de nuevo.")
                    else -> EstadoRecibos.Error(r.leerError()?.mensaje ?: "No se pudieron cargar tus recibos.")
                }
            } catch (e: Exception) {
                EstadoRecibos.Error("No hay conexión con el servidor. Revisa tu internet.")
            }
        }
    }

    fun abrirDetalle(nominaId: Long) {
        _detalle.value = EstadoDetalle.Cargando
        viewModelScope.launch {
            _detalle.value = try {
                val r = api.reciboDetalle(nominaId)
                when {
                    r.isSuccessful && r.body() != null -> EstadoDetalle.Listo(r.body()!!)
                    else -> EstadoDetalle.Error(r.leerError()?.mensaje ?: "No se pudo cargar el recibo.")
                }
            } catch (e: Exception) {
                EstadoDetalle.Error("No hay conexión con el servidor. Revisa tu internet.")
            }
        }
    }

    fun cerrarDetalle() {
        _detalle.value = null
    }
}
