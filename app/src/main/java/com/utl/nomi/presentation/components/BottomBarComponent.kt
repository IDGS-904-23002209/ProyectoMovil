package com.utl.nomi.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.utl.nomi.R

@Composable
fun BottomBarComponent(
    currentTab: String,
    onTabInicioClick: () -> Unit,
    onTabNominaClick: () -> Unit,
    onTabPerfilClick: () -> Unit
) {
    val colorVerde = Color(0xFF10B981)
    val colorAzul = Color(0xFF1E3A8A)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pestaña Inicio
            IconButton(onClick = onTabInicioClick) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.icono),
                        contentDescription = "Inicio",
                        modifier = Modifier.size(26.dp)
                    )
                    if (currentTab == "inicio") {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(28.dp)
                                .height(2.dp)
                                .background(colorVerde)
                        )
                    }
                }
            }

            // Pestaña Nómina
            IconButton(onClick = onTabNominaClick) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.recibo),
                        contentDescription = "Nómina",
                        modifier = Modifier.size(26.dp)
                    )
                    if (currentTab == "nomina") {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(26.dp)
                                .height(2.dp)
                                .background(colorAzul)
                        )
                    }
                }
            }

            // Pestaña Perfil
            IconButton(onClick = onTabPerfilClick) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = "Perfil",
                        modifier = Modifier.size(26.dp)
                    )
                    if (currentTab == "perfil") {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(26.dp)
                                .height(2.dp)
                                .background(colorAzul)
                        )
                    }
                }
            }
        }
    }
}