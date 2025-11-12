package com.bo.asistenciaapp.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.bo.asistenciaapp.domain.model.Materia

/**
 * Data Access Object para operaciones relacionadas con materias.
 * 
 * Responsabilidad: Gestionar todas las operaciones CRUD de la tabla materias.
 */
class MateriaDao(private val database: SQLiteDatabase) {
    
    /**
     * Obtiene todas las materias.
     */
    fun obtenerTodas(): List<Materia> {
        val lista = mutableListOf<Materia>()
        database.rawQuery(
            "SELECT id, nombre, sigla, nivel FROM materias ORDER BY id DESC", null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Materia(
                        id = c.getInt(0),
                        nombre = c.getString(1),
                        sigla = c.getString(2),
                        nivel = c.getInt(3)
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Inserta una nueva materia.
     */
    fun insertar(nombre: String, sigla: String, nivel: Int) {
        database.execSQL(
            "INSERT INTO materias(nombre, sigla, nivel) VALUES (?,?,?)",
            arrayOf(nombre, sigla, nivel)
        )
    }
    
    /**
     * Elimina una materia por ID.
     */
    fun eliminar(id: Int) {
        database.execSQL(
            "DELETE FROM materias where id=?", arrayOf(id)
        )
    }
}

