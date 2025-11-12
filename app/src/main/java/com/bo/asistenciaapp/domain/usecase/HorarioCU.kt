package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.HorarioRepository
import com.bo.asistenciaapp.domain.model.Horario
import com.bo.asistenciaapp.domain.utils.Validators
import com.bo.asistenciaapp.domain.utils.ValidationResult
import com.bo.asistenciaapp.domain.utils.validate

/**
 * Caso de uso para gestionar horarios.
 * 
 * Orquesta la lógica de negocio relacionada con horarios,
 * utilizando el repositorio para acceder a los datos.
 * 
 * Incluye validaciones de negocio antes de realizar operaciones.
 */
class HorarioCU(private val horarioRepository: HorarioRepository) {
    
    /**
     * Obtiene todos los horarios del sistema.
     */
    fun obtenerHorarios(): List<Horario> {
        return horarioRepository.obtenerTodos()
    }

    /**
     * Valida los datos de un horario antes de agregarlo.
     */
    fun validarDatosHorario(
        grupoId: Int,
        dia: String,
        horaInicio: String,
        horaFin: String
    ): ValidationResult {
        return validate(
            if (Validators.isPositive(grupoId)) ValidationResult.Success
            else ValidationResult.Error("ID de grupo inválido"),
            
            if (Validators.isValidDiaSemana(dia)) ValidationResult.Success
            else ValidationResult.Error("Día de la semana inválido"),
            
            if (Validators.isValidTimeFormat(horaInicio)) ValidationResult.Success
            else ValidationResult.Error("Formato de hora de inicio inválido (debe ser HH:mm)"),
            
            if (Validators.isValidTimeFormat(horaFin)) ValidationResult.Success
            else ValidationResult.Error("Formato de hora de fin inválido (debe ser HH:mm)"),
            
            if (Validators.isValidTimeRange(horaInicio, horaFin)) ValidationResult.Success
            else ValidationResult.Error("La hora de inicio debe ser anterior a la hora de fin")
        )
    }

    /**
     * Agrega un nuevo horario a un grupo.
     * 
     * @return ValidationResult con el resultado de la operación
     */
    fun agregarHorario(
        grupoId: Int,
        dia: String,
        horaInicio: String,
        horaFin: String
    ): ValidationResult {
        val validation = validarDatosHorario(grupoId, dia, horaInicio, horaFin)
        
        if (!validation.isValid) {
            return validation
        }
        
        horarioRepository.agregar(grupoId, dia, horaInicio, horaFin)
        return ValidationResult.Success
    }
    
    /**
     * Elimina un horario del sistema.
     * 
     * @param id ID del horario a eliminar
     * @return ValidationResult con el resultado de la operación
     */
    fun eliminarHorario(id: Int): ValidationResult {
        if (!Validators.isPositive(id)) {
            return ValidationResult.Error("ID de horario inválido")
        }
        
        // Verificar que el horario existe
        val horario = horarioRepository.obtenerTodos().find { it.id == id }
        if (horario == null) {
            return ValidationResult.Error("El horario no existe")
        }
        
        horarioRepository.eliminar(id)
        return ValidationResult.Success
    }
}