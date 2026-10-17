package com.utl.nomi.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.utl.nomi.R

@Composable
fun TopBarComponent(
    onNotificacionesClick: () -> Unit
) {
    val colorRojo = Color(0xFFEF4444)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Nomi",
            modifier = Modifier
                .height(45.dp)
                .width(110.dp)
        )

        Box(modifier = Modifier.size(40.dp)) {
            IconButton(
                onClick = onNotificacionesClick,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_popup_reminder),
                    contentDescription = "Notificaciones",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(colorRojo, RoundedCornerShape(5.dp))
                    .align(Alignment.TopEnd)
            )
        }
    }
}