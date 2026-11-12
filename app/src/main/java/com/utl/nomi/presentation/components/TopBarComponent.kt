package com.utl.nomi.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Barra superior de las pantallas principales: logo de Nomi a la izquierda
// y campana de notificaciones a la derecha (con punto rojo si hay pendientes).
// Se coloca dentro de la columna de cada pantalla, como antes.
@Composable
fun TopBarComponent(
    onNotificacionesClick: () -> Unit,
    hayPendientes: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoNomi()

        Box {
            IconButton(onClick = onNotificacionesClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (hayPendientes) {
                // Punto rojo con borde blanco, como en el Figma
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
            }
        }
    }
}

// Logo: cuadro verde claro con un reloj + la palabra "Nomi".
// Se dibuja con Compose para que se vea nítido en cualquier pantalla.
@Composable
fun LogoNomi(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = "Nomi",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
