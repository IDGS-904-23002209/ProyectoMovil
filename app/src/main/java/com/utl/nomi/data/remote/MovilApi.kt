package com.utl.nomi.data.remote

import com.google.gson.Gson
import com.utl.nomi.data.model.ErrorApi
import com.utl.nomi.data.model.InicioMovil
import com.utl.nomi.data.model.ReciboDetalleMovil
import com.utl.nomi.data.model.ReciboMovil
import com.utl.nomi.data.model.RegistroAsistenciaMovil
import com.utl.nomi.data.model.RegistroAsistenciaMovilRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Endpoints de la app móvil (MovilController en NomiBack).
// Todos piden sesión: el token lo agrega RetrofitClient.
interface MovilApi {

    @GET("api/movil/inicio")
    suspend fun inicio(): Response<InicioMovil>

    @POST("api/movil/asistencia")
    suspend fun registrarAsistencia(@Body request: RegistroAsistenciaMovilRequest): Response<RegistroAsistenciaMovil>

    @GET("api/movil/recibos")
    suspend fun misRecibos(): Response<List<ReciboMovil>>

    @GET("api/movil/recibos/{nominaId}")
    suspend fun reciboDetalle(@Path("nominaId") nominaId: Long): Response<ReciboDetalleMovil>
}

// Lee el cuerpo de una respuesta de error ({ codigo, mensaje, distanciaMetros }).
// Si no se puede leer, regresa null y quien llama pone un mensaje genérico.
fun Response<*>.leerError(): ErrorApi? = try {
    errorBody()?.string()?.let { Gson().fromJson(it, ErrorApi::class.java) }
} catch (e: Exception) {
    null
}
