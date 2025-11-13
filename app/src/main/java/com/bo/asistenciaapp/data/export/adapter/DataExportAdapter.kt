package com.bo.asistenciaapp.data.export.adapter

import com.bo.asistenciaapp.domain.model.Asistencia

/**
 * Interface del Patrón Adapter para exportación de datos.
 * 
 * Define el contrato que deben cumplir todos los adaptadores de exportación,
 * permitiendo exportar datos en diferentes formatos sin que el cliente
 * (UseCase) conozca los detalles de implementación específicos.
 * 
 * ## Patrón Adapter
 * - **Target**: Esta interface (lo que el cliente espera)
 * - **Adaptee**: Las librerías específicas (Apache POI, PdfDocument)
 * - **Adapter**: Las implementaciones concretas (AsistenciaExcelAdapter, AsistenciaPDFAdapter)
 * - **Client**: ExportarAsistenciaCU (no conoce el tipo específico de adapter)
 * 
 * ## Responsabilidades:
 * - Definir métodos comunes para exportación
 * - Abstraer los detalles de formato específico
 * - Permitir agregar nuevos formatos sin modificar código existente
 * 
 * @param T Tipo de datos a exportar (en este caso, Asistencia)
 */
interface DataExportAdapter<T> {
    
    /**
     * Exporta una lista de datos al formato específico.
     * 
     * Este método adapta los datos del dominio al formato de salida específico,
     * encapsulando toda la lógica de conversión y generación del archivo.
     * 
     * @param data Lista de datos a exportar
     * @param nombreArchivo Nombre base del archivo (sin extensión)
     * @return ByteArray con el contenido del archivo generado
     * @throws Exception Si ocurre un error durante la exportación
     */
    fun exportar(data: List<T>, nombreArchivo: String): ByteArray
    
    /**
     * Obtiene la extensión del archivo para este formato.
     * 
     * @return Extensión sin el punto (ej: "xlsx", "pdf", "csv")
     */
    fun obtenerExtension(): String
    
    /**
     * Obtiene el tipo MIME del formato de exportación.
     * 
     * Útil para compartir el archivo o especificar el Content-Type.
     * 
     * @return Tipo MIME (ej: "application/vnd.ms-excel", "application/pdf")
     */
    fun obtenerTipoMime(): String
    
    /**
     * Obtiene el nombre descriptivo del formato.
     * 
     * Se usa para mostrar en la interfaz de usuario.
     * 
     * @return Nombre del formato (ej: "Excel", "PDF", "CSV")
     */
    fun obtenerNombreFormato(): String
}

