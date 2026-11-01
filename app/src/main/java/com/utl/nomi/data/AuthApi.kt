package com.utl.nomi.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// Llamadas a los endpoints de login de NomiBack.
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

    // El backend responde los errores como {"mensaje": "..."}
    private fun mensajeDe(texto: String, porDefecto: String): String {
        return try {
            JSONObject(texto).optString("mensaje", porDefecto).ifBlank { porDefecto }
        } catch (e: JSONException) {
            porDefecto
        }
    }

    // Lee un texto que puede venir null o vacío en el JSON
    private fun JSONObject.textoOpcional(clave: String): String? =
        if (isNull(clave)) null else optString(clave).ifBlank { null }

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

    // GET /api/auth/perfil : datos de la persona y del empleado del usuario con sesión
    suspend fun perfil(token: String): ResultadoApi<PerfilUsuario> =
        withContext(Dispatchers.IO) {
            try {
                val (codigo, texto) = ejecutar("/api/auth/perfil", "GET", token, null)
                if (codigo == 200) {
                    val json = JSONObject(texto)
                    ResultadoApi.Exito(
                        PerfilUsuario(
                            usuarioId = json.getInt("usuarioId"),
                            nombreUsuario = json.getString("nombreUsuario"),
                            rol = json.getString("rol"),
                            nombreCompleto = json.getString("nombreCompleto"),
                            correo = json.textoOpcional("correo"),
                            telefono = json.textoOpcional("telefono"),
                            empleadoId = if (json.isNull("empleadoId")) null else json.getInt("empleadoId"),
                            rfc = json.textoOpcional("rfc"),
                            curp = json.textoOpcional("curp"),
                            nss = json.textoOpcional("nss"),
                            fechaIngreso = json.textoOpcional("fechaIngreso"),
                            empresa = json.textoOpcional("empresa"),
                            departamento = json.textoOpcional("departamento"),
                            turno = json.textoOpcional("turno"),
                            sede = json.textoOpcional("sede")
                        )
                    )
                } else {
                    ResultadoApi.Error(mensajeDe(texto, "No se pudo cargar el perfil."), codigo)
                }
            } catch (e: IOException) {
                ResultadoApi.Error(MENSAJE_SIN_CONEXION)
            } catch (e: JSONException) {
                ResultadoApi.Error("Respuesta inesperada del servidor.")
            }
        }

    // POST /api/auth/cambiar-contrasena : si sale bien, el servidor cierra todas las sesiones del usuario
    suspend fun cambiarContrasena(token: String, actual: String, nueva: String): ResultadoApi<String> =
        withContext(Dispatchers.IO) {
            try {
                val cuerpo = JSONObject()
                    .put("contrasenaActual", actual)
                    .put("contrasenaNueva", nueva)
                    .toString()
                val (codigo, texto) = ejecutar("/api/auth/cambiar-contrasena", "POST", token, cuerpo)
                if (codigo == 200) {
                    ResultadoApi.Exito(mensajeDe(texto, "Contraseña actualizada."))
                } else {
                    ResultadoApi.Error(mensajeDe(texto, "No se pudo cambiar la contraseña."), codigo)
                }
            } catch (e: IOException) {
                ResultadoApi.Error(MENSAJE_SIN_CONEXION)
            } catch (e: JSONException) {
                ResultadoApi.Error("Respuesta inesperada del servidor.")
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
