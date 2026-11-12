package com.utl.nomi.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Colores de Material 3 asignados a la paleta de Nomi.
// Solo hay tema claro: el diseño de Figma no tiene versión oscura.
private val EsquemaNomi = lightColorScheme(
    primary = VerdeNomi,
    onPrimary = Blanco,
    primaryContainer = VerdeNomiClaro,
    onPrimaryContainer = VerdeNomiOscuro,
    secondary = AzulMarino,
    onSecondary = Blanco,
    secondaryContainer = InfoFondo,
    onSecondaryContainer = AzulMarino,
    tertiary = Info,
    onTertiary = Blanco,
    background = FondoApp,
    onBackground = TextoPrincipal,
    surface = Blanco,
    onSurface = TextoPrincipal,
    surfaceVariant = FondoApp,
    onSurfaceVariant = TextoSecundario,
    surfaceContainer = Blanco,
    surfaceContainerLow = Blanco,
    surfaceContainerHigh = Blanco,
    outline = Borde,
    outlineVariant = Borde,
    error = Peligro,
    onError = Blanco,
    errorContainer = PeligroFondo,
    onErrorContainer = Peligro,
)

// Esquinas: 8 dp en botones y campos, 12 dp en tarjetas, 16 dp en hojas y diálogos
private val FormasNomi = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun NomiTheme(content: @Composable () -> Unit) {
    // Sin dynamicColor: antes, en Android 12 o más, la app tomaba
    // los colores del fondo de pantalla del teléfono.
    MaterialTheme(
        colorScheme = EsquemaNomi,
        typography = Typography,
        shapes = FormasNomi,
        content = content
    )
}
