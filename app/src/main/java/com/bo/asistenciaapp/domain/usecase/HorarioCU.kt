package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.HorarioRepository
import com.bo.asistenciaapp.domain.model.Horario

/**
 * Caso de uso para gestionar horarios.
 * 
 * Orquesta la lógica de negocio relacionada con horarios,
 * utilizando el repositorio para acceder a los datos.
 */
class HorarioCU(private val horarioRepository: HorarioRepository) {
    
    /**
     * Obtiene todos los horarios del sistema.
     */
    fun obtenerHorarios(): List<Horario> {
        return horarioRepository.obtenerTodos()
    }

    /**
     * Agrega un nuevo horario a un grupo.
     */
    fun agregarHorario(
        grupoId: Int,
        dia: String,
        horaInicio: String,
        horaFin: String
    ) {
        horarioRepository.agregar(grupoId, dia, horaInicio, horaFin)
    }
    
    /**
     * Elimina un horario del sistema.
     */
    fun eliminarHorario(id: Int) {
        horarioRepository.eliminar(id)
    }
}