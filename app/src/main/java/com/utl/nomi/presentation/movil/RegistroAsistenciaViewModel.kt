package com.utl.nomi.presentation.movil

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.model.RegistroAsistenciaMovil
import com.utl.nomi.data.model.RegistroAsistenciaMovilRequest
import com.utl.nomi.data.remote.LocationHelper
import com.utl.nomi.data.remote.MovilApi
import com.utl.nomi.data.remote.RetrofitClient
import com.utl.nomi.data.remote.leerError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Pasos del flujo de registro, como en el Figma:
// escanear -> verificar -> éxito o rechazo
sealed class EstadoRegistro {
    object Escaneando : EstadoRegistro()
    data class Verificando(val paso: String) : EstadoRegistro()
    data class Exito(val registro: RegistroAsistenciaMovil) : EstadoRegistro()
    data class Rechazado(val mensaje: String) : EstadoRegistro()
}

class RegistroAsistenciaViewModel(
    private val api: MovilApi = RetrofitClient.movilApi
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoRegistro>(EstadoRegistro.Escaneando)
    val estado: StateFlow<EstadoRegistro> = _estado

    // La cámara detecta el mismo QR muchas veces por segundo:
    // solo el primero inicia el registro.
    @Volatile private var enProceso = false

    fun registrar(context: Context, codigoQr: String) {
        if (enProceso) return
        enProceso = true
        _estado.value = EstadoRegistro.Verificando("Obteniendo tu ubicación...")

        viewModelScope.launch {
            try {
                val gps = LocationHelper(context)
                val ubicacion = gps.getCurrentLocation()
                if (ubicacion == null) {
                    _estado.value = EstadoRegistro.Rechazado(
                        "No se pudo obtener tu ubicación. Activa el GPS e intenta de nuevo."
                    )
                    return@launch
                }

                // GPS falso, ubicación vieja o señal débil: no se manda al servidor
                gps.problemaCon(ubicacion)?.let { problema ->
                    _estado.value = EstadoRegistro.Rechazado(problema)
                    return@launch
                }

                _estado.value = EstadoRegistro.Verificando("Verificando que estés en la planta...")

                // El servidor valida el QR, la distancia, el retardo y que no se repita
                val r = api.registrarAsistencia(
                    RegistroAsistenciaMovilRequest(codigoQr, ubicacion.latitude, ubicacion.longitude)
                )

                _estado.value = when {
                    r.isSuccessful && r.body() != null -> EstadoRegistro.Exito(r.body()!!)
                    r.code() == 401 -> EstadoRegistro.Rechazado("Tu sesión expiró. Cierra sesión y vuelve a entrar.")
                    else -> EstadoRegistro.Rechazado(
                        r.leerError()?.mensaje ?: "No se pudo registrar la asistencia (código ${r.code()})."
                    )
                }
            } catch (e: Exception) {
                _estado.value = EstadoRegistro.Rechazado("No hay conexión con el servidor. Revisa tu internet.")
            }
        }
    }

    // Vuelve a la cámara (después de un rechazo o al entrar de nuevo a la pantalla)
    fun reiniciar() {
        enProceso = false
        _estado.value = EstadoRegistro.Escaneando
    }
}
