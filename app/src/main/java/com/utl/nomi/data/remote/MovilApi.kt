package com.utl.nomi.data.remote

import com.google.gson.Gson
import com.utl.nomi.data.model.ErrorApi
import com.utl.nomi.data.model.InicioMovil
import com.utl.nomi.data.model.NotificacionesMovil
import com.utl.nomi.data.model.NuevaSolicitudRequest
import com.utl.nomi.data.model.ReciboDetalleMovil
import com.utl.nomi.data.model.ReciboMovil
import com.utl.nomi.data.model.RegistroAsistenciaMovil
import com.utl.nomi.data.model.RegistroAsistenciaMovilRequest
import com.utl.nomi.data.model.RespuestaSolicitud
import com.utl.nomi.data.model.SolicitudMovil
import com.utl.nomi.data.model.TipoPermiso
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("api/movil/solicitudes/tipos")
    suspend fun tiposPermiso(): Response<List<TipoPermiso>>

    @GET("api/movil/solicitudes")
    suspend fun misSolicitudes(): Response<List<SolicitudMovil>>

    @POST("api/movil/solicitudes")
    suspend fun crearSolicitud(@Body request: NuevaSolicitudRequest): Response<RespuestaSolicitud>

    @GET("api/movil/notificaciones")
    suspend fun notificaciones(): Response<NotificacionesMovil>

    @PUT("api/movil/notificaciones/{id}/leida")
    suspend fun marcarLeida(@Path("id") id: Long): Response<Unit>

    @PUT("api/movil/notificaciones/leidas")
    suspend fun marcarTodasLeidas(): Response<Unit>
}

// Lee el cuerpo de una respuesta de error ({ codigo, mensaje, distanciaMetros }).
// Si no se puede leer, regresa null y quien llama pone un mensaje genérico.
fun Response<*>.leerError(): ErrorApi? = try {
    errorBody()?.string()?.let { Gson().fromJson(it, ErrorApi::class.java) }
} catch (e: Exception) {
    null
}
