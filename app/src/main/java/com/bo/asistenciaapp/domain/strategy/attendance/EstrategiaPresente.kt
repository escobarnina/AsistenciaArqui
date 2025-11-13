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
        private const val MARGEN_MINUTOS = 10 // Margen de tolerancia en minutos
    }
    
    /**
     * Calcula el estado como PRESENTE si marca dentro del margen de tolerancia.
     * 
     * Algoritmo:
     * 1. Convertir ambas horas a minutos desde medianoche
     * 2. Calcular la diferencia en minutos
     * 3. Si la diferencia es <= 10 minutos después de inicio → PRESENTE
     * 4. Caso contrario → delegar a otra estrategia (pero esta siempre retorna PRESENTE)
     * 
     * @param horaMarcado Hora en que marcó asistencia (HH:mm)
     * @param horaInicio Hora de inicio de clase (HH:mm)
     * @return "PRESENTE" si marca dentro del margen, "PRESENTE" en todos los casos
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
            
            // Lógica de la estrategia: PRESENTE si marca dentro del margen
            val estado = if (diferencia <= MARGEN_MINUTOS) {
                "PRESENTE"
            } else {
                // Esta estrategia siempre considera PRESENTE
                // (en un sistema real, aquí podría retornar otro estado)
                "PRESENTE"
            }
            
            Log.d(TAG, "Estado determinado: $estado")
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

