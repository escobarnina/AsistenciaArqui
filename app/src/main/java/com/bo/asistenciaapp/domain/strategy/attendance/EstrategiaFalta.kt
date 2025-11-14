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
        private const val MARGEN_PRESENTE = 10 // 0-10 min = PRESENTE
        private const val MARGEN_RETRASO = 30  // 11-30 min = RETRASO
    }
    
    /**
     * Calcula el estado con política estricta (Estricto).
     * 
     * ⭐ PATRÓN STRATEGY CON MÁRGENES:
     * - 0-10 min después del inicio → PRESENTE
     * - 11-30 min después del inicio → RETRASO
     * - >30 min después del inicio → FALTA
     * 
     * Esta estrategia es más estricta y marca FALTA más fácilmente.
     * 
     * @param horaMarcado Hora en que marcó asistencia (HH:mm)
     * @param horaInicio Hora de inicio de clase (HH:mm)
     * @param toleranciaMinutos No se usa (márgenes fijos de 15 min)
     * @return "PRESENTE", "RETRASO" o "FALTA" según el caso
     */
    override fun calcularEstado(horaMarcado: String, horaInicio: String, toleranciaMinutos: Int): String {
        Log.d(TAG, "Evaluando asistencia (Estricto) - Marcado: $horaMarcado, Inicio: $horaInicio")
        
        try {
            // Convertir horas a minutos desde medianoche
            val minutosMarcado = convertirHoraAMinutos(horaMarcado)
            val minutosInicio = convertirHoraAMinutos(horaInicio)
            
            // Calcular diferencia
            val diferencia = minutosMarcado - minutosInicio
            
            Log.d(TAG, "Diferencia: $diferencia minutos | Márgenes: 0-10 PRESENTE, 11-30 RETRASO, >30 FALTA")
            
            // Lógica con márgenes (estricta)
            val estado = when {
                diferencia <= MARGEN_PRESENTE -> {
                    // Llegó a tiempo (0-10 min)
                    Log.d(TAG, "Llegó a tiempo (0-10 min) → PRESENTE")
                    "PRESENTE"
                }
                diferencia <= MARGEN_RETRASO -> {
                    // Llegó con retraso (11-30 min)
                    Log.d(TAG, "Llegó con retraso (11-30 min) → RETRASO")
                    "RETRASO"
                }
                else -> {
                    // Llegó muy tarde (>30 min) → FALTA
                    Log.d(TAG, "Llegó muy tarde (>30 min) → FALTA")
                    "FALTA"
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

