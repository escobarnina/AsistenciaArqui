package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.model.Boleta

/**
 * Caso de uso para gestionar inscripciones.
 * 
 * Orquesta la lógica de negocio relacionada con inscripciones,
 * utilizando el repositorio para acceder a los datos.
 */
class InscripcionCU(private val inscripcionRepository: InscripcionRepository) {
    
    /**
     * Obtiene todas las inscripciones de un alumno.
     */
    fun obtenerInscripciones(alumnoId: Int): List<Boleta> {
        return inscripcionRepository.obtenerPorAlumno(alumnoId)
    }

    /**
     * Registra una nueva inscripción para un alumno.
     */
    fun agregarInscripcion(
        alumnoId: Int,
        grupoId: Int,
        fecha: String,
        semestre: Int,
        gestion: Int
    ) {
        inscripcionRepository.registrar(alumnoId, grupoId, fecha, semestre, gestion)
    }
    
    /**
     * Verifica si un alumno tiene cruce de horarios con un grupo.
     */
    fun tieneCruceDeHorario(alumnoId: Int, grupoId: Int): Boolean {
        return inscripcionRepository.tieneCruceDeHorario(alumnoId, grupoId)
    }
}