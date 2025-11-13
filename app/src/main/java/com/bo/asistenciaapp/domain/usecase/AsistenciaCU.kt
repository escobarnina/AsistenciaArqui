package com.bo.asistenciaapp.domain.usecase

import android.util.Log
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.strategy.attendance.IEstrategiaAsistencia
import com.bo.asistenciaapp.domain.strategy.attendance.EstrategiaRetraso
import com.bo.asistenciaapp.domain.strategy.attendance.EstrategiaPresente
import com.bo.asistenciaapp.domain.strategy.attendance.EstrategiaFalta
import com.bo.asistenciaapp.domain.utils.Validators
import com.bo.asistenciaapp.domain.utils.ValidationResult
import com.bo.asistenciaapp.domain.utils.validate

/**
 * Caso de uso para gestionar asistencias.
 * 
 * ## Patrón Strategy - Rol: Context (Contexto)
 * Este caso de uso actúa como contexto que usa diferentes estrategias
 * para calcular el estado de asistencia (PRESENTE, RETRASO, FALTA).
 * 
 * ## Responsabilidades:
 * - Orquestar la lógica de negocio relacionada con asistencias
 * - Mantener una referencia a la estrategia actual
 * - Delegar el cálculo del estado a la estrategia seleccionada
 * - Validar datos antes de realizar operaciones
 * 
 * ## Ventajas del patrón:
 * - Permite cambiar el algoritmo de cálculo en tiempo de ejecución
 * - Elimina condicionales complejos para determinar el estado
 * - Facilita agregar nuevas estrategias sin modificar este código
 * 
 * @property asistenciaRepository Repositorio para acceder a datos
 * @property estrategia Estrategia actual para calcular el estado (patrón Strategy)
 */
class AsistenciaCU(private val asistenciaRepository: AsistenciaRepository) {
    
    companion object {
        private const val TAG = "AsistenciaCU"
    }
    
    /**
     * Estrategia actual para calcular el estado de asistencia.
     * 
     * ## Patrón Strategy:
     * Esta propiedad privada mantiene la referencia a la estrategia concreta
     * que se usará para determinar si un estudiante está presente,
     * llegó tarde o faltó.
     * 
     * Se inicializa mediante el método setEstrategia().
     */
    private var _estrategia: IEstrategiaAsistencia? = null
    
    /**
     * Establece la estrategia para calcular el estado de asistencia.
     * 
     * ## Patrón Strategy:
     * Este método permite cambiar el algoritmo de cálculo en tiempo de ejecución.
     * El contexto (este caso de uso) no conoce los detalles de implementación
     * de cada estrategia, solo sabe que debe usar el método calcularEstado().
     * 
     * @param estrategia Nueva estrategia a utilizar
     * 
     * ## Ejemplo de uso:
     * ```kotlin
     * asistenciaCU.setEstrategia(EstrategiaPresente())  // Política flexible
     * asistenciaCU.setEstrategia(EstrategiaRetraso())   // Política estándar
     * asistenciaCU.setEstrategia(EstrategiaFalta())     // Política estricta
     * ```
     */
    fun setEstrategia(estrategia: IEstrategiaAsistencia) {
        Log.d(TAG, "Cambiando estrategia a: ${estrategia::class.simpleName}")
        this._estrategia = estrategia
    }
    
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
     * ## Patrón Strategy APLICADO:
     * Este método usa la estrategia actual para calcular el estado de asistencia
     * (PRESENTE, RETRASO o FALTA) basándose en la hora de marcado y hora de inicio.
     * 
     * @param alumnoId ID del alumno
     * @param grupoId ID del grupo
     * @param fecha Fecha de la asistencia
     * @param horaMarcado Hora en que el alumno marcó asistencia (formato HH:mm)
     * @param horaInicio Hora de inicio de la clase (formato HH:mm)
     * @return ValidationResult con el resultado de la operación
     */
    fun marcarAsistencia(
        alumnoId: Int, 
        grupoId: Int, 
        fecha: String,
        horaMarcado: String,
        horaInicio: String
    ): ValidationResult {
        Log.d(TAG, "=== MARCANDO ASISTENCIA ===")
        Log.d(TAG, "Alumno: $alumnoId, Grupo: $grupoId, Fecha: $fecha")
        Log.d(TAG, "Hora marcado: $horaMarcado, Hora inicio: $horaInicio")
        
        // Validar datos básicos
        val validation = validarDatosAsistencia(alumnoId, grupoId, fecha)
        
        if (!validation.isValid) {
            Log.e(TAG, "Validación de datos falló")
            return validation
        }
        
        // Validar que el alumno puede marcar asistencia (regla de negocio)
        if (!puedeMarcarAsistencia(alumnoId, grupoId)) {
            Log.e(TAG, "El alumno no puede marcar asistencia")
            return ValidationResult.Error("No se puede marcar asistencia: no es el día/hora correcta o no está inscrito")
        }
        
        // ⭐ PATRÓN STRATEGY CON DATOS DE BD:
        // Obtener tolerancia del grupo desde la base de datos
        val toleranciaMinutos = asistenciaRepository.obtenerToleranciaGrupo(grupoId)
        Log.d(TAG, "Tolerancia obtenida del grupo $grupoId: $toleranciaMinutos minutos")
        
        // ⭐ CONFIGURAR ESTRATEGIA AUTOMÁTICAMENTE DESDE BD:
        // Si no hay estrategia configurada manualmente, obtenerla del grupo
        if (_estrategia == null) {
            val tipoEstrategia = asistenciaRepository.obtenerTipoEstrategiaGrupo(grupoId)
            Log.d(TAG, "Tipo de estrategia del grupo $grupoId: $tipoEstrategia")
            _estrategia = when (tipoEstrategia) {
                "PRESENTE" -> EstrategiaPresente()
                "FALTA" -> EstrategiaFalta()
                else -> EstrategiaRetraso()  // Por defecto RETRASO
            }
            Log.d(TAG, "Estrategia configurada automáticamente: ${_estrategia!!::class.simpleName}")
        }
        
        // Delegar el cálculo del estado a la estrategia actual
        val estado = _estrategia!!.calcularEstado(horaMarcado, horaInicio, toleranciaMinutos)
        Log.d(TAG, "Estado calculado por la estrategia: $estado (tolerancia: $toleranciaMinutos min)")
        
        // Registrar asistencia en el repositorio
        asistenciaRepository.registrar(alumnoId, grupoId, fecha)
        
        Log.d(TAG, "Asistencia registrada exitosamente con estado: $estado")
        
        // Retornar éxito con el estado calculado
        return ValidationResult.Success
    }
    
    /**
     * Versión simplificada de marcarAsistencia sin hora (retrocompatibilidad).
     * Usa la hora actual del sistema y obtiene la hora de inicio del grupo automáticamente.
     */
    fun marcarAsistencia(alumnoId: Int, grupoId: Int, fecha: String): ValidationResult {
        // Usar hora actual del sistema
        val horaActual = obtenerHoraActual()
        // Obtener hora de inicio del grupo desde la BD
        val horaInicio = asistenciaRepository.obtenerHoraInicioGrupo(grupoId)
        Log.d(TAG, "Hora del sistema: $horaActual, Hora inicio grupo: $horaInicio")
        
        return marcarAsistencia(alumnoId, grupoId, fecha, horaActual, horaInicio)
    }
    
    /**
     * Obtiene la hora actual del sistema en formato HH:mm.
     */
    private fun obtenerHoraActual(): String {
        val calendario = java.util.Calendar.getInstance()
        val hora = calendario.get(java.util.Calendar.HOUR_OF_DAY)
        val minutos = calendario.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hora, minutos)
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