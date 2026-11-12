package com.utl.nomi.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta de Nomi, tomada del diseño de Figma (la misma que usa la web).
// Regla del equipo: ninguna pantalla escribe Color(0xFF...) a mano;
// se usa MaterialTheme.colorScheme.* o los colores de estado de abajo.

// Marca
val VerdeNomi = Color(0xFF0E7C86)        // Principal: botones, pestaña activa, enlaces
val VerdeNomiOscuro = Color(0xFF0A6B74)  // Principal presionado
val VerdeNomiClaro = Color(0xFFE5F4F3)   // Fondos suaves de iconos y avatar
val AzulMarino = Color(0xFF0B2545)       // Textos fuertes y acentos oscuros

// Neutros
val FondoApp = Color(0xFFF5F7FA)         // Fondo de todas las pantallas
val Blanco = Color(0xFFFFFFFF)           // Tarjetas y barras
val TextoPrincipal = Color(0xFF102A43)
val TextoSecundario = Color(0xFF5B6B7B)
val Borde = Color(0xFFE5EAF0)

// Estados (chips, mensajes, iconos)
val Exito = Color(0xFF2E9E6B)
val ExitoFondo = Color(0xFFE8F6EF)
val Aviso = Color(0xFFB06D05)
val AvisoFondo = Color(0xFFFFF4DF)
val Peligro = Color(0xFFD64545)
val PeligroFondo = Color(0xFFFDEAEA)
val Info = Color(0xFF3B70B7)
val InfoFondo = Color(0xFFE9F1FB)
