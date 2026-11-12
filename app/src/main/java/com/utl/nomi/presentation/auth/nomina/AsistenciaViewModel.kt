package com.utl.nomi.presentation.auth.nomina

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.model.AsistenciaDto
import com.utl.nomi.data.remote.AsistenciaApi
import com.utl.nomi.data.remote.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Resultado del último intento, para que la pantalla sepa qué color y botón mostrar
enum class EstadoRegistro { ESPERANDO, PROCESANDO, EXITO, ERROR }

class AsistenciaViewModel(
    private val api: AsistenciaApi
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _estado = MutableStateFlow(EstadoRegistro.ESPERANDO)
    val estado: StateFlow<EstadoRegistro> = _estado

    // Antes aquí estaban fijas las coordenadas de Lima, Perú (-12.04, -77.04)
    // con un radio de 50 m, y siempre rechazaba. La validación de distancia
    // contra la ubicación real de la planta la hará el servidor (Fase 3).

    fun validarYRegistrar(context: Context, empleadoId: Int, qrCode: String) {
        // Evita doble envío: la cámara puede detectar el mismo QR varias veces seguidas
        if (_isLoading.value) return
        _isLoading.value = true
        _estado.value = EstadoRegistro.PROCESANDO

        viewModelScope.launch {
            _statusMessage.value = "Obteniendo tu ubicación..."

            try {
                val location = LocationHelper(context).getCurrentLocation()

                if (location == null) {
                    terminarConError("No se pudo obtener tu ubicación. Activa el GPS e intenta de nuevo.")
                    return@launch
                }

                _statusMessage.value = "Registrando asistencia..."

                val request = AsistenciaDto(
                    empleadoId = empleadoId,
                    latitudEntrada = location.latitude,
                    longitudEntrada = location.longitude,
                    qrCode = qrCode
                )

                val response = api.registrarAsistencia(request)

                if (response.isSuccessful) {
                    _statusMessage.value = "Asistencia registrada correctamente."
                    _estado.value = EstadoRegistro.EXITO
                } else if (response.code() == 401) {
                    terminarConError("Tu sesión expiró. Cierra sesión y vuelve a entrar.")
                } else {
                    terminarConError("El servidor no pudo registrar la asistencia (código ${response.code()}).")
                }
            } catch (e: Exception) {
                terminarConError("No hay conexión con el servidor. Revisa tu internet.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Deja la pantalla lista para escanear otra vez
    fun reiniciar() {
        _statusMessage.value = null
        _estado.value = EstadoRegistro.ESPERANDO
    }

    private fun terminarConError(mensaje: String) {
        _statusMessage.value = mensaje
        _estado.value = EstadoRegistro.ERROR
    }
}
