package com.bo.asistenciaapp.data.repository

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Asistencia

/**
 * Responsabilidad: Gestionar todas las operaciones relacionadas con asistencias.
 * 
 * Este repositorio abstrae el acceso a datos de asistencias, permitiendo:
 * - Cambiar la fuente de datos sin afectar los casos de uso
 * - Centralizar la lógica de acceso a datos de asistencias
 * - Facilitar pruebas unitarias
 */
class AsistenciaRepository(private val database: AppDatabase) {
    
    /**
     * Obtiene todas las asistencias de un alumno.
     * 
     * @param alumnoId ID del alumno
     * @return Lista de asistencias del alumno con información de grupo y materia
     */
    fun obtenerPorAlumno(alumnoId: Int): List<Asistencia> {
        return database.asistenciaDao.obtenerPorAlumno(alumnoId)
    }
    
    /**
     * Registra una nueva asistencia para un alumno.
     * 
     * @param alumnoId ID del alumno
     * @param grupoId ID del grupo
     * @param fecha Fecha de la asistencia
     */
    fun registrar(alumnoId: Int, grupoId: Int, fecha: String) {
        database.asistenciaDao.insertar(alumnoId, grupoId, fecha)
    }
    
    /**
     * Verifica si un alumno puede marcar asistencia en un grupo.
     * Valida que sea el día y hora correcta según el horario del grupo.
     * 
     * @param alumnoId ID del alumno
     * @param grupoId ID del grupo
     * @return true si puede marcar asistencia, false en caso contrario
     */
    fun puedeMarcarAsistencia(alumnoId: Int, grupoId: Int): Boolean {
        return database.asistenciaDao.puedeMarcarAsistencia(alumnoId, grupoId)
    }
}

