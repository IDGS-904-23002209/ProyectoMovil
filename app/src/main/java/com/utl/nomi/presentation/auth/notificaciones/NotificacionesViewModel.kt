package com.utl.nomi.presentation.auth.notificaciones

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.utl.nomi.data.model.NotificacionDto

class NotificacionesViewModel : ViewModel() {

    // Lista de notificaciones de ejemplo o conectadas a tu API/FCM
    var notificacionesHoy by mutableStateOf(
        listOf(
            NotificacionDto(1, "Aprobacion Solicitud", "El recibo de Nomina ya esta listo para su descarga", "HOY", null),
            NotificacionDto(2, "Recibo Disponible", "Tu recibo con goce ya ha sido aprobado", "HOY", null)
        )
    )
        private set

    var notificacionesAyer by mutableStateOf(
        listOf(
            NotificacionDto(3, "Asistencia", "Hiciste el registro de asistencia fuera del tiempo establecido", "AYER", null)
        )
    )
        private set

    var cargando by mutableStateOf(false)
        private set

    // Aquí puedes agregar un método para registrar el token FCM en tu backend si lo requieres
    fun registrarTokenFcm(token: String) {
        // Lógica opcional para enviar el token a tu API de .NET
    }
}