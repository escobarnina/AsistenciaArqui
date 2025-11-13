package com.bo.asistenciaapp.domain.model

/**
 * Sealed class que representa el resultado de una operación de exportación.
 * 
 * ## Patrón Result/Either
 * Encapsula el resultado de una operación que puede fallar,
 * evitando excepciones y haciendo explícito el manejo de errores.
 * 
 * ## Casos de uso:
 * - **Success**: La exportación se completó correctamente
 * - **Error**: Ocurrió un error durante la exportación
 * 
 * ## Ventajas:
 * - Hace explícito el manejo de errores en el tipo
 * - Evita null checks y excepciones no controladas
 * - Facilita el testing
 * - Pattern matching exhaustivo con when
 * 
 * ## Ejemplo de uso:
 * ```kotlin
 * when (val resultado = exportarAsistenciaCU.exportar(idGrupo, adapter)) {
 *     is ExportResult.Success -> {
 *         // Guardar o compartir el archivo
 *         guardarArchivo(resultado.datos, resultado.nombreCompleto)
 *     }
 *     is ExportResult.Error -> {
 *         // Mostrar error al usuario
 *         mostrarError(resultado.mensaje)
 *     }
 * }
 * ```
 */
sealed class ExportResult {
    
    /**
     * Resultado exitoso de una exportación.
     * 
     * Contiene todos los datos necesarios para guardar o compartir el archivo generado.
     * 
     * @property datos ByteArray con el contenido del archivo generado
     * @property nombreArchivo Nombre base del archivo (sin extensión)
     * @property extension Extensión del archivo (ej: "xlsx", "pdf")
     * @property tipoMime Tipo MIME del archivo para compartir o guardar correctamente
     * @property formato Nombre descriptivo del formato (ej: "Excel", "PDF")
     * @property cantidadRegistros Cantidad de registros exportados
     */
    data class Success(
        val datos: ByteArray,
        val nombreArchivo: String,
        val extension: String,
        val tipoMime: String,
        val formato: String,
        val cantidadRegistros: Int
    ) : ExportResult() {
        
        /**
         * Obtiene el nombre completo del archivo con extensión.
         * 
         * @return Nombre completo (ej: "asistencias_grupo_1.xlsx")
         */
        val nombreCompleto: String
            get() = "$nombreArchivo.$extension"
        
        /**
         * Obtiene el tamaño del archivo en bytes.
         * 
         * @return Tamaño en bytes
         */
        val tamanoBytes: Int
            get() = datos.size
        
        /**
         * Obtiene el tamaño del archivo en KB (formateado).
         * 
         * @return Tamaño legible (ej: "45.2 KB")
         */
        fun tamanoFormateado(): String {
            val kb = tamanoBytes / 1024.0
            return String.format("%.2f KB", kb)
        }
        
        /**
         * Override de equals para comparar correctamente ByteArray.
         * 
         * Nota: ByteArray usa comparación de referencia por defecto,
         * necesitamos comparar contenido.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as Success
            
            if (!datos.contentEquals(other.datos)) return false
            if (nombreArchivo != other.nombreArchivo) return false
            if (extension != other.extension) return false
            if (tipoMime != other.tipoMime) return false
            if (formato != other.formato) return false
            if (cantidadRegistros != other.cantidadRegistros) return false
            
            return true
        }
        
        /**
         * Override de hashCode para ser consistente con equals.
         */
        override fun hashCode(): Int {
            var result = datos.contentHashCode()
            result = 31 * result + nombreArchivo.hashCode()
            result = 31 * result + extension.hashCode()
            result = 31 * result + tipoMime.hashCode()
            result = 31 * result + formato.hashCode()
            result = 31 * result + cantidadRegistros
            return result
        }
    }
    
    /**
     * Resultado de error en una exportación.
     * 
     * Contiene información sobre el error ocurrido para mostrarlo al usuario
     * o registrarlo en logs.
     * 
     * @property mensaje Mensaje descriptivo del error
     * @property causa Excepción original que causó el error (opcional)
     * @property codigoError Código de error para categorización (opcional)
     */
    data class Error(
        val mensaje: String,
        val causa: Throwable? = null,
        val codigoError: ErrorCode = ErrorCode.DESCONOCIDO
    ) : ExportResult() {
        
        /**
         * Obtiene el mensaje completo incluyendo la causa si existe.
         * 
         * @return Mensaje detallado del error
         */
        fun mensajeCompleto(): String {
            return if (causa != null) {
                "$mensaje\nCausa: ${causa.message}"
            } else {
                mensaje
            }
        }
        
        /**
         * Verifica si el error es recuperable.
         * 
         * @return true si el usuario puede reintentar la operación
         */
        fun esRecuperable(): Boolean {
            return codigoError in listOf(
                ErrorCode.RED_NO_DISPONIBLE,
                ErrorCode.ALMACENAMIENTO_INSUFICIENTE,
                ErrorCode.TIMEOUT
            )
        }
    }
    
    /**
     * Códigos de error para categorización.
     * 
     * Permite manejar diferentes tipos de errores de forma específica.
     */
    enum class ErrorCode {
        /** Error desconocido o no categorizado */
        DESCONOCIDO,
        
        /** No hay datos para exportar */
        SIN_DATOS,
        
        /** Error al acceder a la base de datos */
        ERROR_BD,
        
        /** Error al generar el archivo */
        ERROR_GENERACION,
        
        /** No hay espacio suficiente en el almacenamiento */
        ALMACENAMIENTO_INSUFICIENTE,
        
        /** No hay conexión de red (para exportación a la nube) */
        RED_NO_DISPONIBLE,
        
        /** Operación cancelada por timeout */
        TIMEOUT,
        
        /** Permisos insuficientes para guardar el archivo */
        PERMISOS_INSUFICIENTES
    }
}

