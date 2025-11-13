package com.bo.asistenciaapp.data.export.adapter

import android.util.Log
import com.bo.asistenciaapp.domain.model.Asistencia
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

/**
 * Adapter concreto para exportar asistencias a formato Excel (.xlsx).
 * 
 * ## Patrón Adapter - Rol: Adapter
 * - Adapta la librería Apache POI (Adaptee) a la interface DataExportAdapter (Target)
 * - Convierte objetos Asistencia a formato Excel
 * - Versión SIMPLIFICADA sin estilos para mayor estabilidad
 * 
 * ## Implementación:
 * - Usa Apache POI XSSFWorkbook para archivos .xlsx
 * - Sin estilos (solo datos puros)
 * - Try-catch completo para manejo de errores
 * - Logs detallados con Log.d()
 * 
 * ## Dependencias:
 * - Apache POI 5.2.3 (org.apache.poi:poi-ooxml)
 * - Todas las dependencias transitivas incluidas en build.gradle.kts
 */
class AsistenciaExcelAdapter : DataExportAdapter<Asistencia> {
    
    companion object {
        private const val TAG = "AsistenciaExcelAdapter"
    }
    
    /**
     * Exporta las asistencias a un archivo Excel SIMPLE.
     * 
     * SIN estilos, SIN formato, solo datos puros para evitar errores.
     * 
     * @param data Lista de asistencias a exportar
     * @param nombreArchivo Nombre base del archivo
     * @return ByteArray con el contenido del archivo Excel (.xlsx)
     */
    override fun exportar(data: List<Asistencia>, nombreArchivo: String): ByteArray {
        Log.d(TAG, "=== INICIO EXPORTACIÓN EXCEL ===")
        Log.d(TAG, "Nombre archivo: $nombreArchivo")
        Log.d(TAG, "Cantidad de asistencias: ${data.size}")
        
        var workbook: XSSFWorkbook? = null
        var outputStream: ByteArrayOutputStream? = null
        
        try {
            // 1. Crear workbook
            Log.d(TAG, "Creando XSSFWorkbook...")
            workbook = XSSFWorkbook()
            Log.d(TAG, "XSSFWorkbook creado exitosamente")
            
            // 2. Crear hoja
            Log.d(TAG, "Creando hoja 'Asistencias'...")
            val hoja = workbook.createSheet("Asistencias")
            Log.d(TAG, "Hoja creada exitosamente")
            
            // 3. Crear encabezados (fila 0) - SIN ESTILOS
            Log.d(TAG, "Creando fila de encabezados...")
            val filaEncabezado = hoja.createRow(0)
            val encabezados = arrayOf("ID", "ID_Alumno", "ID_Grupo", "Fecha", "Grupo", "Materia")
            
            encabezados.forEachIndexed { indice, titulo ->
                try {
                    val celda = filaEncabezado.createCell(indice)
                    celda.setCellValue(titulo)
                    Log.d(TAG, "Encabezado $indice: $titulo")
                } catch (e: Exception) {
                    Log.e(TAG, "Error en encabezado $indice: ${e.message}")
                    throw e
                }
            }
            Log.d(TAG, "Encabezados creados exitosamente")
            
            // 4. Agregar datos (a partir de la fila 1) - SIN ESTILOS
            Log.d(TAG, "Agregando ${data.size} filas de datos...")
            data.forEachIndexed { indice, asistencia ->
                try {
                    val fila = hoja.createRow(indice + 1)
                    
                    // Columna 0: ID
                    fila.createCell(0).setCellValue(asistencia.id.toDouble())
                    
                    // Columna 1: ID Alumno
                    fila.createCell(1).setCellValue(asistencia.alumnoId.toDouble())
                    
                    // Columna 2: ID Grupo
                    fila.createCell(2).setCellValue(asistencia.grupoId.toDouble())
                    
                    // Columna 3: Fecha
                    fila.createCell(3).setCellValue(asistencia.fecha)
                    
                    // Columna 4: Grupo
                    fila.createCell(4).setCellValue(asistencia.grupo)
                    
                    // Columna 5: Materia
                    fila.createCell(5).setCellValue(asistencia.materiaNombre)
                    
                    if ((indice + 1) % 10 == 0) {
                        Log.d(TAG, "Procesadas ${indice + 1} filas...")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error en fila ${indice + 1}: ${e.message}", e)
                    throw e
                }
            }
            Log.d(TAG, "Todas las filas agregadas exitosamente")
            
            // 5. Convertir a ByteArray
            Log.d(TAG, "Convirtiendo workbook a ByteArray...")
            outputStream = ByteArrayOutputStream()
            workbook.write(outputStream)
            val bytes = outputStream.toByteArray()
            Log.d(TAG, "Conversión exitosa. Tamaño: ${bytes.size} bytes")
            
            Log.d(TAG, "=== EXPORTACIÓN EXCEL EXITOSA ===")
            return bytes
            
        } catch (e: Exception) {
            Log.e(TAG, "=== ERROR EN EXPORTACIÓN EXCEL ===")
            Log.e(TAG, "Tipo de error: ${e.javaClass.simpleName}")
            Log.e(TAG, "Mensaje: ${e.message}")
            Log.e(TAG, "Stack trace:", e)
            throw Exception("Error al exportar a Excel: ${e.message}", e)
            
        } finally {
            // Cerrar recursos en el orden correcto
            try {
                outputStream?.close()
                Log.d(TAG, "OutputStream cerrado")
            } catch (e: Exception) {
                Log.e(TAG, "Error cerrando OutputStream: ${e.message}")
            }
            
            try {
                workbook?.close()
                Log.d(TAG, "Workbook cerrado")
            } catch (e: Exception) {
                Log.e(TAG, "Error cerrando Workbook: ${e.message}")
            }
        }
    }
    
    /**
     * Retorna la extensión del archivo Excel moderno.
     */
    override fun obtenerExtension(): String {
        Log.d(TAG, "obtenerExtension() llamado: xlsx")
        return "xlsx"
    }
    
    /**
     * Retorna el tipo MIME para archivos Excel modernos (.xlsx).
     */
    override fun obtenerTipoMime(): String {
        val mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        Log.d(TAG, "obtenerTipoMime() llamado: $mime")
        return mime
    }
    
    /**
     * Retorna el nombre descriptivo del formato.
     */
    override fun obtenerNombreFormato(): String {
        Log.d(TAG, "obtenerNombreFormato() llamado: Excel")
        return "Excel"
    }
}

