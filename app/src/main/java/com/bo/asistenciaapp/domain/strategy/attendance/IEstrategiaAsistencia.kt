package com.bo.asistenciaapp.domain.strategy.attendance

/**
 * Interface del Patrón Strategy para determinar el estado de asistencia.
 * 
 * ## Patrón Strategy - Rol: Strategy (Interface)
 * Define el contrato que deben cumplir todas las estrategias concretas
 * para calcular el estado de asistencia según diferentes criterios.
 * 
 * ## Responsabilidad:
 * - Definir el método común para todas las estrategias
 * - Permitir que el contexto (AsistenciaCU) use diferentes algoritmos
 * - Hacer intercambiables las estrategias en tiempo de ejecución
 * 
 * ## Ventajas del Patrón:
 * - Elimina condicionales if/else para determinar el estado
 * - Fácil agregar nuevas estrategias sin modificar código existente
 * - Cada estrategia encapsula su propia lógica
 * - Cumple con el principio Open/Closed (abierto a extensión, cerrado a modificación)
 * 
 * ## Ejemplo de uso:
 * ```kotlin
 * val estrategia: IEstrategiaAsistencia = EstrategiaPresente()
 * val estado = estrategia.calcularEstado("08:05", "08:00")
 * // resultado: "PRESENTE"
 * ```
 */
interface IEstrategiaAsistencia {
    
    /**
     * Calcula el estado de asistencia según el algoritmo específico de la estrategia.
     * 
     * Este método define el comportamiento variable del patrón Strategy.
     * Cada estrategia concreta implementará su propia lógica para determinar
     * si el estudiante está presente, llegó tarde o faltó.
     * 
     * @param horaMarcado Hora en que el estudiante marcó asistencia (formato HH:mm)
     * @param horaInicio Hora de inicio de la clase (formato HH:mm)
     * @param toleranciaMinutos ⭐ Tolerancia en minutos obtenida de la BD (configurable por grupo)
     * @return Estado de asistencia: "PRESENTE", "RETRASO" o "FALTA"
     * 
     * ## Ejemplo:
     * ```kotlin
     * // Estudiante llega a las 08:05, clase inicia a las 08:00, tolerancia 10 minutos
     * val estado = calcularEstado("08:05", "08:00", 10)
     * // El resultado dependerá de la estrategia concreta utilizada
     * ```
     */
    fun calcularEstado(horaMarcado: String, horaInicio: String, toleranciaMinutos: Int = 10): String
}

