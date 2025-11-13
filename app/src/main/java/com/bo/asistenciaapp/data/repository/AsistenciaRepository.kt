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
    
    /**
     * Obtiene todas las asistencias de un grupo específico.
     * 
     * @param grupoId ID del grupo
     * @return Lista de asistencias del grupo
     */
    fun obtenerPorGrupo(grupoId: Int): List<Asistencia> {
        return database.asistenciaDao.obtenerPorGrupo(grupoId)
    }
    
    /**
     * Obtiene las asistencias de un estudiante en un grupo específico.
     * 
     * @param alumnoId ID del alumno
     * @param grupoId ID del grupo
     * @return Lista de asistencias del alumno en el grupo
     */
    fun obtenerPorAlumnoYGrupo(alumnoId: Int, grupoId: Int): List<Asistencia> {
        return database.asistenciaDao.obtenerPorAlumnoYGrupo(alumnoId, grupoId)
    }
    
    /**
     * Obtiene la tolerancia en minutos configurada para un grupo.
     * 
     * ⭐ PATRÓN STRATEGY CON DATOS DE BD:
     * Este método permite obtener la tolerancia desde la tabla grupos,
     * haciendo que el patrón Strategy sea configurable por datos.
     * 
     * @param grupoId ID del grupo
     * @return Tolerancia en minutos (por defecto 10 si el grupo no existe)
     */
    fun obtenerToleranciaGrupo(grupoId: Int): Int {
        val grupo = database.grupoDao.obtenerPorId(grupoId)
        return grupo?.toleranciaMinutos ?: 10  // Valor por defecto si no existe el grupo
    }
}

