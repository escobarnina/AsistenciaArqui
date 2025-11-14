package com.bo.asistenciaapp.domain.usecase

import android.util.Log
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.domain.utils.ValidationResult

/**
 * Caso de uso para configurar la tolerancia de un grupo.
 * 
 * ## Patrón Strategy - Configuración Dinámica:
 * Este caso de uso permite modificar el parámetro de tolerancia que utilizan
 * las estrategias de asistencia, haciendo el sistema completamente configurable
 * sin necesidad de modificar código.
 * 
 * ## Responsabilidades:
 * - Validar que la tolerancia esté en el rango permitido (0-60 minutos)
 * - Actualizar la tolerancia en la base de datos
 * - Proporcionar feedback sobre el resultado de la operación
 * 
 * ## Arquitectura:
 * Domain Layer → Use Case coordina la lógica de negocio
 * 
 * @property grupoRepository Repositorio para acceder a datos de grupos
 */
class ConfigurarGrupoCU(private val grupoRepository: GrupoRepository) {
    
    companion object {
        private const val TAG = "ConfigurarGrupoCU"
        private const val TOLERANCIA_MINIMA = 0
        private const val TOLERANCIA_MAXIMA = 60
        
        /**
         * Tipos de estrategia válidos.
         */
        val TIPOS_ESTRATEGIA_VALIDOS = listOf("PRESENTE", "RETRASO", "FALTA")
    }
    
    /**
     * Configura la tolerancia de un grupo específico.
     * 
     * ## Validaciones:
     * - El ID del grupo debe ser positivo
     * - La tolerancia debe estar entre 0 y 60 minutos
     * 
     * ## Patrón Strategy:
     * Al actualizar la tolerancia, las estrategias de asistencia automáticamente
     * utilizarán el nuevo valor en los próximos cálculos de estado.
     * 
     * @param grupoId ID del grupo a configurar
     * @param toleranciaMinutos Nueva tolerancia en minutos (0-60)
     * @return ValidationResult.Success si se actualizó correctamente, 
     *         ValidationResult.Error con mensaje si hubo un error
     * 
     * ## Ejemplo de uso:
     * ```kotlin
     * val resultado = configurarGrupoCU.configurarTolerancia(grupoId = 1, toleranciaMinutos = 15)
     * when (resultado) {
     *     is ValidationResult.Success -> mostrarMensaje("Tolerancia actualizada")
     *     is ValidationResult.Error -> mostrarError(resultado.message)
     * }
     * ```
     */
    fun configurarTolerancia(grupoId: Int, toleranciaMinutos: Int): ValidationResult {
        Log.d(TAG, "=== CONFIGURANDO TOLERANCIA ===")
        Log.d(TAG, "Grupo ID: $grupoId, Nueva tolerancia: $toleranciaMinutos minutos")
        
        // Validación 1: ID del grupo debe ser positivo
        if (grupoId <= 0) {
            val mensaje = "ID de grupo inválido: $grupoId"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Validación 2: Tolerancia debe estar en el rango permitido
        if (toleranciaMinutos < TOLERANCIA_MINIMA || toleranciaMinutos > TOLERANCIA_MAXIMA) {
            val mensaje = "La tolerancia debe estar entre $TOLERANCIA_MINIMA y $TOLERANCIA_MAXIMA minutos. Valor recibido: $toleranciaMinutos"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Validación 3: Verificar que el grupo existe
        val grupoExiste = grupoRepository.existeGrupo(grupoId)
        if (!grupoExiste) {
            val mensaje = "El grupo con ID $grupoId no existe"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Actualizar tolerancia en la base de datos
        return try {
            grupoRepository.actualizarTolerancia(grupoId, toleranciaMinutos)
            Log.d(TAG, "✅ Tolerancia actualizada exitosamente a $toleranciaMinutos minutos")
            Log.d(TAG, "Las estrategias de asistencia ahora usarán este valor para el grupo $grupoId")
            ValidationResult.Success
        } catch (e: Exception) {
            val mensaje = "Error al actualizar tolerancia: ${e.message}"
            Log.e(TAG, mensaje, e)
            ValidationResult.Error(mensaje)
        }
    }
    
    /**
     * Obtiene la tolerancia actual de un grupo.
     * 
     * @param grupoId ID del grupo
     * @return Tolerancia en minutos, o null si el grupo no existe
     */
    fun obtenerToleranciaActual(grupoId: Int): Int? {
        if (grupoId <= 0) {
            Log.e(TAG, "ID de grupo inválido: $grupoId")
            return null
        }
        
        return try {
            val grupo = grupoRepository.obtenerPorId(grupoId)
            grupo?.toleranciaMinutos
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tolerancia: ${e.message}", e)
            null
        }
    }
    
    /**
     * Obtiene información sobre los rangos recomendados de tolerancia.
     * 
     * @return Map con información de rangos sugeridos
     */
    fun obtenerRangosRecomendados(): Map<String, IntRange> {
        return mapOf(
            "Muy Estricto" to (0..5),
            "Estricto" to (6..10),
            "Estándar" to (11..15),
            "Flexible" to (16..25),
            "Muy Flexible" to (26..60)
        )
    }
    
    /**
     * Valida si una tolerancia es válida sin realizar la actualización.
     * 
     * @param toleranciaMinutos Tolerancia a validar
     * @return true si es válida, false en caso contrario
     */
    fun esToleranciaValida(toleranciaMinutos: Int): Boolean {
        return toleranciaMinutos in TOLERANCIA_MINIMA..TOLERANCIA_MAXIMA
    }
    
    /**
     * Obtiene el rango válido de tolerancia.
     * 
     * @return Par (mínimo, máximo)
     */
    fun obtenerRangoValido(): Pair<Int, Int> {
        return Pair(TOLERANCIA_MINIMA, TOLERANCIA_MAXIMA)
    }
    
    /**
     * Configura el tipo de estrategia de un grupo específico.
     * 
     * ## Validaciones:
     * - El ID del grupo debe ser positivo
     * - El tipo de estrategia debe ser uno de: "PRESENTE", "RETRASO", "FALTA"
     * 
     * ## Patrón Strategy:
     * Al actualizar el tipo de estrategia, el sistema utilizará la estrategia
     * correspondiente para calcular el estado de asistencia en los próximos registros.
     * 
     * @param grupoId ID del grupo a configurar
     * @param tipoEstrategia Tipo de estrategia: "PRESENTE", "RETRASO" o "FALTA"
     * @return ValidationResult.Success si se actualizó correctamente, 
     *         ValidationResult.Error con mensaje si hubo un error
     */
    fun configurarTipoEstrategia(grupoId: Int, tipoEstrategia: String): ValidationResult {
        Log.d(TAG, "=== CONFIGURANDO TIPO DE ESTRATEGIA ===")
        Log.d(TAG, "Grupo ID: $grupoId, Nueva estrategia: $tipoEstrategia")
        
        // Validación 1: ID del grupo debe ser positivo
        if (grupoId <= 0) {
            val mensaje = "ID de grupo inválido: $grupoId"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Validación 2: Tipo de estrategia debe ser válido
        if (tipoEstrategia !in TIPOS_ESTRATEGIA_VALIDOS) {
            val mensaje = "Tipo de estrategia inválido: $tipoEstrategia. Valores válidos: ${TIPOS_ESTRATEGIA_VALIDOS.joinToString(", ")}"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Validación 3: Verificar que el grupo existe
        val grupoExiste = grupoRepository.existeGrupo(grupoId)
        if (!grupoExiste) {
            val mensaje = "El grupo con ID $grupoId no existe"
            Log.e(TAG, mensaje)
            return ValidationResult.Error(mensaje)
        }
        
        // Actualizar tipo de estrategia en la base de datos
        return try {
            grupoRepository.actualizarTipoEstrategia(grupoId, tipoEstrategia)
            Log.d(TAG, "✅ Tipo de estrategia actualizado exitosamente a $tipoEstrategia")
            Log.d(TAG, "El grupo $grupoId ahora utilizará la estrategia $tipoEstrategia para calcular asistencias")
            ValidationResult.Success
        } catch (e: Exception) {
            val mensaje = "Error al actualizar tipo de estrategia: ${e.message}"
            Log.e(TAG, mensaje, e)
            ValidationResult.Error(mensaje)
        }
    }
    
    /**
     * Valida si un tipo de estrategia es válido.
     * 
     * @param tipoEstrategia Tipo de estrategia a validar
     * @return true si es válido, false en caso contrario
     */
    fun esTipoEstrategiaValido(tipoEstrategia: String): Boolean {
        return tipoEstrategia in TIPOS_ESTRATEGIA_VALIDOS
    }
}

