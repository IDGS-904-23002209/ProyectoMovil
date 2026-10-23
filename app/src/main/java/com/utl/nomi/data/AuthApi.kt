package com.utl.nomi.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object AuthApi {

    private const val MENSAJE_SIN_CONEXION = "No se pudo conectar con el servidor. Revisa tu conexión."

    // Hace la petición HTTP y devuelve (código HTTP, texto de la respuesta)
    private fun ejecutar(ruta: String, metodo: String, token: String?, cuerpo: String?): Pair<Int, String> {
        val conexion = URL(ApiConfig.BASE_URL + ruta).openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = metodo
            conexion.connectTimeout = 10_000
            conexion.readTimeout = 10_000
            conexion.setRequestProperty("Content-Type", "application/json")
            conexion.setRequestProperty("Accept", "application/json")
            if (token != null) {
                conexion.setRequestProperty("Authorization", "Bearer $token")
            }
            if (metodo == "POST") {
                conexion.doOutput = true
                conexion.outputStream.use { it.write((cuerpo ?: "").toByteArray(Charsets.UTF_8)) }
            }
            val codigo = conexion.responseCode
            val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
            val texto = flujo?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            return Pair(codigo, texto)
        } finally {
            conexion.disconnect()
        }
    }

    private fun mensajeDe(texto: String, porDefecto: String): String {
        return try {
            JSONObject(texto).optString("mensaje", porDefecto).ifBlank { porDefecto }
        } catch (e: JSONException) {
            porDefecto
        }
    }

    // POST /api/auth/login
    suspend fun login(usuario: String, contrasena: String): ResultadoApi<SesionUsuario> =
        withContext(Dispatchers.IO) {
            try {
                val cuerpo = JSONObject()
                    .put("usuario", usuario)
                    .put("contrasena", contrasena)
                    .toString()
                val (codigo, texto) = ejecutar("/api/auth/login", "POST", null, cuerpo)

                if (codigo == 200) {
                    val json = JSONObject(texto)
                    val u = json.getJSONObject("usuario")
                    ResultadoApi.Exito(
                        SesionUsuario(
                            token = json.getString("token"),
                            expiracion = json.getString("expiracion"),
                            id = u.getInt("id"),
                            nombre = u.getString("nombre"),
                            rol = u.getString("rol")
                        )
                    )
                } else {
                    ResultadoApi.Error(mensajeDe(texto, "No se pudo iniciar sesión."), codigo)
                }
            } catch (e: IOException) {
                ResultadoApi.Error(MENSAJE_SIN_CONEXION)
            } catch (e: JSONException) {
                ResultadoApi.Error("Respuesta inesperada del servidor.")
            }
        }

    // GET /api/auth/me : comprueba que el token guardado sigue vigente
    suspend fun me(token: String): ResultadoApi<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val (codigo, texto) = ejecutar("/api/auth/me", "GET", token, null)
                if (codigo == 200) {
                    ResultadoApi.Exito(Unit)
                } else {
                    ResultadoApi.Error(mensajeDe(texto, "Sesión no válida."), codigo)
                }
            } catch (e: IOException) {
                ResultadoApi.Error(MENSAJE_SIN_CONEXION)
            }
        }

    // POST /api/auth/logout : cierra la sesión en el servidor (si falla, no pasa nada)
    suspend fun logout(token: String) {
        withContext(Dispatchers.IO) {
            try {
                ejecutar("/api/auth/logout", "POST", token, null)
            } catch (e: IOException) {
                // sin conexión: la sesión local ya se cerró
            }
        }
    }
}
