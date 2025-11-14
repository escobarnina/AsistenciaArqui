package com.bo.asistenciaapp.data.repository

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.model.Horario

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
    
    /**
     * Obtiene el tipo de estrategia configurado para un grupo.
     * 
     * ⭐ PATRÓN STRATEGY CON DATOS DE BD:
     * Este método permite obtener qué estrategia debe usar el grupo
     * para calcular el estado de asistencia.
     * 
     * @param grupoId ID del grupo
     * @return Tipo de estrategia: "PRESENTE", "RETRASO" o "FALTA" (por defecto "RETRASO")
     */
    fun obtenerTipoEstrategiaGrupo(grupoId: Int): String {
        val grupo = database.grupoDao.obtenerPorId(grupoId)
        return grupo?.tipoEstrategia ?: "RETRASO"  // Valor por defecto si no existe el grupo
    }
    
    /**
     * Obtiene la hora de inicio de un grupo para el día actual.
     * 
     * @param grupoId ID del grupo
     * @return Hora de inicio en formato HH:mm, o "08:00" por defecto si no hay horario
     */
    fun obtenerHoraInicioGrupo(grupoId: Int): String {
        val horarios = database.horarioDao.obtenerPorGrupo(grupoId)
        if (horarios.isEmpty()) {
            return "08:00"  // Hora por defecto
        }
        // Obtener el día actual
        val diaActual = obtenerDiaActual()
        // Buscar horario para el día actual
        val horarioHoy = horarios.find { it.dia.equals(diaActual, ignoreCase = true) }
        return horarioHoy?.horaInicio ?: horarios.first().horaInicio  // Usar el primer horario si no hay para hoy
    }
    
    /**
     * Obtiene el nombre del día actual en español.
     */
    private fun obtenerDiaActual(): String {
        val dias = arrayOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        val calendario = java.util.Calendar.getInstance()
        val diaSemana = calendario.get(java.util.Calendar.DAY_OF_WEEK)
        return dias[diaSemana - 1]
    }
    
    /**
     * Verifica si un alumno está inscrito en un grupo.
     * 
     * @param alumnoId ID del alumno
     * @param grupoId ID del grupo
     * @return true si está inscrito, false en caso contrario
     */
    fun estaInscrito(alumnoId: Int, grupoId: Int): Boolean {
        return database.asistenciaDao.estaInscrito(alumnoId, grupoId)
    }
    
    /**
     * Obtiene los horarios de un grupo específico.
     * 
     * @param grupoId ID del grupo
     * @return Lista de horarios del grupo
     */
    fun obtenerHorariosGrupo(grupoId: Int): List<Horario> {
        return database.horarioDao.obtenerPorGrupo(grupoId)
    }
}

