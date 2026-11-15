package com.utl.nomi.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.utl.nomi.ui.theme.Aviso
import com.utl.nomi.ui.theme.AvisoFondo
import com.utl.nomi.ui.theme.Exito
import com.utl.nomi.ui.theme.ExitoFondo
import com.utl.nomi.ui.theme.Info
import com.utl.nomi.ui.theme.InfoFondo
import com.utl.nomi.ui.theme.Peligro
import com.utl.nomi.ui.theme.PeligroFondo
import com.utl.nomi.ui.theme.TextoSecundario

// Piezas visuales que se repiten en todas las pantallas del móvil,
// para que todas se vean igual (las mismas del Figma).

// Tono de color de chips e iconos
enum class Tono { PRINCIPAL, EXITO, AVISO, PELIGRO, INFO, NEUTRO }

// Color del texto/icono y color del fondo de cada tono
@Composable
private fun colores(tono: Tono): Pair<Color, Color> = when (tono) {
    Tono.PRINCIPAL -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
    Tono.EXITO -> Exito to ExitoFondo
    Tono.AVISO -> Aviso to AvisoFondo
    Tono.PELIGRO -> Peligro to PeligroFondo
    Tono.INFO -> Info to InfoFondo
    Tono.NEUTRO -> TextoSecundario to Color(0xFFEEF1F4)
}

// Tarjeta blanca con borde fino y esquinas de 12 dp
@Composable
fun TarjetaNomi(
    modifier: Modifier = Modifier,
    relleno: Dp = 16.dp,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(relleno), content = contenido)
    }
}

// Etiqueta de estado redondeada: "Puntual", "Retardo", "En turno"...
@Composable
fun ChipEstado(texto: String, tono: Tono, modifier: Modifier = Modifier) {
    val (frente, fondo) = colores(tono)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = texto, style = MaterialTheme.typography.labelSmall, color = frente)
    }
}

// Icono dentro de un cuadro de color suave (esquinas redondeadas)
@Composable
fun IconoRedondo(
    icono: ImageVector,
    tono: Tono = Tono.PRINCIPAL,
    tamano: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val (frente, fondo) = colores(tono)
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(tamano * 0.3f))
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = frente, modifier = Modifier.size(tamano * 0.55f))
    }
}
