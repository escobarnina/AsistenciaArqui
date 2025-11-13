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
     * Calcula el estado como FALTA si marca más de 30 minutos tarde.
     * 
     * Algoritmo:
     * 1. Convertir ambas horas a minutos desde medianoche
     * 2. Calcular la diferencia en minutos
     * 3. Si diferencia > 30 minutos → FALTA
     * 4. Si diferencia <= 30 minutos → evaluar si PRESENTE o RETRASO
     * 
     * @param horaMarcado Hora en que marcó asistencia (HH:mm)
     * @param horaInicio Hora de inicio de clase (HH:mm)
     * @return "FALTA" si marca muy tarde, o estado correspondiente según diferencia
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
            
            // Lógica de la estrategia: FALTA si llega muy tarde
            val estado = when {
                diferencia > MINUTOS_LIMITE_FALTA -> {
                    // Llegó muy tarde (más de 30 minutos)
                    Log.d(TAG, "Llegó muy tarde (diferencia > $MINUTOS_LIMITE_FALTA min) → FALTA")
                    "FALTA"
                }
                diferencia > 10 -> {
                    // Llegó con retraso moderado (entre 10 y 30 minutos)
                    Log.d(TAG, "Llegó con retraso (diferencia entre 10 y $MINUTOS_LIMITE_FALTA min)")
                    "RETRASO"
                }
                diferencia >= 0 -> {
                    // Llegó a tiempo o con pequeño retraso (0-10 minutos)
                    Log.d(TAG, "Llegó a tiempo (diferencia <= 10 min)")
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

