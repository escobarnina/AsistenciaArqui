package com.bo.asistenciaapp.domain.usecase

import android.util.Log
import com.bo.asistenciaapp.data.repository.HorarioRepository
import com.bo.asistenciaapp.domain.utils.ValidationResult

/**
 * Caso de uso para configurar horarios de clase de un grupo.
 * 
 * ## PATRÓN STRATEGY - Configuración de Horarios:
 * Este Use Case permite configurar horarios de clase para que el sistema
 * pueda validar cuándo un alumno puede marcar asistencia y aplicar
 * la estrategia de tolerancia correspondiente.
 * 
 * ## Responsabilidades (CAPA DOMINIO):
 * - Validar datos de horario (día, hora inicio, hora fin)
 * - Verificar que el grupo existe
 * - Coordinar actualización/creación de horarios
 * - Proporcionar feedback de la operación
 * 
 * @property horarioRepository Repositorio para acceder a datos de horarios
 */
class ConfigurarHorarioCU(private val horarioRepository: HorarioRepository) {
    
    companion object {
        private const val TAG = "ConfigurarHorarioCU"
        
        // Días válidos de la semana
        val DIAS_VALIDOS = listOf(
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
        )
    }
    
    /**
     * Configura el horario de un grupo específico.
     * 
     * ## Validaciones:
     * - Día debe ser válido (Lunes-Sábado)
     * - Formato de hora válido (HH:mm)
     * - Hora fin debe ser mayor que hora inicio
     * - Grupo debe existir
     * 
     * @param grupoId ID del grupo
     * @param dia Día de la semana
     * @param horaInicio Hora de inicio (HH:mm)
     * @param horaFin Hora de fin (HH:mm)
     * @return ValidationResult con el resultado
     */
    fun configurarHorario(
        grupoId: Int,
        dia: String,
        horaInicio: String,
        horaFin: String
    ): ValidationResult {
        Log.d(TAG, "=== CONFIGURANDO HORARIO ===")
        Log.d(TAG, "Grupo: $grupoId, Día: $dia, Inicio: $horaInicio, Fin: $horaFin")
        
        // Validación 1: ID del grupo
        if (grupoId <= 0) {
            val mensaje = "ID de grupo inválido: $grupoId"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Validación 2: Día válido
        if (dia !in DIAS_VALIDOS) {
            val mensaje = "Día inválido: $dia. Debe ser uno de: ${DIAS_VALIDOS.joinToString()}"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Validación 3: Formato de horas
        if (!esHoraValida(horaInicio)) {
            val mensaje = "Hora de inicio inválida: $horaInicio. Formato debe ser HH:mm"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        if (!esHoraValida(horaFin)) {
            val mensaje = "Hora de fin inválida: $horaFin. Formato debe ser HH:mm"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Validación 4: Hora fin > hora inicio
        if (!esHoraFinMayor(horaInicio, horaFin)) {
            val mensaje = "Hora de fin ($horaFin) debe ser mayor que hora de inicio ($horaInicio)"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Crear horario
        return try {
            horarioRepository.agregar(grupoId, dia, horaInicio, horaFin)
            Log.d(TAG, "✅ Horario configurado exitosamente")
            ValidationResult.Success
        } catch (e: Exception) {
            val mensaje = "Error al configurar horario: ${e.message}"
            Log.e(TAG, mensaje, e)
            ValidationResult.Error(mensaje)
        }
    }
    
    /**
     * Obtiene los horarios actuales de un grupo.
     * 
     * @param grupoId ID del grupo
     * @return Lista de horarios
     */
    fun obtenerHorariosGrupo(grupoId: Int): List<String> {
        if (grupoId <= 0) return emptyList()
        
        return try {
            val horarios = horarioRepository.obtenerPorGrupo(grupoId)
            horarios.map { "${it.dia} ${it.horaInicio}-${it.horaFin}" }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener horarios: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Elimina todos los horarios de un grupo (para reconfigurar).
     * 
     * @param grupoId ID del grupo
     * @return ValidationResult
     */
    fun limpiarHorarios(grupoId: Int): ValidationResult {
        if (grupoId <= 0) {
            return ValidationResult.Error("ID de grupo inválido")
        }
        
        return try {
            horarioRepository.eliminarPorGrupo(grupoId)
            Log.d(TAG, "Horarios eliminados para grupo $grupoId")
            ValidationResult.Success
        } catch (e: Exception) {
            val mensaje = "Error al limpiar horarios: ${e.message}"
            Log.e(TAG, mensaje, e)
            ValidationResult.Error(mensaje)
        }
    }
    
    /**
     * Valida formato de hora HH:mm.
     */
    private fun esHoraValida(hora: String): Boolean {
        val regex = Regex("^([0-1][0-9]|2[0-3]):[0-5][0-9]$")
        return regex.matches(hora)
    }
    
    /**
     * Verifica que hora fin sea mayor que hora inicio.
     */
    private fun esHoraFinMayor(horaInicio: String, horaFin: String): Boolean {
        try {
            val minutosInicio = convertirHoraAMinutos(horaInicio)
            val minutosFin = convertirHoraAMinutos(horaFin)
            return minutosFin > minutosInicio
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Convierte hora HH:mm a minutos desde medianoche.
     */
    private fun convertirHoraAMinutos(hora: String): Int {
        val partes = hora.split(":")
        val horas = partes[0].toInt()
        val minutos = partes[1].toInt()
        return horas * 60 + minutos
    }
    
    /**
     * Obtiene el día actual del sistema.
     */
    fun obtenerDiaActual(): String {
        val calendario = java.util.Calendar.getInstance()
        val numeroDia = calendario.get(java.util.Calendar.DAY_OF_WEEK)
        
        return when (numeroDia) {
            java.util.Calendar.MONDAY -> "Lunes"
            java.util.Calendar.TUESDAY -> "Martes"
            java.util.Calendar.WEDNESDAY -> "Miércoles"
            java.util.Calendar.THURSDAY -> "Jueves"
            java.util.Calendar.FRIDAY -> "Viernes"
            java.util.Calendar.SATURDAY -> "Sábado"
            else -> "Domingo"
        }
    }
    
    /**
     * Obtiene la hora actual del sistema.
     */
    fun obtenerHoraActual(): String {
        val calendario = java.util.Calendar.getInstance()
        val hora = calendario.get(java.util.Calendar.HOUR_OF_DAY)
        val minutos = calendario.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hora, minutos)
    }
}

