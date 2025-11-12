package com.bo.asistenciaapp.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.bo.asistenciaapp.domain.model.Grupo

/**
 * Data Access Object para operaciones relacionadas con grupos.
 * 
 * Responsabilidad: Gestionar todas las operaciones CRUD de la tabla grupos.
 */
class GrupoDao(private val database: SQLiteDatabase) {
    
    /**
     * Obtiene todos los grupos.
     */
    fun obtenerTodos(): List<Grupo> {
        val lista = mutableListOf<Grupo>()
        database.rawQuery(
            "SELECT id, materia_id, materia_nombre, docente_id, docente_nombre, semestre, gestion, capacidad, nro_inscritos, grupo FROM grupos",
            null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Grupo(
                        id = c.getInt(0),
                        materiaId = c.getInt(1),
                        materiaNombre = c.getString(2),
                        docenteId = c.getInt(3),
                        docenteNombre = c.getString(4),
                        semestre = c.getInt(5),
                        gestion = c.getInt(6),
                        capacidad = c.getInt(7),
                        nroInscritos = c.getInt(8),
                        grupo = c.getString(9)
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Inserta un nuevo grupo.
     */
    fun insertar(
        materiaId: Int,
        materiaNombre: String,
        docenteId: Int,
        docenteNombre: String,
        semestre: Int,
        gestion: Int,
        capacidad: Int,
        grupo: String
    ) {
        database.execSQL(
            "INSERT INTO grupos(materia_id, materia_nombre, docente_id, docente_nombre, semestre, gestion, capacidad, grupo) VALUES (?,?,?,?,?,?,?,?)",
            arrayOf(
                materiaId,
                materiaNombre,
                docenteId,
                docenteNombre,
                semestre,
                gestion,
                capacidad,
                grupo
            )
        )
    }
    
    /**
     * Elimina un grupo por ID.
     */
    fun eliminar(id: Int) {
        database.execSQL(
            "DELETE FROM grupos WHERE id=?", arrayOf(id)
        )
    }
}

