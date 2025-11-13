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
     * Calcula el estado como RETRASO si marca entre 10 y 30 minutos tarde.
     * 
     * Algoritmo:
     * 1. Convertir ambas horas a minutos desde medianoche
     * 2. Calcular la diferencia en minutos
     * 3. Si diferencia > 10 y <= 30 minutos → RETRASO
     * 4. Si diferencia <= 10 minutos → PRESENTE (llegó a tiempo con margen)
     * 5. Si diferencia > 30 minutos → FALTA (llegó muy tarde)
     * 
     * @param horaMarcado Hora en que marcó asistencia (HH:mm)
     * @param horaInicio Hora de inicio de clase (HH:mm)
     * @return "RETRASO" si marca en el rango permitido, "PRESENTE" o "FALTA" según caso
     */
    override fun calcularEstado(horaMarcado: String, horaInicio: String): String {
        Log.d(TAG, "Evaluando asistencia - Marcado: $horaMarcado, Inicio: $horaInicio")
        
        try {
            // Convertir horas a minutos desde medianoche
            val minutosMarcado = convertirHoraAMinutos(horaMarcado)
            val minutosInicio = convertirHoraAMinutos(horaInicio)
            
            // Calcular diferencia
            val diferencia = minutosMarcado - minutosInicio
            
            Log.d(TAG, "Diferencia: $diferencia minutos")
            
            // Lógica de la estrategia: RETRASO si está en el rango
            val estado = when {
                diferencia <= MINUTOS_MIN_RETRASO -> {
                    // Llegó a tiempo (dentro del margen de tolerancia)
                    Log.d(TAG, "Llegó a tiempo (diferencia <= $MINUTOS_MIN_RETRASO min)")
                    "PRESENTE"
                }
                diferencia <= MINUTOS_MAX_RETRASO -> {
                    // Llegó con retraso (entre 10 y 30 minutos tarde)
                    Log.d(TAG, "Llegó con retraso (diferencia entre $MINUTOS_MIN_RETRASO y $MINUTOS_MAX_RETRASO min)")
                    "RETRASO"
                }
                else -> {
                    // Llegó muy tarde (más de 30 minutos)
                    Log.d(TAG, "Llegó muy tarde (diferencia > $MINUTOS_MAX_RETRASO min)")
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

