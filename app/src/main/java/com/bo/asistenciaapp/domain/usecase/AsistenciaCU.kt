package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.domain.model.Asistencia

/**
 * Caso de uso para gestionar asistencias.
 * 
 * Orquesta la lógica de negocio relacionada con asistencias,
 * utilizando el repositorio para acceder a los datos.
 */
class AsistenciaCU(private val asistenciaRepository: AsistenciaRepository) {
    
    /**
     * Obtiene todas las asistencias de un alumno.
     */
    fun obtenerAsistencias(alumnoId: Int): List<Asistencia> {
        return asistenciaRepository.obtenerPorAlumno(alumnoId)
    }

    /**
     * Registra una nueva asistencia para un alumno.
     */
    fun marcarAsistencia(alumnoId: Int, grupoId: Int, fecha: String) {
        asistenciaRepository.registrar(alumnoId, grupoId, fecha)
    }

    /**
     * Verifica si un alumno puede marcar asistencia en un grupo.
     */
    fun puedeMarcarAsistencia(alumnoId: Int, grupoId: Int): Boolean {
        return asistenciaRepository.puedeMarcarAsistencia(alumnoId, grupoId)
    }
}