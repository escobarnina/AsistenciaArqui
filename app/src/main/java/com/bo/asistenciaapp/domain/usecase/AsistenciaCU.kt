package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.utils.Validators
import com.bo.asistenciaapp.domain.utils.ValidationResult
import com.bo.asistenciaapp.domain.utils.validate

/**
 * Caso de uso para gestionar asistencias.
 * 
 * Orquesta la lógica de negocio relacionada con asistencias,
 * utilizando el repositorio para acceder a los datos.
 * 
 * Incluye validaciones de negocio antes de realizar operaciones.
 */
class AsistenciaCU(private val asistenciaRepository: AsistenciaRepository) {
    
    /**
     * Obtiene todas las asistencias de un alumno.
     */
    fun obtenerAsistencias(alumnoId: Int): List<Asistencia> {
        if (!Validators.isPositive(alumnoId)) {
            return emptyList()
        }
        return asistenciaRepository.obtenerPorAlumno(alumnoId)
    }

    /**
     * Valida los datos de una asistencia antes de registrarla.
     */
    fun validarDatosAsistencia(
        alumnoId: Int,
        grupoId: Int,
        fecha: String
    ): ValidationResult {
        return validate(
            if (Validators.isPositive(alumnoId)) ValidationResult.Success
            else ValidationResult.Error("ID de alumno inválido"),
            
            if (Validators.isPositive(grupoId)) ValidationResult.Success
            else ValidationResult.Error("ID de grupo inválido"),
            
            if (Validators.isValidDateFormat(fecha)) ValidationResult.Success
            else ValidationResult.Error("Formato de fecha inválido (debe ser YYYY-MM-DD)")
        )
    }

    /**
     * Registra una nueva asistencia para un alumno.
     * 
     * @return ValidationResult con el resultado de la operación
     */
    fun marcarAsistencia(alumnoId: Int, grupoId: Int, fecha: String): ValidationResult {
        val validation = validarDatosAsistencia(alumnoId, grupoId, fecha)
        
        if (!validation.isValid) {
            return validation
        }
        
        // Validar que el alumno puede marcar asistencia (regla de negocio)
        if (!puedeMarcarAsistencia(alumnoId, grupoId)) {
            return ValidationResult.Error("No se puede marcar asistencia: no es el día/hora correcta o no está inscrito")
        }
        
        asistenciaRepository.registrar(alumnoId, grupoId, fecha)
        return ValidationResult.Success
    }

    /**
     * Verifica si un alumno puede marcar asistencia en un grupo.
     * Valida que sea el día y hora correcta según el horario del grupo.
     */
    fun puedeMarcarAsistencia(alumnoId: Int, grupoId: Int): Boolean {
        if (!Validators.isPositive(alumnoId) || !Validators.isPositive(grupoId)) {
            return false
        }
        return asistenciaRepository.puedeMarcarAsistencia(alumnoId, grupoId)
    }
}