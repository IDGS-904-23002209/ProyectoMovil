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
import com.utl.nomi.presentation.auth.AuthViewModel
import com.utl.nomi.presentation.auth.EmpleadoViewModel // <-- Asegúrate de importar tu ViewModel de empleado
import com.utl.nomi.presentation.screens.InicioScreen
import com.utl.nomi.presentation.screens.LoginScreen
import com.utl.nomi.presentation.screens.NominaScreen
import com.utl.nomi.presentation.screens.NotificacionesScreen
import com.utl.nomi.presentation.screens.PerfilScreen
import com.utl.nomi.presentation.screens.RegistroAsistenciaScreen
import com.utl.nomi.presentation.screens.RegistroEmpleadoScreen
import com.utl.nomi.ui.theme.NomiTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val empleadoViewModel: EmpleadoViewModel by viewModels() // <-- Declaramos el ViewModel de empleados

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NomiTheme {
                var pantallaActual by remember { mutableStateOf("inicio") }
                val sesion = authViewModel.sesion

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            // Al abrir la app: se revisa si hay una sesión guardada
                            authViewModel.verificando -> Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }

                            // Sin sesión: pantalla de login o registro
                            sesion == null -> {
                                when (pantallaActual) {
                                    "registroEmpleado" -> RegistroEmpleadoScreen(
                                        empleadoViewModel = empleadoViewModel, // <--- Pasamos el ViewModel correcto aquí
                                        onCancelarClick = {
                                            pantallaActual = "inicio" // Vuelve al login
                                        },
                                        onGuardarExitoso = {
                                            pantallaActual = "inicio" // Vuelve al login al registrar con éxito
                                        }
                                    )
                                    else -> LoginScreen(
                                        cargando = authViewModel.cargando,
                                        error = authViewModel.error,
                                        onLoginClick = { usuario, contrasena ->
                                            authViewModel.iniciarSesion(usuario, contrasena)
                                        },
                                        onRegistrarEmpleadoClick = {
                                            pantallaActual = "registroEmpleado" // Salta a la pantalla de registro
                                        }
                                    )
                                }
                            }

                            // Con sesión: el resto de la app
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