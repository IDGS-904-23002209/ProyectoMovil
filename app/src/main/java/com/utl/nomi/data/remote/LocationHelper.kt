package com.utl.nomi.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Pide una ubicación nueva con GPS. Antes se usaba lastLocation,
    // que en el emulador y en teléfonos recién encendidos casi siempre viene vacía.
    // Si no se consigue una nueva, intenta con la última conocida.
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        val nueva = suspendCancellableCoroutine<Location?> { continuation ->
            val cancelacion = CancellationTokenSource()
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelacion.token)
                .addOnSuccessListener { location -> continuation.resume(location) }
                .addOnFailureListener { continuation.resume(null) }
            continuation.invokeOnCancellation { cancelacion.cancel() }
        }
        if (nueva != null) return nueva

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location -> continuation.resume(location) }
                .addOnFailureListener { continuation.resume(null) }
        }
    }

    // Distancia en metros entre dos puntos (fórmula de Haversine)
    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c * 1000
    }
}
