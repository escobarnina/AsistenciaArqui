package com.bo.asistenciaapp.domain.strategy.attendance

import android.util.Log

/**
 * Estrategia concreta que determina el estado como PRESENTE.
 * 
 * ## Patrón Strategy - Rol: ConcreteStrategy (Estrategia Concreta)
 * Implementa el algoritmo específico para considerar al estudiante como PRESENTE.
 * 
 * ## Lógica de negocio:
 * - Si el estudiante marca asistencia dentro de los primeros 10 minutos
 *   después del inicio de clase, se considera PRESENTE.
 * - Ejemplo: Clase inicia 08:00, marca hasta 08:10 → PRESENTE
 * 
 * ## Casos de uso:
 * - Materias con horario estricto
 * - Clases presenciales donde se valora la puntualidad
 * - Cuando se quiere ser flexible con pequeños retrasos
 * 
 * @see IEstrategiaAsistencia
 */
class EstrategiaPresente : IEstrategiaAsistencia {
    
    companion object {
        private const val TAG = "EstrategiaPresente"
        private const val MARGEN_PRESENTE = 10 // 0-10 min = PRESENTE
        private const val MARGEN_RETRASO = 30  // 11-30 min = RETRASO
    }
    
    /**
     * Calcula el estado con política muy flexible (Muy Flexible).
     * 
     * ⭐ PATRÓN STRATEGY CON MÁRGENES:
     * - 0-10 min después del inicio → PRESENTE
     * - 11-30 min después del inicio → RETRASO
     * - >30 min después del inicio → FALTA
     * 
     * Esta estrategia siempre marca como PRESENTE independientemente del tiempo.
     * 
     * @param horaMarcado Hora en que marcó asistencia (HH:mm)
     * @param horaInicio Hora de inicio de clase (HH:mm)
     * @param toleranciaMinutos No se usa en esta estrategia (siempre PRESENTE)
     * @return "PRESENTE" siempre
     */
    override fun calcularEstado(horaMarcado: String, horaInicio: String, toleranciaMinutos: Int): String {
        Log.d(TAG, "Evaluando asistencia (Muy Flexible) - Marcado: $horaMarcado, Inicio: $horaInicio")
        
        try {
            // Convertir horas a minutos desde medianoche
            val minutosMarcado = convertirHoraAMinutos(horaMarcado)
            val minutosInicio = convertirHoraAMinutos(horaInicio)
            
            // Calcular diferencia
            val diferencia = minutosMarcado - minutosInicio
            
            Log.d(TAG, "Diferencia: $diferencia minutos")
            
            // Estrategia Muy Flexible: siempre PRESENTE
            val estado = "PRESENTE"
            
            Log.d(TAG, "Estado determinado: $estado (Muy Flexible)")
            return estado
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular estado: ${e.message}")
            return "PRESENTE" // Por defecto retorna PRESENTE
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

