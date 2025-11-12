package com.bo.asistenciaapp.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.bo.asistenciaapp.data.local.StringRange
import com.bo.asistenciaapp.domain.model.Boleta

/**
 * Data Access Object para operaciones relacionadas con inscripciones (boletas).
 * 
 * Responsabilidad: Gestionar todas las operaciones CRUD de la tabla boletas.
 */
class InscripcionDao(private val database: SQLiteDatabase) {
    
    /**
     * Obtiene todas las inscripciones (boletas) de un alumno.
     */
    fun obtenerPorAlumno(alumnoId: Int): List<Boleta> {
        val lista = mutableListOf<Boleta>()
        database.rawQuery(
            """
                SELECT 
                b.id, 
                b.alumno_id, 
                b.grupo_id, 
                b.fecha, 
                b.semestre, 
                b.gestion,
                g.grupo,
                g.materia_nombre,
                h.dia,
                h.hora_inicio,
                h.hora_fin
                FROM boletas b join grupos g on b.grupo_id = g.id
                join horarios h on h.grupo_id = g.id
                WHERE b.alumno_id=?
            """.trimIndent(),
            arrayOf(alumnoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Boleta(
                        id = c.getInt(0),
                        alumnoId = c.getInt(1),
                        grupoId = c.getInt(2),
                        fecha = c.getString(3),
                        semestre = c.getInt(4),
                        gestion = c.getInt(5),
                        grupo = c.getString(6),
                        materiaNombre = c.getString(7),
                        dia = c.getString(8),
                        horario = c.getString(8) + " - " + c.getString(9)
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Registra una nueva inscripción (boleta).
     */
    fun insertar(alumnoId: Int, grupoId: Int, fecha: String, semestre: Int, gestion: Int) {
        database.execSQL(
            "INSERT INTO boletas(alumno_id, grupo_id, fecha, semestre, gestion) VALUES (?,?,?,?,?)",
            arrayOf(alumnoId, grupoId, fecha, semestre, gestion)
        )
    }
    
    /**
     * Verifica si un alumno tiene cruce de horarios con un grupo.
     */
    fun tieneCruceDeHorario(alumnoId: Int, grupoId: Int): Boolean {
        // Horarios del grupo a inscribir
        val horariosNuevo = mutableListOf<Pair<String, StringRange>>()
        database.rawQuery(
            "SELECT dia, hora_inicio, hora_fin FROM horarios WHERE grupo_id=?",
            arrayOf(grupoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                horariosNuevo.add(
                    Pair(
                        c.getString(0),
                        StringRange(c.getString(1), c.getString(2))
                    )
                )
            }
        }

        // Horarios de grupos ya inscritos
        val horariosPrevios = mutableListOf<Pair<String, StringRange>>()
        database.rawQuery(
            """
                SELECT h.dia, h.hora_inicio, h.hora_fin
                FROM horarios h
                INNER JOIN boletas b ON b.grupo_id = h.grupo_id
                WHERE b.alumno_id = ?
            """, arrayOf(alumnoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                horariosPrevios.add(
                    Pair(
                        c.getString(0),
                        StringRange(c.getString(1), c.getString(2))
                    )
                )
            }
        }

        // Comparar
        for (nuevo in horariosNuevo) {
            for (prev in horariosPrevios) {
                if (nuevo.first == prev.first &&
                    nuevo.second.overlaps(prev.second)
                ) return true
            }
        }
        return false
    }
}

