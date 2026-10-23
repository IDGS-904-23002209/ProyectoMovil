package com.utl.nomi.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionStore(context: Context) {

    private val prefs: SharedPreferences? = try {
        val llave = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "nomi_sesion",
            llave,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        null
    }

    fun guardar(sesion: SesionUsuario) {
        prefs?.edit {
            putString("token", sesion.token)
            putString("expiracion", sesion.expiracion)
            putInt("id", sesion.id)
            putString("nombre", sesion.nombre)
            putString("rol", sesion.rol)
        }
    }

    fun leer(): SesionUsuario? {
        val p = prefs ?: return null
        val token = p.getString("token", null) ?: return null
        val expiracion = p.getString("expiracion", null) ?: return null
        val nombre = p.getString("nombre", null) ?: return null
        val rol = p.getString("rol", null) ?: return null
        return SesionUsuario(token, expiracion, p.getInt("id", 0), nombre, rol)
    }

    fun borrar() {
        prefs?.edit { clear() }
    }
}
