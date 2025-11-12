package com.bo.asistenciaapp.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.bo.asistenciaapp.domain.model.Horario

/**
 * Data Access Object para operaciones relacionadas con horarios.
 * 
 * Responsabilidad: Gestionar todas las operaciones CRUD de la tabla horarios.
 */
class HorarioDao(private val database: SQLiteDatabase) {
    
    /**
     * Obtiene todos los horarios con información de grupos.
     */
    fun obtenerTodos(): List<Horario> {
        val lista = mutableListOf<Horario>()
        database.rawQuery(
            """
                SELECT 
                h.id, 
                h.grupo_id, 
                h.dia, 
                h.hora_inicio, 
                h.hora_fin, 
                g.materia_nombre,
                g.grupo
                FROM 
                horarios h join grupos g on h.grupo_id = g.id 
                ORDER BY g.id DESC
            """.trimIndent(), null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Horario(
                        id = c.getInt(0),
                        grupoId = c.getInt(1),
                        dia = c.getString(2),
                        horaInicio = c.getString(3),
                        horaFin = c.getString(4),
                        materia = c.getString(5),
                        grupo = c.getString(6),
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Inserta un nuevo horario.
     */
    fun insertar(grupoId: Int, dia: String, horaInicio: String, horaFin: String) {
        database.execSQL(
            "INSERT INTO horarios(grupo_id, dia, hora_inicio, hora_fin) VALUES (?,?,?,?)",
            arrayOf(grupoId, dia, horaInicio, horaFin)
        )
    }
    
    /**
     * Elimina un horario por ID.
     */
    fun eliminar(id: Int) {
        database.execSQL(
            "DELETE FROM horarios WHERE id=?", arrayOf(id)
        )
    }
}

