package com.utl.nomi.data.remote

import com.utl.nomi.data.ApiConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Misma dirección que usa el login (ApiConfig)
    private const val BASE_URL = ApiConfig.BASE_URL + "/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}