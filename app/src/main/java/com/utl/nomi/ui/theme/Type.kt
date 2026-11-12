package com.utl.nomi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tamaños de texto de Nomi. Cada pantalla usa MaterialTheme.typography.<estilo>
// en lugar de escribir fontSize a mano.
//
// headlineMedium  -> saludo o título grande de pantalla ("Buenos días, Luis")
// headlineSmall   -> título de pantalla secundaria ("Mis recibos")
// titleLarge      -> número o dato destacado (hora del registro, neto a pagar)
// titleMedium     -> título de tarjeta
// titleSmall      -> título de fila o nombre dentro de una lista
// bodyLarge       -> texto normal
// bodyMedium      -> texto de apoyo
// bodySmall       -> notas pequeñas
// labelLarge      -> texto de botones
// labelMedium     -> etiquetas de campos y de la barra inferior
// labelSmall      -> encabezados en mayúsculas ("TURNO DE HOY") y chips
private val Fuente = FontFamily.Default

val Typography = Typography(
    headlineMedium = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    headlineSmall = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.6.sp),
)
