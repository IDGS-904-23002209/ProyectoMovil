package com.utl.nomi.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utl.nomi.data.model.*
import com.utl.nomi.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class EmpleadoViewModel : ViewModel() {

    val listaRoles = RolDto.listaRoles

    var listaDepartamentos by mutableStateOf<List<DepartamentoDto>>(emptyList())
        private set

    var listaTurnos by mutableStateOf<List<TurnoDto>>(emptyList())
        private set

    var cargandoCatalogos by mutableStateOf(false)
        private set

    var registroCargando by mutableStateOf(false)
        private set

    var registroError by mutableStateOf<String?>(null)
        private set

    var registroExitoso by mutableStateOf(false)
        private set

    init {
        cargarCatalogos()
    }

    fun cargarCatalogos() {
        viewModelScope.launch {
            cargandoCatalogos = true
            try {
                val resDepartamentos = RetrofitClient.apiService.obtenerDepartamentos()
                if (resDepartamentos.isSuccessful) {
                    listaDepartamentos = resDepartamentos.body().orEmpty()
                }

                val resTurnos = RetrofitClient.apiService.obtenerTurnos()
                if (resTurnos.isSuccessful) {
                    listaTurnos = resTurnos.body().orEmpty()
                }
            } catch (e: Exception) {
                // Silencioso o manejo de error
            } finally {
                cargandoCatalogos = false
            }
        }
    }

    fun registrarEmpleado(datos: Map<String, String>, onExito: () -> Unit) {
        val nombre = datos["nombre"].orEmpty()
        val apellidoPaterno = datos["apellido_paterno"].orEmpty()
        val nombreUsuario = datos["nombre_usuario"].orEmpty()
        val contrasena = datos["contrasena"].orEmpty()

        if (nombre.isBlank() || apellidoPaterno.isBlank()) {
            registroError = "El nombre y apellido paterno son obligatorios."
            return
        }

        if (nombreUsuario.isBlank() || contrasena.isBlank()) {
            registroError = "El nombre de usuario y la contraseña son obligatorios."
            return
        }

        viewModelScope.launch {
            registroCargando = true
            registroError = null
            registroExitoso = false

            try {
                // Conversión de IDs seleccionados
                val rolIdInt = datos["rol_id"]?.toIntOrNull() ?: 3
                val deptoIdInt = datos["departamento_id"]?.takeIf { it.isNotBlank() }?.toIntOrNull()
                val turnoIdInt = datos["turno_id"]?.takeIf { it.isNotBlank() }?.toIntOrNull()

                // Armamos la estructura exacta que espera C# (idéntica a Postman)
                val dto = EmpleadoRegistroDto(
                    empresaId = IdWrapper(1),
                    personaId = PersonaRegistroDto(
                        nombre = nombre,
                        apellidoPaterno = apellidoPaterno,
                        apellidoMaterno = datos["apellido_materno"]?.takeIf { it.isNotBlank() },
                        correo = datos["correo"]?.takeIf { it.isNotBlank() },
                        telefono = datos["telefono"]?.takeIf { it.isNotBlank() }
                    ),
                    usuariosId = UsuarioRegistroDto(
                        nombreUsuario = nombreUsuario,
                        contrasena = contrasena,
                        rolId = IdWrapper(rolIdInt)
                    ),
                    departamentoId = deptoIdInt?.let { IdWrapper(it) },
                    turnoId = turnoIdInt?.let { IdWrapper(it) },
                    sedeId = IdWrapper(1),
                    rfc = datos["rfc"]?.takeIf { it.isNotBlank() },
                    curp = datos["curp"]?.takeIf { it.isNotBlank() },
                    nss = datos["nss"]?.takeIf { it.isNotBlank() }
                )

                val response = RetrofitClient.apiService.registrarEmpleado(dto)

                if (response.isSuccessful) {
                    val respuesta = response.body()
                    if (respuesta?.codigo == 1) {
                        registroExitoso = true
                        onExito()
                    } else {
                        registroError = respuesta?.mensaje ?: "No se pudo registrar el empleado."
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    registroError = "Error 400: ${errorBody ?: "Datos inválidos"}"
                }

            } catch (e: Exception) {
                registroError = "Error de conexión: ${e.localizedMessage}"
            } finally {
                registroCargando = false
            }
        }
    }
}