package com.bo.asistenciaapp.data.export.adapter

import com.bo.asistenciaapp.domain.model.Asistencia
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

/**
 * Adapter concreto para exportar asistencias a formato Excel (.xlsx).
 * 
 * ## Patrón Adapter - Rol: Adapter
 * - Adapta la librería Apache POI (Adaptee) a la interface DataExportAdapter (Target)
 * - Convierte objetos Asistencia a formato Excel
 * - Encapsula toda la complejidad de Apache POI
 * 
 * ## Implementación:
 * - Usa Apache POI XSSFWorkbook para archivos .xlsx
 * - Crea una hoja con encabezados formateados
 * - Aplica estilos profesionales (colores, bordes, alineación)
 * - Ajusta automáticamente el ancho de las columnas
 * 
 * ## Dependencias:
 * - Apache POI 5.x (org.apache.poi:poi-ooxml)
 * - Debe agregarse al build.gradle.kts:
 *   implementation("org.apache.poi:poi-ooxml:5.2.3")
 */
class AsistenciaExcelAdapter : DataExportAdapter<Asistencia> {
    
    /**
     * Exporta las asistencias a un archivo Excel con formato profesional.
     * 
     * El archivo generado contiene:
     * - Fila de encabezados con estilo (fondo azul, texto blanco, negrita)
     * - Datos de asistencias con bordes
     * - Columnas auto-ajustadas al contenido
     * - Formato de fecha legible
     * 
     * @param data Lista de asistencias a exportar
     * @param nombreArchivo Nombre base del archivo (se usa para el título de la hoja)
     * @return ByteArray con el contenido del archivo Excel (.xlsx)
     * @throws Exception Si hay error al generar el archivo
     */
    override fun exportar(data: List<Asistencia>, nombreArchivo: String): ByteArray {
        // Crear un nuevo libro de trabajo (workbook) de Excel
        val workbook: Workbook = XSSFWorkbook()
        
        try {
            // Crear una hoja llamada "Asistencias"
            val hoja: Sheet = workbook.createSheet("Asistencias")
            
            // Crear estilos
            val estiloEncabezado = crearEstiloEncabezado(workbook)
            val estiloDatos = crearEstiloDatos(workbook)
            
            // Crear fila de encabezados (fila 0)
            val filaEncabezado = hoja.createRow(0)
            val encabezados = arrayOf("ID", "ID Alumno", "ID Grupo", "Fecha", "Grupo", "Materia")
            
            encabezados.forEachIndexed { indice, titulo ->
                val celda = filaEncabezado.createCell(indice)
                celda.setCellValue(titulo)
                celda.cellStyle = estiloEncabezado
            }
            
            // Agregar los datos de asistencias (a partir de la fila 1)
            data.forEachIndexed { indice, asistencia ->
                val fila = hoja.createRow(indice + 1)
                
                // Columna 0: ID
                val celdaId = fila.createCell(0)
                celdaId.setCellValue(asistencia.id.toDouble())
                celdaId.cellStyle = estiloDatos
                
                // Columna 1: ID Alumno
                val celdaAlumnoId = fila.createCell(1)
                celdaAlumnoId.setCellValue(asistencia.alumnoId.toDouble())
                celdaAlumnoId.cellStyle = estiloDatos
                
                // Columna 2: ID Grupo
                val celdaGrupoId = fila.createCell(2)
                celdaGrupoId.setCellValue(asistencia.grupoId.toDouble())
                celdaGrupoId.cellStyle = estiloDatos
                
                // Columna 3: Fecha
                val celdaFecha = fila.createCell(3)
                celdaFecha.setCellValue(asistencia.fecha)
                celdaFecha.cellStyle = estiloDatos
                
                // Columna 4: Grupo
                val celdaGrupo = fila.createCell(4)
                celdaGrupo.setCellValue(asistencia.grupo)
                celdaGrupo.cellStyle = estiloDatos
                
                // Columna 5: Materia
                val celdaMateria = fila.createCell(5)
                celdaMateria.setCellValue(asistencia.materiaNombre)
                celdaMateria.cellStyle = estiloDatos
            }
            
            // Auto-ajustar el ancho de todas las columnas al contenido
            for (i in encabezados.indices) {
                hoja.autoSizeColumn(i)
                // Agregar un poco de espacio extra (10% más)
                val anchoActual = hoja.getColumnWidth(i)
                hoja.setColumnWidth(i, (anchoActual * 1.1).toInt())
            }
            
            // Convertir el workbook a ByteArray
            val outputStream = ByteArrayOutputStream()
            workbook.write(outputStream)
            return outputStream.toByteArray()
            
        } finally {
            // Cerrar el workbook para liberar recursos
            workbook.close()
        }
    }
    
    /**
     * Crea el estilo para las celdas de encabezado.
     * 
     * Estilo aplicado:
     * - Fondo azul (RGB: 79, 129, 189)
     * - Texto blanco
     * - Negrita
     * - Centrado horizontal y vertical
     * - Bordes en todos los lados
     */
    private fun crearEstiloEncabezado(workbook: Workbook): CellStyle {
        val estilo = workbook.createCellStyle()
        val fuente = workbook.createFont()
        
        // Configurar la fuente
        fuente.bold = true
        fuente.color = IndexedColors.WHITE.index
        fuente.fontHeightInPoints = 11
        
        // Aplicar la fuente al estilo
        estilo.setFont(fuente)
        
        // Configurar el fondo
        estilo.fillForegroundColor = IndexedColors.DARK_BLUE.index
        estilo.fillPattern = FillPatternType.SOLID_FOREGROUND
        
        // Configurar alineación
        estilo.alignment = HorizontalAlignment.CENTER
        estilo.verticalAlignment = VerticalAlignment.CENTER
        
        // Configurar bordes
        estilo.borderTop = BorderStyle.THIN
        estilo.borderBottom = BorderStyle.THIN
        estilo.borderLeft = BorderStyle.THIN
        estilo.borderRight = BorderStyle.THIN
        
        return estilo
    }
    
    /**
     * Crea el estilo para las celdas de datos.
     * 
     * Estilo aplicado:
     * - Texto negro sobre fondo blanco
     * - Alineación centrada
     * - Bordes en todos los lados
     */
    private fun crearEstiloDatos(workbook: Workbook): CellStyle {
        val estilo = workbook.createCellStyle()
        
        // Configurar alineación
        estilo.alignment = HorizontalAlignment.CENTER
        estilo.verticalAlignment = VerticalAlignment.CENTER
        
        // Configurar bordes
        estilo.borderTop = BorderStyle.THIN
        estilo.borderBottom = BorderStyle.THIN
        estilo.borderLeft = BorderStyle.THIN
        estilo.borderRight = BorderStyle.THIN
        
        // Color de bordes
        estilo.topBorderColor = IndexedColors.GREY_50_PERCENT.index
        estilo.bottomBorderColor = IndexedColors.GREY_50_PERCENT.index
        estilo.leftBorderColor = IndexedColors.GREY_50_PERCENT.index
        estilo.rightBorderColor = IndexedColors.GREY_50_PERCENT.index
        
        return estilo
    }
    
    /**
     * Retorna la extensión del archivo Excel moderno.
     */
    override fun obtenerExtension(): String = "xlsx"
    
    /**
     * Retorna el tipo MIME para archivos Excel modernos (.xlsx).
     */
    override fun obtenerTipoMime(): String = 
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    
    /**
     * Retorna el nombre descriptivo del formato.
     */
    override fun obtenerNombreFormato(): String = "Excel"
}

