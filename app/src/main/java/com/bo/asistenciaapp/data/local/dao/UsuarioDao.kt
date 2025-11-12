package com.bo.asistenciaapp.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.bo.asistenciaapp.domain.model.Usuario

/**
 * Data Access Object para operaciones relacionadas con usuarios.
 * 
 * Responsabilidad: Gestionar todas las operaciones CRUD de la tabla usuarios.
 */
class UsuarioDao(private val database: SQLiteDatabase) {
    
    /**
     * Valida las credenciales de un usuario.
     */
    fun validarUsuario(username: String, contrasena: String): Usuario? {
        database.rawQuery(
            "SELECT * FROM usuarios WHERE username=? AND contrasena=?",
            arrayOf(username, contrasena)
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                Usuario(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nombres = cursor.getString(cursor.getColumnIndexOrThrow("nombres")),
                    apellidos = cursor.getString(cursor.getColumnIndexOrThrow("apellidos")),
                    registro = cursor.getString(cursor.getColumnIndexOrThrow("registro")),
                    rol = cursor.getString(cursor.getColumnIndexOrThrow("rol")),
                    username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                )
            } else {
                null
            }
        }
    }
    
    /**
     * Obtiene todos los usuarios.
     */
    fun obtenerTodos(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        database.rawQuery(
            "SELECT id, nombres, apellidos, registro, rol, username FROM usuarios ORDER BY id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Usuario(
                        id = c.getInt(0),
                        nombres = c.getString(1),
                        apellidos = c.getString(2),
                        registro = c.getString(3),
                        rol = c.getString(4),
                        username = c.getString(5),
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Obtiene solo los usuarios con rol de docente.
     */
    fun obtenerDocentes(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        database.rawQuery(
            "SELECT id, nombres, apellidos, registro, rol, username FROM usuarios WHERE rol LIKE '%docente%' ORDER BY id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Usuario(
                        id = c.getInt(0),
                        nombres = c.getString(1),
                        apellidos = c.getString(2),
                        registro = c.getString(3),
                        rol = c.getString(4),
                        username = c.getString(5),
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Inserta un nuevo usuario.
     */
    fun insertar(nombres: String, apellidos: String, registro: String, rol: String, username: String, contrasena: String) {
        database.execSQL(
            "INSERT INTO usuarios(nombres, apellidos, registro, rol, username, contrasena) VALUES (?,?,?,?,?,?)",
            arrayOf(nombres, apellidos, registro, rol, username, contrasena)
        )
    }
    
    /**
     * Elimina un usuario por ID.
     */
    fun eliminar(id: Int) {
        database.execSQL("DELETE FROM usuarios where id=?", arrayOf(id))
    }
    
    /**
     * Actualiza los datos de un usuario.
     */
    fun actualizar(id: Int, nombres: String, apellidos: String, rol: String) {
        database.execSQL(
            "UPDATE usuarios SET nombres=?, apellidos=?, rol=? WHERE id=?",
            arrayOf(nombres, apellidos, rol, id)
        )
    }
}

