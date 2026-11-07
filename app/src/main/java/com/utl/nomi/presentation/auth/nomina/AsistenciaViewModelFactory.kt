package com.util.nomi.presentation.nomina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.utl.nomi.data.remote.AsistenciaApi
import com.utl.nomi.presentation.auth.nomina.AsistenciaViewModel

class AsistenciaViewModelFactory(private val api: AsistenciaApi) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AsistenciaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AsistenciaViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}