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
            "SELECT id, materia_id, materia_nombre, docente_id, docente_nombre, semestre, gestion, capacidad, nro_inscritos, grupo, tolerancia_minutos, tipo_estrategia FROM grupos",
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
                        grupo = c.getString(9),
                        toleranciaMinutos = c.getInt(10),
                        tipoEstrategia = c.getString(11)  // ⭐ NUEVO CAMPO
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
     * Obtiene todos los grupos asignados a un docente específico.
     */
    fun obtenerPorDocente(docenteId: Int): List<Grupo> {
        val lista = mutableListOf<Grupo>()
        database.rawQuery(
            "SELECT id, materia_id, materia_nombre, docente_id, docente_nombre, semestre, gestion, capacidad, nro_inscritos, grupo, tolerancia_minutos, tipo_estrategia FROM grupos WHERE docente_id=?",
            arrayOf(docenteId.toString())
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
                        grupo = c.getString(9),
                        toleranciaMinutos = c.getInt(10),
                        tipoEstrategia = c.getString(11)  // ⭐ NUEVO CAMPO
                    )
                )
            }
        }
        return lista
    }
    
    /**
     * Obtiene un grupo por su ID.
     * ⭐ PATRÓN STRATEGY: Usado para obtener la tolerancia del grupo
     */
    fun obtenerPorId(id: Int): Grupo? {
        database.rawQuery(
            "SELECT id, materia_id, materia_nombre, docente_id, docente_nombre, semestre, gestion, capacidad, nro_inscritos, grupo, tolerancia_minutos, tipo_estrategia FROM grupos WHERE id=?",
            arrayOf(id.toString())
        ).use { c ->
            return if (c.moveToFirst()) {
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
                    grupo = c.getString(9),
                    toleranciaMinutos = c.getInt(10),
                    tipoEstrategia = c.getString(11)  // ⭐ NUEVO CAMPO
                )
            } else {
                null
            }
        }
    }
    
    /**
     * Actualiza la tolerancia de un grupo específico.
     * 
     * ⭐ PATRÓN STRATEGY - Configuración Dinámica:
     * Este método permite modificar la tolerancia que utilizan las estrategias
     * de asistencia, haciendo el sistema configurable desde la UI.
     * 
     * @param id ID del grupo a actualizar
     * @param toleranciaMinutos Nueva tolerancia en minutos (0-60)
     */
    fun actualizarTolerancia(id: Int, toleranciaMinutos: Int) {
        database.execSQL(
            "UPDATE grupos SET tolerancia_minutos = ? WHERE id = ?",
            arrayOf(toleranciaMinutos, id)
        )
    }
    
    /**
     * Actualiza el tipo de estrategia de un grupo específico.
     * 
     * ⭐ PATRÓN STRATEGY - Configuración Dinámica:
     * Este método permite modificar qué estrategia utilizará el grupo
     * para calcular el estado de asistencia (PRESENTE, RETRASO, FALTA).
     * 
     * @param id ID del grupo a actualizar
     * @param tipoEstrategia Tipo de estrategia: "PRESENTE", "RETRASO" o "FALTA"
     */
    fun actualizarTipoEstrategia(id: Int, tipoEstrategia: String) {
        database.execSQL(
            "UPDATE grupos SET tipo_estrategia = ? WHERE id = ?",
            arrayOf(tipoEstrategia, id)
        )
    }
    
    /**
     * Verifica si existe un grupo con el ID especificado.
     * 
     * @param id ID del grupo a verificar
     * @return true si el grupo existe, false en caso contrario
     */
    fun existe(id: Int): Boolean {
        database.rawQuery(
            "SELECT COUNT(*) FROM grupos WHERE id = ?",
            arrayOf(id.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getInt(0) > 0
            } else {
                false
            }
        }
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

