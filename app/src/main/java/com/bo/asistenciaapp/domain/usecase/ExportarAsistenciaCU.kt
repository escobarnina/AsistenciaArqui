package com.bo.asistenciaapp.domain.usecase

import android.util.Log
import com.bo.asistenciaapp.data.export.adapter.DataExportAdapter
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.model.ExportResult

/**
 * Caso de Uso para exportar asistencias en diferentes formatos.
 * 
 * ## Patrón Adapter - Rol: Client
 * Este UseCase es el **cliente** en el patrón Adapter:
 * - NO conoce los detalles de implementación específicos (Excel, PDF)
 * - Solo conoce la interface Target (DataExportAdapter)
 * - Delega la exportación al adapter sin saber cuál es
 * - Permite agregar nuevos formatos sin modificar este código
 * 
 * ## Responsabilidades:
 * - Orquestar el proceso de exportación
 * - Obtener los datos del repository
 * - Validar que haya datos para exportar
 * - Delegar la generación del archivo al adapter
 * - Manejar errores y encapsularlos en ExportResult
 * - Registrar logs para debugging
 * 
 * ## Principios SOLID aplicados:
 * - **S (Single Responsibility)**: Solo se encarga de la lógica de exportación
 * - **O (Open/Closed)**: Abierto a extensión (nuevos adapters), cerrado a modificación
 * - **D (Dependency Inversion)**: Depende de abstracción (interface), no de implementaciones
 * 
 * @property asistenciaRepository Repositorio para obtener los datos de asistencias
 */
class ExportarAsistenciaCU(
    private val asistenciaRepository: AsistenciaRepository
) {
    
    companion object {
        private const val TAG = "ExportarAsistenciaCU"
    }
    
    /**
     * Exporta las asistencias de un grupo usando el adapter especificado.
     * 
     * Este método implementa el patrón Adapter desde la perspectiva del cliente:
     * - Recibe cualquier implementación de DataExportAdapter
     * - No conoce ni le importa el formato específico
     * - Simplemente delega la exportación al adapter
     * 
     * ## Flujo de ejecución:
     * 1. Validar el ID del grupo
     * 2. Obtener las asistencias del repository
     * 3. Validar que haya datos para exportar
     * 4. Generar el nombre del archivo
     * 5. Delegar la exportación al adapter (aquí ocurre la "adaptación")
     * 6. Encapsular el resultado exitoso
     * 7. Manejar cualquier error que ocurra
     * 
     * @param idGrupo ID del grupo del cual exportar las asistencias
     * @param adapter Implementación del adapter (puede ser Excel, PDF, CSV, etc.)
     * @return ExportResult.Success con los datos generados o ExportResult.Error si falla
     * 
     * ## Ejemplo de uso:
     * ```kotlin
     * // Exportar a Excel - el UseCase NO sabe que es Excel
     * val resultadoExcel = exportarAsistenciaCU.exportar(1, AsistenciaExcelAdapter())
     * 
     * // Exportar a PDF - el UseCase NO sabe que es PDF
     * val resultadoPDF = exportarAsistenciaCU.exportar(1, AsistenciaPDFAdapter())
     * 
     * // El código del UseCase es IDÉNTICO para ambos casos
     * ```
     */
    fun exportar(
        idGrupo: Int,
        adapter: DataExportAdapter<Asistencia>  // ⭐ Solo conocemos la interface, no la implementación
    ): ExportResult {
        
        Log.d(TAG, "Iniciando exportación de asistencias para grupo ID: $idGrupo")
        Log.d(TAG, "Formato de exportación: ${adapter.obtenerNombreFormato()}")
        
        return try {
            // ===== 1. VALIDAR ID DEL GRUPO =====
            if (idGrupo <= 0) {
                Log.e(TAG, "ID de grupo inválido: $idGrupo")
                return ExportResult.Error(
                    mensaje = "El ID del grupo debe ser mayor a cero",
                    codigoError = ExportResult.ErrorCode.DESCONOCIDO
                )
            }
            
            // ===== 2. OBTENER DATOS DEL REPOSITORY =====
            Log.d(TAG, "Obteniendo asistencias del repository...")
            val asistencias = asistenciaRepository.obtenerPorGrupo(idGrupo)
            Log.d(TAG, "Se obtuvieron ${asistencias.size} asistencias")
            
            // ===== 3. VALIDAR QUE HAYA DATOS =====
            if (asistencias.isEmpty()) {
                Log.w(TAG, "No hay asistencias para exportar en el grupo $idGrupo")
                return ExportResult.Error(
                    mensaje = "No hay asistencias registradas para este grupo",
                    codigoError = ExportResult.ErrorCode.SIN_DATOS
                )
            }
            
            // ===== 4. GENERAR NOMBRE DEL ARCHIVO =====
            val nombreArchivo = generarNombreArchivo(idGrupo)
            Log.d(TAG, "Nombre del archivo: $nombreArchivo")
            
            // ===== 5. DELEGAR EXPORTACIÓN AL ADAPTER =====
            // ⭐ AQUÍ OCURRE LA MAGIA DEL PATRÓN ADAPTER
            // El UseCase llama a exportar() sin saber si está generando Excel, PDF, CSV, etc.
            // El adapter se encarga de "adaptar" los datos de Asistencia al formato específico
            Log.d(TAG, "Generando archivo con el adapter...")
            val datosExportados = adapter.exportar(asistencias, nombreArchivo)
            Log.d(TAG, "Archivo generado correctamente. Tamaño: ${datosExportados.size} bytes")
            
            // ===== 6. ENCAPSULAR RESULTADO EXITOSO =====
            val resultado = ExportResult.Success(
                datos = datosExportados,
                nombreArchivo = nombreArchivo,
                extension = adapter.obtenerExtension(),
                tipoMime = adapter.obtenerTipoMime(),
                formato = adapter.obtenerNombreFormato(),
                cantidadRegistros = asistencias.size
            )
            
            Log.i(TAG, "Exportación exitosa: ${resultado.nombreCompleto} (${resultado.tamanoFormateado()})")
            resultado
            
        } catch (e: Exception) {
            // ===== 7. MANEJAR ERRORES =====
            Log.e(TAG, "Error durante la exportación", e)
            
            // Clasificar el tipo de error
            val codigoError = when {
                e.message?.contains("database", ignoreCase = true) == true -> 
                    ExportResult.ErrorCode.ERROR_BD
                    
                e.message?.contains("memory", ignoreCase = true) == true ||
                e.message?.contains("space", ignoreCase = true) == true -> 
                    ExportResult.ErrorCode.ALMACENAMIENTO_INSUFICIENTE
                    
                e.message?.contains("permission", ignoreCase = true) == true -> 
                    ExportResult.ErrorCode.PERMISOS_INSUFICIENTES
                    
                else -> 
                    ExportResult.ErrorCode.ERROR_GENERACION
            }
            
            ExportResult.Error(
                mensaje = "Error al exportar las asistencias: ${e.message ?: "Error desconocido"}",
                causa = e,
                codigoError = codigoError
            )
        }
    }
    
    /**
     * Exporta las asistencias de un alumno específico en un grupo.
     * 
     * Variante del método principal que filtra por alumno.
     * 
     * @param idAlumno ID del alumno
     * @param idGrupo ID del grupo
     * @param adapter Adapter para el formato de exportación
     * @return ExportResult con el resultado de la operación
     */
    fun exportarPorAlumno(
        idAlumno: Int,
        idGrupo: Int,
        adapter: DataExportAdapter<Asistencia>
    ): ExportResult {
        
        Log.d(TAG, "Exportando asistencias del alumno $idAlumno en grupo $idGrupo")
        
        return try {
            // Validaciones
            if (idAlumno <= 0 || idGrupo <= 0) {
                return ExportResult.Error(
                    mensaje = "IDs inválidos",
                    codigoError = ExportResult.ErrorCode.DESCONOCIDO
                )
            }
            
            // Obtener asistencias filtradas por alumno y grupo
            val asistencias = asistenciaRepository.obtenerPorAlumnoYGrupo(idAlumno, idGrupo)
            
            if (asistencias.isEmpty()) {
                return ExportResult.Error(
                    mensaje = "No hay asistencias registradas para este alumno en el grupo",
                    codigoError = ExportResult.ErrorCode.SIN_DATOS
                )
            }
            
            // Generar nombre del archivo con ID del alumno
            val nombreArchivo = "asistencias_alumno_${idAlumno}_grupo_${idGrupo}"
            
            // Exportar usando el adapter
            val datosExportados = adapter.exportar(asistencias, nombreArchivo)
            
            // Retornar resultado exitoso
            ExportResult.Success(
                datos = datosExportados,
                nombreArchivo = nombreArchivo,
                extension = adapter.obtenerExtension(),
                tipoMime = adapter.obtenerTipoMime(),
                formato = adapter.obtenerNombreFormato(),
                cantidadRegistros = asistencias.size
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al exportar asistencias del alumno", e)
            ExportResult.Error(
                mensaje = "Error al exportar: ${e.message}",
                causa = e,
                codigoError = ExportResult.ErrorCode.ERROR_GENERACION
            )
        }
    }
    
    /**
     * Obtiene la lista de formatos disponibles.
     * 
     * Este método demuestra cómo el UseCase puede trabajar con múltiples adapters
     * sin conocer sus detalles de implementación.
     * 
     * @param adapters Lista de adapters disponibles
     * @return Lista de nombres de formatos
     */
    fun obtenerFormatosDisponibles(
        adapters: List<DataExportAdapter<Asistencia>>
    ): List<String> {
        return adapters.map { it.obtenerNombreFormato() }
    }
    
    /**
     * Valida si un grupo tiene asistencias para exportar.
     * 
     * Útil para habilitar/deshabilitar botones de exportación en la UI.
     * 
     * @param idGrupo ID del grupo a validar
     * @return true si hay asistencias, false si no hay
     */
    fun tieneAsistenciasParaExportar(idGrupo: Int): Boolean {
        return try {
            val asistencias = asistenciaRepository.obtenerPorGrupo(idGrupo)
            asistencias.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error al validar asistencias", e)
            false
        }
    }
    
    /**
     * Genera un nombre de archivo descriptivo basado en el ID del grupo.
     * 
     * Formato: asistencias_grupo_{id}
     * 
     * @param idGrupo ID del grupo
     * @return Nombre base del archivo (sin extensión)
     */
    private fun generarNombreArchivo(idGrupo: Int): String {
        return "asistencias_grupo_$idGrupo"
    }
}

