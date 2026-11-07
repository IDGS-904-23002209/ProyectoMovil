package com.utl.nomi.presentation.auth.nomina

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.util.nomi.data.model.AsistenciaDto
import com.utl.nomi.data.remote.AsistenciaApi
import com.util.nomi.data.remote.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AsistenciaViewModel(
    private val api: AsistenciaApi
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val OFFICE_LAT = -12.046374
    private val OFFICE_LON = -77.042793
    private val MAX_DISTANCE_METERS = 50.0

    fun validarYRegistrar(context: Context, empleadoId: Int, qrCode: String) {
        // Evitar doble envío
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Verificando ubicación GPS..."

            try {
                val locationHelper = LocationHelper(context)
                val location = locationHelper.getCurrentLocation()

                if (location == null) {
                    _statusMessage.value = "Error: No se pudo obtener tu ubicación. Activa el GPS."
                    return@launch
                }

                val distancia = locationHelper.calcularDistancia(
                    location.latitude, location.longitude,
                    OFFICE_LAT, OFFICE_LON
                )

                if (distancia <= MAX_DISTANCE_METERS) {
                    _statusMessage.value = "Ubicación verificada. Registrando asistencia..."

                    // Creamos el DTO con los datos que pide el backend C#
                    val request = AsistenciaDto(
                        empleadoId = empleadoId,
                        latitudEntrada = location.latitude,
                        longitudEntrada = location.longitude,
                        qrCode = qrCode
                    )

                    val response = api.registrarAsistencia(request)

                    if (response.isSuccessful) {
                        _statusMessage.value = "Asistencia registrada correctamente."
                    } else {
                        _statusMessage.value = "Error del servidor: Código ${response.code()}"
                    }

                } else {
                    _statusMessage.value = "Estás a ${distancia.toInt()}m de la oficina. Debes estar a menos de ${MAX_DISTANCE_METERS.toInt()}m."
                }

            } catch (e: Exception) {
                _statusMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}