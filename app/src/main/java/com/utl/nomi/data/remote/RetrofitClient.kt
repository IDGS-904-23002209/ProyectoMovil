package com.utl.nomi.data.remote

import android.content.Context
import com.utl.nomi.data.ApiConfig
import com.utl.nomi.data.SessionStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Único cliente de Retrofit de la app. Todas las APIs salen de aquí,
// con la misma dirección (ApiConfig) y con el token de la sesión.
object RetrofitClient {
    // Misma dirección que usa el login (ApiConfig)
    private const val BASE_URL = ApiConfig.BASE_URL + "/"

    // Se asigna una vez en MainActivity con iniciar(contexto)
    private var almacen: SessionStore? = null

    fun iniciar(contexto: Context) {
        if (almacen == null) almacen = SessionStore(contexto.applicationContext)
    }

    // Agrega "Authorization: Bearer <token>" a cada petición si hay sesión guardada.
    // Así el servidor sabe quién hace la petición cuando el endpoint pide sesión.
    private val interceptorToken = Interceptor { cadena ->
        val token = almacen?.leer()?.token
        val peticion = if (token != null) {
            cadena.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            cadena.request()
        }
        cadena.proceed(peticion)
    }

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(interceptorToken)
            // somee tarda en "despertar" el sitio: se espera hasta 30 segundos
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }

    // Endpoints de la app móvil: inicio, registro de asistencia y recibos
    val movilApi: MovilApi by lazy { retrofit.create(MovilApi::class.java) }
}
