package com.utl.nomi.data.model

data class NotificacionDto(
    val id: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: String, // Ejemplo: "HOY" o "AYER" / "NOMINA" / "ASISTENCIA"
    val fechaCreacion: String?
)