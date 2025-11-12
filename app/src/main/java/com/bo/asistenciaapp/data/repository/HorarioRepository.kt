package com.bo.asistenciaapp.data.repository

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Horario

/**
 * Responsabilidad: Gestionar todas las operaciones relacionadas con horarios.
 * 
 * Este repositorio abstrae el acceso a datos de horarios, permitiendo:
 * - Cambiar la fuente de datos sin afectar los casos de uso
 * - Centralizar la lógica de acceso a datos de horarios
 * - Facilitar pruebas unitarias
 */
class HorarioRepository(private val database: AppDatabase) {
    
    /**
     * Obtiene todos los horarios del sistema con información de grupos.
     * 
     * @return Lista de horarios con información de materia y grupo
     */
    fun obtenerTodos(): List<Horario> {
        return database.horarioDao.obtenerTodos()
    }
    
    /**
     * Agrega un nuevo horario a un grupo.
     * 
     * @param grupoId ID del grupo
     * @param dia Día de la semana
     * @param horaInicio Hora de inicio (formato HH:mm)
     * @param horaFin Hora de fin (formato HH:mm)
     */
    fun agregar(grupoId: Int, dia: String, horaInicio: String, horaFin: String) {
        database.horarioDao.insertar(grupoId, dia, horaInicio, horaFin)
    }
    
    /**
     * Elimina un horario del sistema.
     * 
     * @param id ID del horario a eliminar
     */
    fun eliminar(id: Int) {
        database.horarioDao.eliminar(id)
    }
}

