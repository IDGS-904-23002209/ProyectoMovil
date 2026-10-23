package com.utl.nomi.data

data class SesionUsuario(
    val token: String,
    val expiracion: String,
    val id: Int,
    val nombre: String,
    val rol: String
)

sealed class ResultadoApi<out T> {
    data class Exito<T>(val datos: T) : ResultadoApi<T>()
    data class Error(val mensaje: String, val codigo: Int? = null) : ResultadoApi<Nothing>()
}
