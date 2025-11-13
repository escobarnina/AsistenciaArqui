package com.bo.asistenciaapp.domain.usecase

import android.util.Log
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.domain.strategy.attendance.IEstrategiaAsistencia
import com.bo.asistenciaapp.domain.strategy.attendance.EstrategiaRetraso
import com.bo.asistenciaapp.domain.utils.ValidationResult

/**
 * Caso de uso especializado para PROBAR el Patrón Strategy en modo testing.
 * 
 * ## 🎯 OBJETIVO - DEMOSTRACIÓN DEL PATRÓN STRATEGY:
 * Este Use Case permite probar diferentes estrategias de asistencia
 * SIN las validaciones estrictas de horario del `AsistenciaCU` normal.
 * 
 * ## 📐 PATRÓN STRATEGY - CONTEXTO DE PRUEBA:
 * Este es un CONTEXTO alternativo del patrón Strategy que:
 * - Permite inyectar cualquier estrategia (EstrategiaPresente, EstrategiaRetraso, EstrategiaFalta)
 * - Acepta horas simuladas (no usa hora del sistema)
 * - NO valida horarios de clase (para pruebas flexibles)
 * - Registra el estado calculado por la estrategia
 * 
 * ## 🔗 RELACIÓN CON EL DIAGRAMA GENÉRICO:
 * ```
 * ┌──────────────────────────┐
 * │ MarcarAsistenciaTestCU   │ ← CONTEXTO (Context)
 * │ (Modo Testing)           │
 * ├──────────────────────────┤
 * │ - estrategia             │ ← Referencia a Strategy
 * │ + setEstrategia()        │
 * │ + marcarAsistenciaTest() │ ← doSomething()
 * └──────────────────────────┘
 *          ↓ delega
 * estrategia.calcularEstado(horaMarcado, horaInicio, tolerancia)
 * ```
 * 
 * ## ⚠️ USO:
 * - Solo para TESTING y DEMOSTRACIÓN
 * - En producción usar `AsistenciaCU` con validaciones completas
 * 
 * @property asistenciaRepository Repositorio para persistir asistencias
 */
class MarcarAsistenciaTestCU(private val asistenciaRepository: AsistenciaRepository) {
    
    companion object {
        private const val TAG = "MarcarAsistenciaTestCU"
    }
    
    /**
     * Estrategia de asistencia inyectada (PATRÓN STRATEGY).
     * Puede ser: EstrategiaPresente, EstrategiaRetraso, EstrategiaFalta.
     */
    private var _estrategia: IEstrategiaAsistencia? = null
    
    /**
     * Establece la estrategia a utilizar (PATRÓN STRATEGY).
     * 
     * @param estrategia Estrategia concreta a aplicar
     */
    fun setEstrategia(estrategia: IEstrategiaAsistencia) {
        Log.d(TAG, "🎯 [STRATEGY TEST] Cambiando estrategia a: ${estrategia::class.simpleName}")
        this._estrategia = estrategia
    }
    
    /**
     * Marca asistencia en MODO TESTING (sin validaciones de horario).
     * 
     * ## FLUJO DEL PATRÓN STRATEGY:
     * 1. Obtiene tolerancia del grupo (dato configurable)
     * 2. Delega cálculo a la estrategia:
     *    `estrategia.calcularEstado(horaMarcado, horaInicio, tolerancia)`
     * 3. Registra asistencia con el estado calculado
     * 4. Retorna resultado con detalle de la estrategia usada
     * 
     * @param alumnoId ID del alumno
     * @param grupoId ID del grupo
     * @param fecha Fecha de asistencia (YYYY-MM-DD)
     * @param horaMarcado Hora simulada de marcado (HH:mm)
     * @param horaInicio Hora simulada de inicio de clase (HH:mm)
     * @return Result con estado calculado y detalles
     */
    fun marcarAsistenciaTest(
        alumnoId: Int,
        grupoId: Int,
        fecha: String,
        horaMarcado: String,
        horaInicio: String
    ): ResultadoAsistenciaTest {
        Log.d(TAG, "=== MARCANDO ASISTENCIA EN MODO TEST ===")
        Log.d(TAG, "Alumno: $alumnoId, Grupo: $grupoId")
        Log.d(TAG, "Fecha: $fecha")
        Log.d(TAG, "Hora Marcado: $horaMarcado, Hora Inicio: $horaInicio")
        
        // Validaciones básicas
        if (alumnoId <= 0 || grupoId <= 0) {
            return ResultadoAsistenciaTest(
                exito = false,
                mensaje = "IDs inválidos",
                estado = null,
                estrategiaUsada = null,
                toleranciaMinutos = 0,
                diferencia = 0
            )
        }
        
        if (!esHoraValida(horaMarcado) || !esHoraValida(horaInicio)) {
            return ResultadoAsistenciaTest(
                exito = false,
                mensaje = "Formato de hora inválido. Use HH:mm",
                estado = null,
                estrategiaUsada = null,
                toleranciaMinutos = 0,
                diferencia = 0
            )
        }
        
        // Verificar inscripción del alumno
        if (!asistenciaRepository.estaInscrito(alumnoId, grupoId)) {
            Log.w(TAG, "⚠️ Alumno $alumnoId no está inscrito en grupo $grupoId")
            return ResultadoAsistenciaTest(
                exito = false,
                mensaje = "El alumno no está inscrito en este grupo",
                estado = null,
                estrategiaUsada = null,
                toleranciaMinutos = 0,
                diferencia = 0
            )
        }
        
        // Obtener tolerancia del grupo (dato configurable)
        val toleranciaMinutos = asistenciaRepository.obtenerToleranciaGrupo(grupoId)
        Log.d(TAG, "📊 Tolerancia del grupo: $toleranciaMinutos minutos")
        
        // Calcular diferencia en minutos
        val diferencia = calcularDiferenciaMinutos(horaMarcado, horaInicio)
        Log.d(TAG, "⏱️ Diferencia: $diferencia minutos")
        
        // Obtener estrategia (usar default si no hay)
        val estrategia = _estrategia ?: run {
            Log.w(TAG, "⚠️ No hay estrategia definida, usando EstrategiaRetraso por defecto")
            EstrategiaRetraso()
        }
        
        val nombreEstrategia = estrategia::class.simpleName ?: "Desconocida"
        Log.d(TAG, "🎯 [STRATEGY] Aplicando estrategia: $nombreEstrategia")
        
        // ⭐ PATRÓN STRATEGY EN ACCIÓN:
        // El contexto delega el cálculo a la estrategia concreta
        val estado = try {
            estrategia.calcularEstado(horaMarcado, horaInicio, toleranciaMinutos)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al calcular estado con estrategia: ${e.message}", e)
            return ResultadoAsistenciaTest(
                exito = false,
                mensaje = "Error al calcular estado: ${e.message}",
                estado = null,
                estrategiaUsada = nombreEstrategia,
                toleranciaMinutos = toleranciaMinutos,
                diferencia = diferencia
            )
        }
        
        Log.d(TAG, "✅ [STRATEGY] Estado calculado: $estado")
        
        // Registrar asistencia
        return try {
            asistenciaRepository.registrar(alumnoId, grupoId, fecha)
            Log.d(TAG, "✅ Asistencia registrada exitosamente en modo TEST")
            
            ResultadoAsistenciaTest(
                exito = true,
                mensaje = "Asistencia marcada exitosamente",
                estado = estado,
                estrategiaUsada = nombreEstrategia,
                toleranciaMinutos = toleranciaMinutos,
                diferencia = diferencia
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al registrar asistencia: ${e.message}", e)
            ResultadoAsistenciaTest(
                exito = false,
                mensaje = "Error al registrar: ${e.message}",
                estado = estado,
                estrategiaUsada = nombreEstrategia,
                toleranciaMinutos = toleranciaMinutos,
                diferencia = diferencia
            )
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
     * Calcula diferencia en minutos entre dos horas.
     */
    private fun calcularDiferenciaMinutos(horaMarcado: String, horaInicio: String): Int {
        return try {
            val minutosMarcado = convertirHoraAMinutos(horaMarcado)
            val minutosInicio = convertirHoraAMinutos(horaInicio)
            minutosMarcado - minutosInicio
        } catch (e: Exception) {
            0
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
}

/**
 * Resultado detallado del marcado de asistencia en modo testing.
 * Incluye toda la información necesaria para demostrar el Patrón Strategy.
 * 
 * @property exito Si la operación fue exitosa
 * @property mensaje Mensaje descriptivo del resultado
 * @property estado Estado calculado por la estrategia (PRESENTE/RETRASO/FALTA)
 * @property estrategiaUsada Nombre de la estrategia que calculó el estado
 * @property toleranciaMinutos Tolerancia del grupo usada en el cálculo
 * @property diferencia Diferencia en minutos entre hora marcado y hora inicio
 */
data class ResultadoAsistenciaTest(
    val exito: Boolean,
    val mensaje: String,
    val estado: String?,
    val estrategiaUsada: String?,
    val toleranciaMinutos: Int,
    val diferencia: Int
)

