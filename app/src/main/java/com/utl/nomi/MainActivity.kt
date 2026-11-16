package com.utl.nomi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.utl.nomi.data.remote.RetrofitClient
import com.utl.nomi.presentation.auth.AuthViewModel
import com.utl.nomi.presentation.screens.InicioScreen
import com.utl.nomi.presentation.screens.LoginScreen
import com.utl.nomi.presentation.screens.RecibosScreen
import com.utl.nomi.presentation.screens.NotificacionesScreen
import com.utl.nomi.presentation.screens.PerfilScreen
import com.utl.nomi.presentation.screens.RegistroAsistenciaScreen
import com.utl.nomi.presentation.screens.SolicitudesScreen
import com.utl.nomi.ui.theme.NomiTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrofit lee el token de aquí para mandarlo en cada petición
        RetrofitClient.iniciar(this)
        enableEdgeToEdge()
        setContent {
            NomiTheme {
                var pantallaActual by remember { mutableStateOf("inicio") }
                // Pantalla a la que regresa la flecha de Notificaciones
                var pantallaAnterior by remember { mutableStateOf("inicio") }
                // Datos que Inicio le pasa al flujo de registro
                var registroEsSalida by remember { mutableStateOf(false) }
                var registroSede by remember { mutableStateOf<String?>(null) }
                val sesion = authViewModel.sesion

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            authViewModel.verificando -> Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }

                            // Sin sesión solo existe el login. El registro de empleados
                            // se quitó de aquí: se hace en la web con sesión de RRHH o ADMIN.
                            sesion == null -> LoginScreen(
                                cargando = authViewModel.cargando,
                                error = authViewModel.error,
                                onLoginClick = { usuario, contrasena ->
                                    authViewModel.iniciarSesion(usuario, contrasena)
                                }
                            )
                            else -> {
                                when (pantallaActual) {
                                    "inicio" -> InicioScreen(
                                        onRegistrarClick = { esSalida, sede ->
                                            registroEsSalida = esSalida
                                            registroSede = sede
                                            pantallaActual = "registroAsistencia"
                                        },
                                        onSesionExpirada = {
                                            pantallaActual = "inicio"
                                            authViewModel.cerrarSesion()
                                        },
                                        onNotificacionesClick = {
                                            pantallaAnterior = pantallaActual
                                            pantallaActual = "notificaciones"
                                        },
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
                                        },
                                        onTabSolicitudesClick = {
                                            pantallaActual = "solicitudes"
                                        },
                                        onTabPerfilClick = {
                                            pantallaActual = "perfil"
                                        }
                                    )

                                    // Flujo de registro (pantalla completa, sin barra inferior).
                                    // El servidor saca al empleado del token: ya no se manda su id.
                                    "registroAsistencia" -> RegistroAsistenciaScreen(
                                        esSalida = registroEsSalida,
                                        sede = registroSede,
                                        onVolver = { pantallaActual = "inicio" }
                                    )

                                    // Notificaciones (sin barra inferior; la flecha regresa a donde estaba)
                                    "notificaciones" -> NotificacionesScreen(
                                        onVolver = { pantallaActual = pantallaAnterior },
                                        onAbrirSolicitudes = { pantallaActual = "solicitudes" },
                                        onAbrirRecibos = { pantallaActual = "nomina" }
                                    )

                                    // Mis solicitudes de vacaciones y permisos
                                    "solicitudes" -> SolicitudesScreen(
                                        onNotificacionesClick = {
                                            pantallaAnterior = pantallaActual
                                            pantallaActual = "notificaciones"
                                        },
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
                                        },
                                        onTabSolicitudesClick = {
                                            pantallaActual = "solicitudes"
                                        },
                                        onTabPerfilClick = {
                                            pantallaActual = "perfil"
                                        }
                                    )

                                    // Mis recibos (solo los del usuario con sesión)
                                    "nomina" -> RecibosScreen(
                                        onNotificacionesClick = {
                                            pantallaAnterior = pantallaActual
                                            pantallaActual = "notificaciones"
                                        },
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
                                        },
                                        onTabSolicitudesClick = {
                                            pantallaActual = "solicitudes"
                                        },
                                        onTabPerfilClick = {
                                            pantallaActual = "perfil"
                                        }
                                    )

                                    "perfil" -> PerfilScreen(
                                        sesion = sesion!!,
                                        perfil = authViewModel.perfil,
                                        cargando = authViewModel.cargandoPerfil,
                                        error = authViewModel.errorPerfil,
                                        onCargarPerfil = { authViewModel.cargarPerfil() },
                                        cambiandoContrasena = authViewModel.cambiandoContrasena,
                                        errorContrasena = authViewModel.errorContrasena,
                                        onCambiarContrasena = { actual, nueva, confirmar ->
                                            authViewModel.cambiarContrasena(actual, nueva, confirmar)
                                        },
                                        onLimpiarErrorContrasena = { authViewModel.limpiarErrorContrasena() },
                                        onCerrarSesionClick = {
                                            pantallaActual = "inicio"
                                            authViewModel.cerrarSesion()
                                        },
                                        onNotificacionesClick = {
                                            pantallaAnterior = pantallaActual
                                            pantallaActual = "notificaciones"
                                        },
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
                                        },
                                        onTabSolicitudesClick = {
                                            pantallaActual = "solicitudes"
                                        },
                                        onTabPerfilClick = {
                                            pantallaActual = "perfil"
                                        }
                                    )

                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}