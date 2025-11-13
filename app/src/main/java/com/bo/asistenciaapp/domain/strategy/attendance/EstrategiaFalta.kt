package com.bo.asistenciaapp.domain.strategy.attendance

import android.util.Log

/**
 * Estrategia concreta que determina el estado como FALTA.
 * 
 * ## Patrón Strategy - Rol: ConcreteStrategy (Estrategia Concreta)
 * Implementa el algoritmo específico para considerar al estudiante con FALTA.
 * 
 * ## Lógica de negocio:
 * - Si el estudiante marca asistencia más de 30 minutos después
 *   del inicio de clase, o no marca, se considera FALTA.
 * - Ejemplo: Clase inicia 08:00, marca después de 08:30 → FALTA
 * 
 * ## Casos de uso:
 * - Materias con políticas estrictas de asistencia
 * - Clases donde no se permite entrada tardía
 * - Cuando se requiere puntualidad obligatoria
 * 
 * @see IEstrategiaAsistencia
 */
class EstrategiaFalta : IEstrategiaAsistencia {
    
    companion object {
        private const val TAG = "EstrategiaFalta"
        private const val MINUTOS_LIMITE_FALTA = 30  // Después de estos minutos es falta automática
    }
    
    /**
     * Calcula el estado con política estricta basada en tolerancia configurable.
     * 
     * ⭐ PATRÓN STRATEGY CON DATOS DE BD:
     * Ahora usa toleranciaMinutos obtenido de la tabla grupos (configurable por grupo)
     * en lugar de constantes hardcodeadas. Esta estrategia es más estricta.
     * 
     * Algoritmo:
     * 1. Convertir ambas horas a minutos desde medianoche
     * 2. Calcular la diferencia en minutos
     * 3. Si diferencia > (toleranciaMinutos * 3) → FALTA
     * 4. Si diferencia > toleranciaMinutos → RETRASO
     * 5. Si diferencia <= toleranciaMinutos → PRESENTE
     * 
     * @param horaMarcado Hora en que marcó asistencia (HH:mm)
     * @param horaInicio Hora de inicio de clase (HH:mm)
     * @param toleranciaMinutos Tolerancia obtenida de la BD (por defecto 10)
     * @return "FALTA", "RETRASO" o "PRESENTE" según el caso
     */
    override fun calcularEstado(horaMarcado: String, horaInicio: String, toleranciaMinutos: Int): String {
        Log.d(TAG, "Evaluando asistencia - Marcado: $horaMarcado, Inicio: $horaInicio, Tolerancia: $toleranciaMinutos min")
        
        try {
            // Convertir horas a minutos desde medianoche
            val minutosMarcado = convertirHoraAMinutos(horaMarcado)
            val minutosInicio = convertirHoraAMinutos(horaInicio)
            
            // Calcular diferencia
            val diferencia = minutosMarcado - minutosInicio
            
            // ⭐ Calcular límites basados en tolerancia de la BD
            val limiteFalta = toleranciaMinutos * 3  // Después de 3x la tolerancia es falta
            
            Log.d(TAG, "Diferencia: $diferencia minutos | Tolerancia: $toleranciaMinutos | Límite falta: $limiteFalta")
            
            // Lógica de la estrategia: FALTA si llega muy tarde
            val estado = when {
                diferencia > limiteFalta -> {
                    // Llegó muy tarde (más del límite de falta)
                    Log.d(TAG, "Llegó muy tarde (diferencia > $limiteFalta min) → FALTA")
                    "FALTA"
                }
                diferencia > toleranciaMinutos -> {
                    // Llegó con retraso moderado (entre tolerancia y límite de falta)
                    Log.d(TAG, "Llegó con retraso (diferencia entre $toleranciaMinutos y $limiteFalta min)")
                    "RETRASO"
                }
                diferencia >= 0 -> {
                    // Llegó a tiempo o con pequeño retraso (dentro de tolerancia)
                    Log.d(TAG, "Llegó a tiempo (diferencia <= $toleranciaMinutos min)")
                    "PRESENTE"
                }
                else -> {
                    // Llegó antes de la hora de inicio (diferencia negativa)
                    Log.d(TAG, "Llegó antes de la hora de inicio")
                    "PRESENTE"
                }
            }
            
            Log.d(TAG, "Estado determinado: $estado")
            return estado
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular estado: ${e.message}")
            return "FALTA" // Por defecto retorna FALTA
        }
    }
    
    /**
     * Convierte una hora en formato HH:mm a minutos desde medianoche.
     * 
     * @param hora Hora en formato HH:mm (ejemplo: "08:30")
     * @return Minutos desde medianoche (ejemplo: 510)
     */
    private fun convertirHoraAMinutos(hora: String): Int {
        val partes = hora.split(":")
        val horas = partes[0].toInt()
        val minutos = partes[1].toInt()
        return horas * 60 + minutos
    }
}

