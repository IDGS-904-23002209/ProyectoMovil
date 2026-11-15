package com.utl.nomi.presentation.components

import java.text.NumberFormat
import java.util.Locale

// Formatos de texto que se usan en varias pantallas

private val formatoPesos: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

// 12750.0 -> "$12,750.00"
fun pesos(monto: Double): String = formatoPesos.format(monto)

// "2026-10-16" o "2026-10-16T00:00:00" -> "16/10/2026"
fun fechaCorta(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "—"
    val partes = fecha.take(10).split("-")
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else fecha
}

// "Ana Martínez Ruiz" -> "AM"
fun iniciales(nombre: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    if (partes.isEmpty()) return "?"
    val segunda = if (partes.size > 1) partes[1].first().toString() else ""
    return (partes[0].first().toString() + segunda).uppercase()
}

// Estado de pago como texto y tono del chip
fun estadoPago(estado: String?): Pair<String, Tono> = when (estado?.uppercase()) {
    "PAGADO", "PAGADA" -> "Pagado" to Tono.EXITO
    "TIMBRADO" -> "Timbrado" to Tono.EXITO
    "PENDIENTE" -> "Pendiente" to Tono.AVISO
    "CANCELADO" -> "Cancelado" to Tono.PELIGRO
    null, "" -> "Sin estado" to Tono.NEUTRO
    else -> estado.orEmpty().lowercase().replaceFirstChar { it.uppercase() } to Tono.NEUTRO
}
