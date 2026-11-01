package com.utl.nomi.data

// Datos de la sesión que devuelve POST /api/auth/login
data class SesionUsuario(
    val token: String,
    val expiracion: String, // fecha en UTC, formato ISO (por ejemplo 2026-10-23T14:00:36Z)
    val id: Int,
    val nombre: String,
    val rol: String
)

// Datos del perfil que devuelve GET /api/auth/perfil.
// Los datos de empleado (RFC, NSS, etc.) son null si el usuario no tiene un empleado asociado.
data class PerfilUsuario(
    val usuarioId: Int,
    val nombreUsuario: String,
    val rol: String,
    val nombreCompleto: String,
    val correo: String?,
    val telefono: String?,
    val empleadoId: Int?,
    val rfc: String?,
    val curp: String?,
    val nss: String?,
    val fechaIngreso: String?, // formato yyyy-MM-dd
    val empresa: String?,
    val departamento: String?,
    val turno: String?,
    val sede: String?
)

// Resultado de una llamada a la API: o salió bien (Exito) o hubo un problema (Error)
sealed class ResultadoApi<out T> {
    data class Exito<T>(val datos: T) : ResultadoApi<T>()
    data class Error(val mensaje: String, val codigo: Int? = null) : ResultadoApi<Nothing>()
}
