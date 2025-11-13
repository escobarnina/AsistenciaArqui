package com.bo.asistenciaapp.domain.strategy.attendance

import android.util.Log

/**
 * Estrategia concreta que determina el estado como RETRASO.
 * 
 * ## Patrón Strategy - Rol: ConcreteStrategy (Estrategia Concreta)
 * Implementa el algoritmo específico para considerar al estudiante con RETRASO.
 * 
 * ## Lógica de negocio:
 * - Si el estudiante marca asistencia entre 10 y 30 minutos después
 *   del inicio de clase, se considera RETRASO.
 * - Ejemplo: Clase inicia 08:00, marca entre 08:10 y 08:30 → RETRASO
 * 
 * ## Casos de uso:
 * - Materias que permiten entrada tardía pero la registran
 * - Clases donde se penaliza llegar tarde
 * - Cuando se quiere llevar estadísticas de puntualidad
 * 
 * @see IEstrategiaAsistencia
 */
class EstrategiaRetraso : IEstrategiaAsistencia {
    
    companion object {
        private const val TAG = "EstrategiaRetraso"
        private const val MINUTOS_MIN_RETRASO = 10  // A partir de estos minutos es retraso
        private const val MINUTOS_MAX_RETRASO = 30  // Después de estos minutos es falta
    }
    
    /**
     * Calcula el estado como PRESENTE, RETRASO o FALTA según tolerancia configurable.
     * 
     * ⭐ PATRÓN STRATEGY CON DATOS DE BD:
     * Ahora usa toleranciaMinutos obtenido de la tabla grupos (configurable por grupo)
     * en lugar de constantes hardcodeadas.
     * 
     * Algoritmo:
     * 1. Convertir ambas horas a minutos desde medianoche
     * 2. Calcular la diferencia en minutos
     * 3. Si diferencia <= toleranciaMinutos → PRESENTE
     * 4. Si diferencia <= (toleranciaMinutos * 3) → RETRASO
     * 5. Si diferencia > (toleranciaMinutos * 3) → FALTA
     * 
     * Ejemplo con tolerancia de 10 minutos:
     * - 0-10 min → PRESENTE
     * - 11-30 min → RETRASO
     * - >30 min → FALTA
     * 
     * @param horaMarcado Hora en que marcó asistencia (HH:mm)
     * @param horaInicio Hora de inicio de clase (HH:mm)
     * @param toleranciaMinutos Tolerancia obtenida de la BD (por defecto 10)
     * @return "PRESENTE", "RETRASO" o "FALTA" según el caso
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
            val limiteRetraso = toleranciaMinutos * 3  // Hasta 3x la tolerancia es retraso
            
            Log.d(TAG, "Diferencia: $diferencia minutos | Tolerancia: $toleranciaMinutos | Límite retraso: $limiteRetraso")
            
            // Lógica de la estrategia: RETRASO si está en el rango
            val estado = when {
                diferencia <= toleranciaMinutos -> {
                    // Llegó a tiempo (dentro del margen de tolerancia)
                    Log.d(TAG, "Llegó a tiempo (diferencia <= $toleranciaMinutos min)")
                    "PRESENTE"
                }
                diferencia <= limiteRetraso -> {
                    // Llegó con retraso (entre tolerancia y límite de retraso)
                    Log.d(TAG, "Llegó con retraso (diferencia entre $toleranciaMinutos y $limiteRetraso min)")
                    "RETRASO"
                }
                else -> {
                    // Llegó muy tarde (más del límite de retraso)
                    Log.d(TAG, "Llegó muy tarde (diferencia > $limiteRetraso min)")
                    "FALTA"
                }
            }
            
            Log.d(TAG, "Estado determinado: $estado")
            return estado
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular estado: ${e.message}")
            return "RETRASO" // Por defecto retorna RETRASO
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

