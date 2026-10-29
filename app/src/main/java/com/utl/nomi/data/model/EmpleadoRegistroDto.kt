package com.utl.nomi.data.model

import com.google.gson.annotations.SerializedName

// ==========================================
// MODELOS ANIDADOS PARA EL REGISTRO
// ==========================================

data class IdWrapper(
    @SerializedName("Id") val id: Int
)

data class DireccionRegistroDto(
    @SerializedName("Id") val id: Int = 1
)

data class PersonaRegistroDto(
    @SerializedName("Nombre") val nombre: String,
    @SerializedName("Apellido_Paterno") val apellidoPaterno: String,
    @SerializedName("Apellido_Materno") val apellidoMaterno: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("direccion_id") val direccionId: DireccionRegistroDto = DireccionRegistroDto(1)
)

data class UsuarioRegistroDto(
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("rol_id") val rolId: IdWrapper
)

// Este es el DTO principal que coincide con la clase Empleado de C#
data class EmpleadoRegistroDto(
    @SerializedName("empresa_id") val empresaId: IdWrapper,
    @SerializedName("persona_id") val personaId: PersonaRegistroDto,
    @SerializedName("usuarios_id") val usuariosId: UsuarioRegistroDto,
    @SerializedName("departamento_id") val departamentoId: IdWrapper?,
    @SerializedName("turno_id") val turnoId: IdWrapper?,
    @SerializedName("sede_id") val sedeId: IdWrapper,
    @SerializedName("rfc") val rfc: String?,
    @SerializedName("curp") val curp: String?,
    @SerializedName("nss") val nss: String?,
    @SerializedName("fecha_ingreso") val fechaIngreso: String = "2026-10-26T00:00:00"
)

data class RespuestaRegistroDto(
    @SerializedName("codigo") val codigo: Int,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("empleado_id") val empleadoId: Int?,
    @SerializedName("usuario_id") val usuarioId: Int?,
    @SerializedName("persona_id") val personaId: Int?,
    @SerializedName("nombre_usuario") val nombreUsuario: String?
)

// ==========================================
// CATÁLOGOS PARA LOS SELECTS
// ==========================================

data class RolDto(
    val id: Int,
    val nombre: String
) {
    companion object {
        val listaRoles = listOf(
            RolDto(id = 1, nombre = "Administrador RH"),
            RolDto(id = 2, nombre = "Supervisor"),
            RolDto(id = 3, nombre = "Empleado")
        )
    }
}

data class DepartamentoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String
)

data class TurnoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String
)