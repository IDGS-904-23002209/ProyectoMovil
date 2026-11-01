package com.utl.nomi.presentation.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.AuthApi
import com.utl.nomi.data.PerfilUsuario
import com.utl.nomi.data.ResultadoApi
import com.utl.nomi.data.SesionUsuario
import com.utl.nomi.data.SessionStore
import kotlinx.coroutines.launch
import java.time.Instant

// Maneja el estado del login: quién tiene la sesión, si está cargando y si hubo error.
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

    // Datos del perfil (se cargan al abrir la pestaña Perfil)
    var perfil by mutableStateOf<PerfilUsuario?>(null)
        private set

    var cargandoPerfil by mutableStateOf(false)
        private set

    var errorPerfil by mutableStateOf<String?>(null)
        private set

    // Estado de la ventana "Cambiar contraseña"
    var cambiandoContrasena by mutableStateOf(false)
        private set

    var errorContrasena by mutableStateOf<String?>(null)
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
                almacen.borrar() // el servidor ya la cerró o expiró
            } else {
                sesion = guardada // vigente (o sin internet: se confía en la guardada)
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

    // Pide el perfil al servidor. Si el token ya no sirve (401), cierra la sesión.
    fun cargarPerfil() {
        val token = sesion?.token ?: return
        cargandoPerfil = true
        errorPerfil = null
        viewModelScope.launch {
            when (val resultado = AuthApi.perfil(token)) {
                is ResultadoApi.Exito -> perfil = resultado.datos
                is ResultadoApi.Error -> {
                    if (resultado.codigo == 401) {
                        cerrarSesion()
                        error = "Tu sesión expiró. Inicia sesión de nuevo."
                    } else {
                        errorPerfil = resultado.mensaje
                    }
                }
            }
            cargandoPerfil = false
        }
    }

    fun limpiarErrorContrasena() {
        errorContrasena = null
    }

    // Valida los campos y pide el cambio. Si sale bien, el servidor cierra las sesiones:
    // se limpia la sesión local y se manda al login con un aviso.
    fun cambiarContrasena(actual: String, nueva: String, confirmar: String) {
        val token = sesion?.token ?: return
        when {
            actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty() ->
                errorContrasena = "Completa todos los campos"
            nueva.length < 6 ->
                errorContrasena = "La contraseña nueva debe tener al menos 6 caracteres"
            nueva != confirmar ->
                errorContrasena = "La confirmación no coincide con la contraseña nueva"
            else -> {
                errorContrasena = null
                cambiandoContrasena = true
                viewModelScope.launch {
                    when (val resultado = AuthApi.cambiarContrasena(token, actual, nueva)) {
                        is ResultadoApi.Exito -> {
                            limpiarSesionLocal()
                            error = "Contraseña actualizada. Inicia sesión con la nueva."
                        }
                        is ResultadoApi.Error -> {
                            if (resultado.codigo == 401) {
                                limpiarSesionLocal()
                                error = "Tu sesión expiró. Inicia sesión de nuevo."
                            } else {
                                errorContrasena = resultado.mensaje
                            }
                        }
                    }
                    cambiandoContrasena = false
                }
            }
        }
    }

    // Borra todo lo de la sesión en este teléfono (sin avisar al servidor)
    private fun limpiarSesionLocal() {
        sesion = null
        perfil = null
        errorPerfil = null
        errorContrasena = null
        error = null
        almacen.borrar()
    }

    fun cerrarSesion() {
        val token = sesion?.token
        limpiarSesionLocal()
        if (token != null) {
            viewModelScope.launch { AuthApi.logout(token) }
        }
    }
}
