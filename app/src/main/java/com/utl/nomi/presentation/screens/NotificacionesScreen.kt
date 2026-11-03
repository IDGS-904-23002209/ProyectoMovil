package com.utl.nomi.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utl.nomi.data.model.NotificacionDto
import com.utl.nomi.presentation.auth.notificaciones.NotificacionesViewModel
import com.utl.nomi.presentation.components.BottomBarComponent
import com.utl.nomi.presentation.components.TopBarComponent

@Composable
fun NotificacionesScreen(
    notificacionesViewModel: NotificacionesViewModel = NotificacionesViewModel(),
    onTabInicioClick: () -> Unit = {},
    onTabNominaClick: () -> Unit = {},
    onTabPerfilClick: () -> Unit = {}
) {
    val colorGrisBlanco = Color(0xFFF8FAFC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorGrisBlanco)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            TopBarComponent(onNotificacionesClick = { })

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NOTIFICACIONES",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Hoy
            Text(
                text = "Hoy",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            notificacionesViewModel.notificacionesHoy.forEach { notif ->
                ItemNotificacion(
                    titulo = notif.titulo,
                    subtitulo = notif.mensaje,
                    iconRes = when {
                        notif.titulo.contains("Aprobacion", true) -> android.R.drawable.checkbox_on_background
                        notif.titulo.contains("Recibo", true) -> android.R.drawable.ic_menu_agenda
                        else -> android.R.drawable.ic_menu_info_details
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Ayer
            Text(
                text = "Ayer",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            notificacionesViewModel.notificacionesAyer.forEach { notif ->
                ItemNotificacion(
                    titulo = notif.titulo,
                    subtitulo = notif.mensaje,
                    iconRes = android.R.drawable.ic_dialog_alert
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomBarComponent(
                currentTab = "",
                onTabInicioClick = onTabInicioClick,
                onTabNominaClick = onTabNominaClick,
                onTabPerfilClick = onTabPerfilClick
            )
        }
    }
}

@Composable
fun ItemNotificacion(
    titulo: String,
    subtitulo: String,
    iconRes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = Color(0xFF555555),
                    lineHeight = 16.sp
                )
            }
        }
    }
}