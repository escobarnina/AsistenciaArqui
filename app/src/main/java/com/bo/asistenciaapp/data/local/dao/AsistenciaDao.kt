package com.bo.asistenciaapp.data.local.dao

import android.database.sqlite.SQLiteDatabase
import com.bo.asistenciaapp.domain.model.Asistencia

/**
 * Data Access Object para operaciones relacionadas con asistencias.
 * 
 * Responsabilidad: Gestionar todas las operaciones CRUD de la tabla asistencias.
 */
class AsistenciaDao(private val database: SQLiteDatabase) {
    
    /**
     * Obtiene todas las asistencias de un alumno.
     */
    fun obtenerPorAlumno(alumnoId: Int): List<Asistencia> {
        val lista = mutableListOf<Asistencia>()
        database.rawQuery(
            """
                SELECT 
                a.id, 
                a.alumno_id, 
                a.grupo_id, 
                a.fecha,
                g.grupo,
                g.materia_nombre
                FROM 
                asistencias a join grupos g 
                WHERE a.alumno_id=?
            """.trimIndent(),
            arrayOf(alumnoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Asistencia(
                        id = c.getInt(0),
                        alumnoId = c.getInt(1),
                        grupoId = c.getInt(2),
                        fecha = c.getString(3),
                        grupo = c.getString(4),
                        materiaNombre = c.getString(5)
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Registra una nueva asistencia.
     */
    fun insertar(alumnoId: Int, grupoId: Int, fecha: String) {
        database.execSQL(
            "INSERT INTO asistencias(alumno_id, grupo_id, fecha) VALUES (?,?,?)",
            arrayOf(alumnoId, grupoId, fecha)
        )
    }
    
    /**
     * Verifica si un alumno puede marcar asistencia en un grupo.
     * Valida que sea el día y hora correcta según el horario del grupo.
     */
    fun puedeMarcarAsistencia(alumnoId: Int, grupoId: Int): Boolean {
        val cal = java.util.Calendar.getInstance()
        val diaSemana = cal.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.LONG, java.util.Locale.getDefault())
        val horaActual = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(cal.time)

        database.rawQuery(
            """
        SELECT 1
        FROM horarios h
        INNER JOIN boletas b ON b.grupo_id = h.grupo_id
        WHERE b.alumno_id = ? AND h.grupo_id = ?
          AND lower(h.dia) = lower(?)
          AND h.hora_inicio <= ? AND h.hora_fin >= ?
        """.trimIndent(),
            arrayOf(alumnoId.toString(), grupoId.toString(), diaSemana, horaActual, horaActual)
        ).use { c ->
            return c.moveToFirst()
        }
    }
    
    /**
     * Obtiene todas las asistencias de un grupo específico.
     */
    fun obtenerPorGrupo(grupoId: Int): List<Asistencia> {
        val lista = mutableListOf<Asistencia>()
        database.rawQuery(
            """
                SELECT 
                a.id, 
                a.alumno_id, 
                a.grupo_id, 
                a.fecha,
                g.grupo,
                g.materia_nombre
                FROM 
                asistencias a 
                JOIN grupos g ON a.grupo_id = g.id
                WHERE a.grupo_id = ?
                ORDER BY a.fecha DESC
            """.trimIndent(),
            arrayOf(grupoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Asistencia(
                        id = c.getInt(0),
                        alumnoId = c.getInt(1),
                        grupoId = c.getInt(2),
                        fecha = c.getString(3),
                        grupo = c.getString(4),
                        materiaNombre = c.getString(5)
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Obtiene las asistencias de un estudiante en un grupo específico.
     */
    fun obtenerPorAlumnoYGrupo(alumnoId: Int, grupoId: Int): List<Asistencia> {
        val lista = mutableListOf<Asistencia>()
        database.rawQuery(
            """
                SELECT 
                a.id, 
                a.alumno_id, 
                a.grupo_id, 
                a.fecha,
                g.grupo,
                g.materia_nombre
                FROM 
                asistencias a 
                JOIN grupos g ON a.grupo_id = g.id
                WHERE a.alumno_id = ? AND a.grupo_id = ?
                ORDER BY a.fecha DESC
            """.trimIndent(),
            arrayOf(alumnoId.toString(), grupoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Asistencia(
                        id = c.getInt(0),
                        alumnoId = c.getInt(1),
                        grupoId = c.getInt(2),
                        fecha = c.getString(3),
                        grupo = c.getString(4),
                        materiaNombre = c.getString(5)
                    )
                )
            }
        }
        return lista
    }
}

