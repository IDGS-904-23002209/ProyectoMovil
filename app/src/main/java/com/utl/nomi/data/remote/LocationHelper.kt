package com.utl.nomi.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.SystemClock
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

    // Revisa que la ubicación sea confiable antes de mandarla al servidor.
    // Regresa el mensaje para el empleado, o null si está bien.
    fun problemaCon(ubicacion: Location): String? {
        // 1. Ubicación simulada (apps de "GPS falso" o ubicación de prueba)
        val simulada = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ubicacion.isMock
        else esSimuladaAndroidViejo(ubicacion)
        if (simulada) {
            return "Tu teléfono está usando una ubicación simulada. Desactiva las apps de GPS falso para registrar tu asistencia."
        }

        // 2. Ubicación vieja: lastLocation puede ser de hace horas (de cuando estabas en la planta)
        val edadMs = (SystemClock.elapsedRealtimeNanos() - ubicacion.elapsedRealtimeNanos) / 1_000_000
        if (edadMs > EDAD_MAXIMA_MS) {
            return "No se pudo obtener tu ubicación actual. Activa el GPS, espera unos segundos e intenta de nuevo."
        }

        // 3. Señal débil: con un margen de error tan grande no se puede saber si estás en la planta
        if (ubicacion.hasAccuracy() && ubicacion.accuracy > PRECISION_MINIMA_M) {
            return "La señal del GPS es débil (±${ubicacion.accuracy.toInt()} m). Acércate a una ventana o sal a un lugar abierto e intenta de nuevo."
        }
        return null
    }

    // En Android 11 o anterior el aviso se llama distinto (ya está marcado como viejo)
    @Suppress("DEPRECATION")
    private fun esSimuladaAndroidViejo(ubicacion: Location): Boolean = ubicacion.isFromMockProvider

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

    companion object {
        private const val EDAD_MAXIMA_MS = 2 * 60 * 1000L   // 2 minutos
        private const val PRECISION_MINIMA_M = 150f        // metros de margen de error
    }
}
