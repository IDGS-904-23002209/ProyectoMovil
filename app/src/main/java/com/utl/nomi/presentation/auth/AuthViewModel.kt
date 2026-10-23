package com.utl.nomi.presentation.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.AuthApi
import com.utl.nomi.data.ResultadoApi
import com.utl.nomi.data.SesionUsuario
import com.utl.nomi.data.SessionStore
import kotlinx.coroutines.launch
import java.time.Instant

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val almacen = SessionStore(app)

    // Sesión actual (null = nadie ha iniciado sesión)
    var sesion by mutableStateOf<SesionUsuario?>(null)
        private set

    // true mientras se espera la respuesta del servidor al iniciar sesión
    var cargando by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // true al abrir la app, mientras se revisa si la sesión guardada sigue vigente
    var verificando by mutableStateOf(true)
        private set

    init {
        restaurarSesion()
    }

    private fun estaVigente(s: SesionUsuario): Boolean {
        return try {
            Instant.parse(s.expiracion).isAfter(Instant.now())
        } catch (e: Exception) {
            false
        }
    }

    // Al abrir la app: si hay una sesión guardada y vigente, entra directo
    private fun restaurarSesion() {
        val guardada = almacen.leer()
        if (guardada == null || !estaVigente(guardada)) {
            almacen.borrar()
            verificando = false
            return
        }
        viewModelScope.launch {
            val resultado = AuthApi.me(guardada.token)
            if (resultado is ResultadoApi.Error && resultado.codigo == 401) {
                almacen.borrar()
            } else {
                sesion = guardada
            }
            verificando = false
        }
    }

    fun iniciarSesion(usuario: String, contrasena: String) {
        if (usuario.isBlank() || contrasena.isEmpty()) {
            error = "Escribe tu usuario y contraseña"
            return
        }
        error = null
        cargando = true
        viewModelScope.launch {
            when (val resultado = AuthApi.login(usuario.trim(), contrasena)) {
                is ResultadoApi.Exito -> {
                    almacen.guardar(resultado.datos)
                    sesion = resultado.datos
                }
                is ResultadoApi.Error -> error = resultado.mensaje
            }
            cargando = false
        }
    }

    fun cerrarSesion() {
        val token = sesion?.token
        sesion = null
        error = null
        almacen.borrar()
        if (token != null) {
            viewModelScope.launch { AuthApi.logout(token) }
        }
    }
}
