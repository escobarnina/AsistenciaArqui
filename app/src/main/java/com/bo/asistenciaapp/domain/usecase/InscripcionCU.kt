package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.model.Boleta
import com.bo.asistenciaapp.domain.utils.Validators
import com.bo.asistenciaapp.domain.utils.ValidationResult
import com.bo.asistenciaapp.domain.utils.validate

/**
 * Caso de uso para gestionar inscripciones.
 * 
 * Orquesta la lógica de negocio relacionada con inscripciones,
 * utilizando el repositorio para acceder a los datos.
 * 
 * Incluye validaciones de negocio antes de realizar operaciones.
 */
class InscripcionCU(private val inscripcionRepository: InscripcionRepository) {
    
    /**
     * Obtiene todas las inscripciones de un alumno.
     */
    fun obtenerInscripciones(alumnoId: Int): List<Boleta> {
        if (!Validators.isPositive(alumnoId)) {
            return emptyList()
        }
        return inscripcionRepository.obtenerPorAlumno(alumnoId)
    }

    /**
     * Valida los datos de una inscripción antes de registrarla.
     */
    fun validarDatosInscripcion(
        alumnoId: Int,
        grupoId: Int,
        fecha: String,
        semestre: Int,
        gestion: Int
    ): ValidationResult {
        return validate(
            if (Validators.isPositive(alumnoId)) ValidationResult.Success
            else ValidationResult.Error("ID de alumno inválido"),
            
            if (Validators.isPositive(grupoId)) ValidationResult.Success
            else ValidationResult.Error("ID de grupo inválido"),
            
            if (Validators.isValidDateFormat(fecha)) ValidationResult.Success
            else ValidationResult.Error("Formato de fecha inválido (debe ser YYYY-MM-DD)"),
            
            if (Validators.isValidSemestre(semestre)) ValidationResult.Success
            else ValidationResult.Error("El semestre debe ser 1 o 2"),
            
            if (Validators.isValidGestion(gestion)) ValidationResult.Success
            else ValidationResult.Error("El año de gestión es inválido")
        )
    }

    /**
     * Registra una nueva inscripción para un alumno.
     * 
     * @return ValidationResult con el resultado de la operación
     */
    fun agregarInscripcion(
        alumnoId: Int,
        grupoId: Int,
        fecha: String,
        semestre: Int,
        gestion: Int
    ): ValidationResult {
        val validation = validarDatosInscripcion(alumnoId, grupoId, fecha, semestre, gestion)
        
        if (!validation.isValid) {
            return validation
        }
        
        // Validar cruce de horarios (regla de negocio importante)
        if (tieneCruceDeHorario(alumnoId, grupoId)) {
            return ValidationResult.Error("No se puede inscribir: hay cruce de horarios con otra materia")
        }
        
        inscripcionRepository.registrar(alumnoId, grupoId, fecha, semestre, gestion)
        return ValidationResult.Success
    }
    
    /**
     * Verifica si un alumno tiene cruce de horarios con un grupo.
     */
    fun tieneCruceDeHorario(alumnoId: Int, grupoId: Int): Boolean {
        if (!Validators.isPositive(alumnoId) || !Validators.isPositive(grupoId)) {
            return false
        }
        return inscripcionRepository.tieneCruceDeHorario(alumnoId, grupoId)
    }
}