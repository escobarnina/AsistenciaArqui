package com.bo.asistenciaapp.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Responsabilidad: Gestionar la sesión del usuario autenticado.
 * 
 * Esta clase maneja el almacenamiento y recuperación de la información de sesión
 * del usuario utilizando SharedPreferences de Android.
 * 
 * Almacena:
 * - ID del usuario
 * - Nombre del usuario
 * - Rol del usuario (Admin, Docente, Alumno)
 * 
 * Uso típico:
 * ```kotlin
 * val userSession = UserSession(context)
 * userSession.saveUser(userId, userName, userRol)
 * val currentUserId = userSession.getUserId()
 * val isLoggedIn = userSession.isLoggedIn()
 * ```
 * 
 * Nota: Los datos se persisten localmente en SharedPreferences y se mantienen
 * entre sesiones de la aplicación hasta que se llame a `clear()` o se desinstale la app.
 */
class UserSession(context: Context) {
    
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    
    // Claves para SharedPreferences
    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_ROL = "user_rol"
        const val DEFAULT_USER_ID = -1
    }

    /**
     * Guarda los datos de sesión del usuario autenticado.
     * 
     * @param id ID único del usuario en la base de datos
     * @param nombre Nombre completo del usuario
     * @param rol Rol del usuario: "Admin", "Docente" o "Alumno"
     */
    fun saveUser(id: Int, nombre: String, rol: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, id)
            .putString(KEY_USER_NAME, nombre)
            .putString(KEY_USER_ROL, rol)
            .apply()
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     * 
     * @return ID del usuario, o -1 si no hay sesión activa
     */
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, DEFAULT_USER_ID)

    /**
     * Obtiene el nombre del usuario actualmente autenticado.
     * 
     * @return Nombre del usuario, o null si no hay sesión activa
     */
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    /**
     * Obtiene el rol del usuario actualmente autenticado.
     * 
     * @return Rol del usuario ("Admin", "Docente" o "Alumno"), o null si no hay sesión activa
     */
    fun getUserRol(): String? = prefs.getString(KEY_USER_ROL, null)

    /**
     * Verifica si hay una sesión de usuario activa.
     * 
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    fun isLoggedIn(): Boolean {
        val userId = getUserId()
        val userName = getUserName()
        val userRol = getUserRol()
        return userId != DEFAULT_USER_ID && userName != null && userRol != null
    }

    /**
     * Limpia todos los datos de sesión del usuario.
     * 
     * Se utiliza típicamente cuando el usuario cierra sesión.
     * Después de llamar a este método, `isLoggedIn()` retornará false.
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}