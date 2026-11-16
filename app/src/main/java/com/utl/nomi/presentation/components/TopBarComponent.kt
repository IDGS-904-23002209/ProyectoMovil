package com.utl.nomi.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.utl.nomi.R
import com.utl.nomi.presentation.movil.ContadorNotificaciones

// Barra superior de las pantallas principales: el logo de Nomi a la izquierda
// y la campana de notificaciones a la derecha (con punto rojo si hay sin leer).
// El punto rojo sale del servidor (GET api/movil/notificaciones) y se
// actualiza cada vez que se abre una pantalla con esta barra.
@Composable
fun TopBarComponent(
    onNotificacionesClick: () -> Unit,
    hayPendientes: Boolean = ContadorNotificaciones.noLeidas > 0
) {
    LaunchedEffect(Unit) { ContadorNotificaciones.actualizar() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoNomi(alto = 40.dp)

        Box {
            IconButton(onClick = onNotificacionesClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (hayPendientes) {
                // Punto rojo con borde blanco
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

// Logo oficial de Nomi (res/drawable-nodpi/logo.png, con fondo transparente
// para que se vea bien sobre el gris de la app y sobre blanco).
// Se usa en la barra superior y en el Login.
@Composable
fun LogoNomi(alto: Dp = 40.dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Logo Nomi",
        contentScale = ContentScale.Fit,
        modifier = modifier.height(alto)
    )
}
