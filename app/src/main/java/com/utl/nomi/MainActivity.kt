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
import com.utl.nomi.presentation.screens.NominaScreen
import com.utl.nomi.presentation.screens.NotificacionesScreen
import com.utl.nomi.presentation.screens.PerfilScreen
import com.utl.nomi.presentation.screens.RegistroAsistenciaScreen
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
                                        onRegistrarEntradaClick = {
                                            pantallaActual = "registroAsistencia"
                                        },
                                        onNotificacionesClick = {
                                            pantallaActual = "notificaciones"
                                        },
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
                                        },
                                        onTabPerfilClick = {
                                            pantallaActual = "perfil"
                                        }
                                    )

                                    "registroAsistencia" -> RegistroAsistenciaScreen(
                                        api = RetrofitClient.asistenciaApi,
                                        // El empleado sale del perfil del usuario con sesión
                                        empleadoId = authViewModel.perfil?.empleadoId,
                                        cargandoPerfil = authViewModel.cargandoPerfil ||
                                                (authViewModel.perfil == null && authViewModel.errorPerfil == null),
                                        onCargarPerfil = { authViewModel.cargarPerfil() },
                                        onCancelarClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onNotificacionesClick = {
                                            pantallaActual = "notificaciones"
                                        },
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
                                        },
                                        onTabPerfilClick = {
                                            pantallaActual = "perfil"
                                        }
                                    )

                                    "notificaciones" -> NotificacionesScreen(
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
                                        },
                                        onTabPerfilClick = {
                                            pantallaActual = "perfil"
                                        }
                                    )

                                    "nomina" -> NominaScreen(
                                        onNotificacionesClick = {
                                            pantallaActual = "notificaciones"
                                        },
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
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
                                        onTabInicioClick = {
                                            pantallaActual = "inicio"
                                        },
                                        onTabNominaClick = {
                                            pantallaActual = "nomina"
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