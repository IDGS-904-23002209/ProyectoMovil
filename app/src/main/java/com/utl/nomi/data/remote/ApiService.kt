package com.utl.nomi.data.remote

import com.utl.nomi.data.model.DepartamentoDto
import com.utl.nomi.data.model.EmpleadoRegistroDto
import com.utl.nomi.data.model.RespuestaRegistroDto
import com.utl.nomi.data.model.TurnoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Endpoints del registro de empleado (módulo de Uriel).
// Se conserva para reactivar el registro dentro de la app, solo para ADMIN y RRHH.
interface ApiService {

    @POST("api/Empleado/Crear")
    suspend fun registrarEmpleado(
        @Body empleado: EmpleadoRegistroDto
    ): Response<RespuestaRegistroDto>

    @GET("api/Turno/Resumen")
    suspend fun obtenerTurnos(): Response<List<TurnoDto>>

    @GET("api/Departamento")
    suspend fun obtenerDepartamentos(): Response<List<DepartamentoDto>>

}