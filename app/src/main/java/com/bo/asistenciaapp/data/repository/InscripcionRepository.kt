package com.bo.asistenciaapp.data.repository

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Boleta

/**
 * Responsabilidad: Gestionar todas las operaciones relacionadas con inscripciones (boletas).
 * 
 * Este repositorio abstrae el acceso a datos de inscripciones, permitiendo:
 * - Cambiar la fuente de datos sin afectar los casos de uso
 * - Centralizar la lógica de acceso a datos de inscripciones
 * - Facilitar pruebas unitarias
 */
class InscripcionRepository(private val database: AppDatabase) {
    
    /**
     * Obtiene todas las inscripciones (boletas) de un alumno.
     * 
     * @param alumnoId ID del alumno
     * @return Lista de boletas del alumno con información de grupo y horario
     */
    fun obtenerPorAlumno(alumnoId: Int): List<Boleta> {
        return database.inscripcionDao.obtenerPorAlumno(alumnoId)
    }
    
    /**
     * Registra una nueva inscripción (boleta) para un alumno.
     * 
     * @param alumnoId ID del alumno
     * @param grupoId ID del grupo
     * @param fecha Fecha de inscripción
     * @param semestre Semestre (1 o 2)
     * @param gestion Año de gestión
     */
    fun registrar(alumnoId: Int, grupoId: Int, fecha: String, semestre: Int, gestion: Int) {
        database.inscripcionDao.insertar(alumnoId, grupoId, fecha, semestre, gestion)
    }
    
    /**
     * Verifica si un alumno tiene cruce de horarios con un grupo.
     * 
     * @param alumnoId ID del alumno
     * @param grupoId ID del grupo a verificar
     * @return true si hay cruce de horarios, false en caso contrario
     */
    fun tieneCruceDeHorario(alumnoId: Int, grupoId: Int): Boolean {
        return database.inscripcionDao.tieneCruceDeHorario(alumnoId, grupoId)
    }
    
    /**
     * Obtiene todos los estudiantes inscritos en un grupo específico.
     * 
     * @param grupoId ID del grupo
     * @return Lista de estudiantes inscritos en el grupo
     */
    fun obtenerEstudiantesPorGrupo(grupoId: Int): List<com.bo.asistenciaapp.domain.model.Usuario> {
        return database.inscripcionDao.obtenerEstudiantesPorGrupo(grupoId)
    }
}

