package com.utl.nomi.data.remote

import com.utl.nomi.data.model.AsistenciaDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AsistenciaApi {
    @POST("api/Asistencia") // Coincide con tu [Route("api/[controller]")] de C#
    suspend fun registrarAsistencia(@Body asistencia: AsistenciaDto): Response<Unit>
}