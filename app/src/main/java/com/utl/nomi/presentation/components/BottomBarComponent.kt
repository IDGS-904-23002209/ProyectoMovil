package com.utl.nomi.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Una pestaña de la barra inferior
private data class Pestana(
    val id: String,
    val texto: String,
    val iconoActivo: ImageVector,
    val iconoInactivo: ImageVector,
    val alPulsar: () -> Unit
)

// Barra inferior de Material 3 con icono y nombre en cada pestaña.
// currentTab: "inicio", "nomina", "solicitudes" o "perfil".
@Composable
fun BottomBarComponent(
    currentTab: String,
    onTabInicioClick: () -> Unit,
    onTabNominaClick: () -> Unit,
    onTabPerfilClick: () -> Unit,
    onTabSolicitudesClick: () -> Unit = {}
) {
    val pestanas = listOf(
        Pestana("inicio", "Inicio", Icons.Filled.Home, Icons.Outlined.Home, onTabInicioClick),
        Pestana("nomina", "Recibos", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, onTabNominaClick),
        Pestana("solicitudes", "Solicitudes", Icons.Filled.EditCalendar, Icons.Outlined.EditCalendar, onTabSolicitudesClick),
        Pestana("perfil", "Perfil", Icons.Filled.Person, Icons.Outlined.Person, onTabPerfilClick),
    )

    Column {
        // Línea fina arriba de la barra, como en el Figma
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            // El Scaffold de MainActivity ya deja el espacio de la barra del sistema;
            // sin esto el espacio se sumaba dos veces y la barra se veía muy alta
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            pestanas.forEach { pestana ->
                val activa = currentTab == pestana.id
                NavigationBarItem(
                    selected = activa,
                    // Si ya está en esa pestaña no vuelve a navegar
                    onClick = { if (!activa) pestana.alPulsar() },
                    icon = {
                        Icon(
                            imageVector = if (activa) pestana.iconoActivo else pestana.iconoInactivo,
                            contentDescription = pestana.texto
                        )
                    },
                    label = { Text(pestana.texto, style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
