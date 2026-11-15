package com.utl.nomi.presentation.movil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.model.InicioMovil
import com.utl.nomi.data.remote.MovilApi
import com.utl.nomi.data.remote.RetrofitClient
import com.utl.nomi.data.remote.leerError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Lo que puede mostrar la pantalla de Inicio
sealed class EstadoInicio {
    object Cargando : EstadoInicio()
    data class Listo(val datos: InicioMovil) : EstadoInicio()
    data class Error(val mensaje: String, val sesionExpirada: Boolean = false) : EstadoInicio()
}

class InicioViewModel(
    private val api: MovilApi = RetrofitClient.movilApi
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoInicio>(EstadoInicio.Cargando)
    val estado: StateFlow<EstadoInicio> = _estado

    // Se llama cada vez que se entra a Inicio, para ver el registro del día actualizado
    fun cargar() {
        // Si ya hay datos, no se borran mientras se actualiza (evita el parpadeo)
        if (_estado.value !is EstadoInicio.Listo) _estado.value = EstadoInicio.Cargando

        viewModelScope.launch {
            _estado.value = try {
                val r = api.inicio()
                when {
                    r.isSuccessful && r.body() != null -> EstadoInicio.Listo(r.body()!!)
                    r.code() == 401 -> EstadoInicio.Error("Tu sesión expiró. Inicia sesión de nuevo.", sesionExpirada = true)
                    else -> EstadoInicio.Error(r.leerError()?.mensaje ?: "No se pudo cargar tu información.")
                }
            } catch (e: Exception) {
                EstadoInicio.Error("No hay conexión con el servidor. Revisa tu internet.")
            }
        }
    }
}
