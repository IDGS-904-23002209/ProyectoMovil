package com.utl.nomi.presentation.auth.nomina

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.model.NominaDto
import com.utl.nomi.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class NominaViewModel : ViewModel() {
    var recibos by mutableStateOf<List<NominaDto>>(emptyList())
        private set

    var cargando by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun cargarNominaPeriodo(periodoId: Int) {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                // Llama a tu API de C# configurada en RetrofitClient
                val respuesta = RetrofitClient.apiService.obtenerNominaPorPeriodo(periodoId)
                recibos = respuesta
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Error al conectar con el servidor de nómina."
            } finally {
                cargando = false
            }
        }
    }
}