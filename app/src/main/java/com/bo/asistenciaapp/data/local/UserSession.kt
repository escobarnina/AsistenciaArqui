package com.bo.asistenciaapp.data.local

import android.content.Context
import android.content.SharedPreferences

class UserSession(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    fun saveUser(id: Int, nombre: String, rol: String) {
        prefs.edit()
            .putInt("user_id", id)
            .putString("user_name", nombre)
            .putString("user_rol", rol)
            .apply()
    }

    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun getUserName(): String? = prefs.getString("user_name", null)
    fun getUserRol(): String? = prefs.getString("user_name", null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}