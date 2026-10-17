package com.utl.nomi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.utl.nomi.presentation.screens.InicioScreen
import com.utl.nomi.presentation.screens.LoginScreen
import com.utl.nomi.presentation.screens.NominaScreen
import com.utl.nomi.presentation.screens.NotificacionesScreen
import com.utl.nomi.presentation.screens.PerfilScreen
import com.utl.nomi.presentation.screens.RegistroAsistenciaScreen
import com.utl.nomi.ui.theme.NomiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NomiTheme {
                var pantallaActual by remember { mutableStateOf("login") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (pantallaActual) {
                            "login" -> LoginScreen(
                                onLoginClick = { numeroEmpleado, password ->
                                    pantallaActual = "inicio"
                                },
                                onForgotPasswordClick = { }
                            )

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
                        }
                    }
                }
            }
        }
    }
}