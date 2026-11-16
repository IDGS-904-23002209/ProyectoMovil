package com.utl.nomi.presentation.movil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.model.NotificacionMovil
import com.utl.nomi.data.remote.MovilApi
import com.utl.nomi.data.remote.RetrofitClient
import com.utl.nomi.data.remote.leerError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Cuántas notificaciones faltan por leer. Lo usa la campana de la barra
// superior (punto rojo) en todas las pantallas.
object ContadorNotificaciones {
    var noLeidas by mutableIntStateOf(0)
        private set

    suspend fun actualizar(api: MovilApi = RetrofitClient.movilApi) {
        try {
            val r = api.notificaciones()
            if (r.isSuccessful) noLeidas = r.body()?.noLeidas ?: 0
        } catch (e: Exception) {
            // Sin conexión: se queda el último valor
        }
    }

    fun fijar(valor: Int) {
        noLeidas = valor.coerceAtLeast(0)
    }
}

sealed class EstadoNotificaciones {
    object Cargando : EstadoNotificaciones()
    data class Listo(val notificaciones: List<NotificacionMovil>) : EstadoNotificaciones()
    data class Error(val mensaje: String) : EstadoNotificaciones()
}

class NotificacionesViewModel(
    private val api: MovilApi = RetrofitClient.movilApi
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoNotificaciones>(EstadoNotificaciones.Cargando)
    val estado: StateFlow<EstadoNotificaciones> = _estado

    fun cargar() {
        if (_estado.value !is EstadoNotificaciones.Listo) _estado.value = EstadoNotificaciones.Cargando
        viewModelScope.launch {
            _estado.value = try {
                val r = api.notificaciones()
                when {
                    r.isSuccessful -> {
                        val datos = r.body()
                        ContadorNotificaciones.fijar(datos?.noLeidas ?: 0)
                        EstadoNotificaciones.Listo(datos?.notificaciones.orEmpty())
                    }
                    r.code() == 401 -> EstadoNotificaciones.Error("Tu sesión expiró. Inicia sesión de nuevo.")
                    else -> EstadoNotificaciones.Error(r.leerError()?.mensaje ?: "No se pudieron cargar tus notificaciones.")
                }
            } catch (e: Exception) {
                EstadoNotificaciones.Error("No hay conexión con el servidor. Revisa tu internet.")
            }
        }
    }

    // Al tocar una notificación: se marca como leída en pantalla y en el servidor
    fun marcarLeida(notificacion: NotificacionMovil) {
        if (notificacion.leida) return
        cambiarLocal { lista -> lista.map { if (it.id == notificacion.id) it.copy(leida = true) else it } }
        viewModelScope.launch {
            try { api.marcarLeida(notificacion.id) } catch (e: Exception) { /* se reintenta al volver a abrir */ }
        }
    }

    fun marcarTodas() {
        cambiarLocal { lista -> lista.map { it.copy(leida = true) } }
        viewModelScope.launch {
            try { api.marcarTodasLeidas() } catch (e: Exception) { /* se reintenta al volver a abrir */ }
        }
    }

    private fun cambiarLocal(cambio: (List<NotificacionMovil>) -> List<NotificacionMovil>) {
        val actual = _estado.value as? EstadoNotificaciones.Listo ?: return
        val nueva = cambio(actual.notificaciones)
        _estado.value = EstadoNotificaciones.Listo(nueva)
        ContadorNotificaciones.fijar(nueva.count { !it.leida })
    }
}
