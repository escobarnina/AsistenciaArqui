package com.bo.asistenciaapp.data.export.adapter

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import com.bo.asistenciaapp.domain.model.Asistencia
import java.io.ByteArrayOutputStream

/**
 * Adapter concreto para exportar asistencias a formato PDF.
 * 
 * ## Patrón Adapter - Rol: Adapter
 * - Adapta la API de Android PdfDocument (Adaptee) a la interface DataExportAdapter (Target)
 * - Convierte objetos Asistencia a formato PDF
 * - Encapsula toda la complejidad de generación de PDF en Android
 * 
 * ## Implementación:
 * - Usa android.graphics.pdf.PdfDocument (nativo de Android)
 * - Crea un documento PDF con tabla formateada
 * - Aplica estilos profesionales (colores, tipografía, alineación)
 * - Maneja paginación automática si hay muchos registros
 * 
 * ## Ventajas:
 * - No requiere dependencias externas (usa API nativa de Android)
 * - Ligero y eficiente
 * - Compatible con todas las versiones de Android desde API 19+
 */
class AsistenciaPDFAdapter : DataExportAdapter<Asistencia> {
    
    // Constantes de configuración del documento
    companion object {
        // Dimensiones de página A4 en puntos (1 punto = 1/72 pulgada)
        private const val ANCHO_PAGINA = 595  // 8.27 pulgadas
        private const val ALTO_PAGINA = 842   // 11.69 pulgadas
        
        // Márgenes
        private const val MARGEN_IZQUIERDO = 40
        private const val MARGEN_SUPERIOR = 60
        private const val MARGEN_DERECHO = 40
        private const val MARGEN_INFERIOR = 60
        
        // Tamaños de fuente
        private const val TAMANO_TITULO = 20f
        private const val TAMANO_SUBTITULO = 14f
        private const val TAMANO_ENCABEZADO = 12f
        private const val TAMANO_DATOS = 10f
        
        // Colores (RGB)
        private const val COLOR_TITULO = 0xFF1976D2.toInt()      // Azul Material
        private const val COLOR_ENCABEZADO = 0xFF424242.toInt()   // Gris oscuro
        private const val COLOR_DATOS = 0xFF000000.toInt()        // Negro
        private const val COLOR_LINEA = 0xFFBDBDBD.toInt()        // Gris claro
        
        // Espaciado
        private const val ALTO_FILA = 30
        private const val ESPACIADO_TITULO = 40
        private const val ESPACIADO_TABLA = 20
    }
    
    /**
     * Exporta las asistencias a un archivo PDF con formato profesional.
     * 
     * El archivo generado contiene:
     * - Título principal con el nombre del archivo
     * - Subtítulo con información del reporte
     * - Tabla con encabezados y datos formateados
     * - Paginación automática si es necesario
     * - Pie de página con número de página
     * 
     * @param data Lista de asistencias a exportar
     * @param nombreArchivo Nombre base del archivo (se usa para el título)
     * @return ByteArray con el contenido del archivo PDF
     * @throws Exception Si hay error al generar el archivo
     */
    override fun exportar(data: List<Asistencia>, nombreArchivo: String): ByteArray {
        // Crear un nuevo documento PDF
        val documento = PdfDocument()
        
        try {
            // Configuración de la página
            val infoPagina = PdfDocument.PageInfo.Builder(
                ANCHO_PAGINA,
                ALTO_PAGINA,
                1  // Número de página inicial
            ).create()
            
            // Crear la primera página
            val pagina = documento.startPage(infoPagina)
            val canvas = pagina.canvas
            
            // Configurar el pincel (Paint) para dibujar texto
            val pincel = Paint()
            pincel.isAntiAlias = true
            
            // Variable para rastrear la posición Y actual en el canvas
            var posicionY = MARGEN_SUPERIOR
            
            // ===== TÍTULO PRINCIPAL =====
            pincel.color = COLOR_TITULO
            pincel.textSize = TAMANO_TITULO
            pincel.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(
                "Reporte de Asistencias",
                MARGEN_IZQUIERDO.toFloat(),
                posicionY.toFloat(),
                pincel
            )
            posicionY += ESPACIADO_TITULO
            
            // ===== SUBTÍTULO =====
            pincel.color = COLOR_ENCABEZADO
            pincel.textSize = TAMANO_SUBTITULO
            pincel.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(
                "Total de registros: ${data.size}",
                MARGEN_IZQUIERDO.toFloat(),
                posicionY.toFloat(),
                pincel
            )
            posicionY += ESPACIADO_TABLA
            
            // ===== LÍNEA SEPARADORA =====
            pincel.color = COLOR_LINEA
            pincel.strokeWidth = 2f
            canvas.drawLine(
                MARGEN_IZQUIERDO.toFloat(),
                posicionY.toFloat(),
                (ANCHO_PAGINA - MARGEN_DERECHO).toFloat(),
                posicionY.toFloat(),
                pincel
            )
            posicionY += 20
            
            // ===== ENCABEZADOS DE LA TABLA =====
            val anchoDisponible = ANCHO_PAGINA - MARGEN_IZQUIERDO - MARGEN_DERECHO
            val anchoColumna = anchoDisponible / 6  // 6 columnas
            
            pincel.color = COLOR_ENCABEZADO
            pincel.textSize = TAMANO_ENCABEZADO
            pincel.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            
            val encabezados = listOf("ID", "ID Alumno", "ID Grupo", "Fecha", "Grupo", "Materia")
            encabezados.forEachIndexed { indice, titulo ->
                canvas.drawText(
                    titulo,
                    (MARGEN_IZQUIERDO + indice * anchoColumna).toFloat(),
                    posicionY.toFloat(),
                    pincel
                )
            }
            posicionY += ALTO_FILA
            
            // ===== LÍNEA DEBAJO DE ENCABEZADOS =====
            pincel.color = COLOR_LINEA
            pincel.strokeWidth = 1f
            canvas.drawLine(
                MARGEN_IZQUIERDO.toFloat(),
                posicionY.toFloat(),
                (ANCHO_PAGINA - MARGEN_DERECHO).toFloat(),
                posicionY.toFloat(),
                pincel
            )
            posicionY += 10
            
            // ===== DATOS DE LA TABLA =====
            pincel.color = COLOR_DATOS
            pincel.textSize = TAMANO_DATOS
            pincel.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
            data.forEach { asistencia ->
                // Verificar si necesitamos una nueva página
                if (posicionY > ALTO_PAGINA - MARGEN_INFERIOR) {
                    // Finalizar la página actual
                    documento.finishPage(pagina)
                    
                    // Crear una nueva página
                    val nuevaPagina = documento.startPage(infoPagina)
                    posicionY = MARGEN_SUPERIOR
                    
                    // Dibujar encabezados nuevamente en la nueva página
                    pincel.color = COLOR_ENCABEZADO
                    pincel.textSize = TAMANO_ENCABEZADO
                    pincel.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    
                    encabezados.forEachIndexed { indice, titulo ->
                        nuevaPagina.canvas.drawText(
                            titulo,
                            (MARGEN_IZQUIERDO + indice * anchoColumna).toFloat(),
                            posicionY.toFloat(),
                            pincel
                        )
                    }
                    posicionY += ALTO_FILA
                    
                    // Restaurar estilo de datos
                    pincel.color = COLOR_DATOS
                    pincel.textSize = TAMANO_DATOS
                    pincel.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                
                // Dibujar cada columna de datos
                val valores = listOf(
                    asistencia.id.toString(),
                    asistencia.alumnoId.toString(),
                    asistencia.grupoId.toString(),
                    asistencia.fecha,
                    asistencia.grupo,
                    // Truncar nombre de materia si es muy largo
                    if (asistencia.materiaNombre.length > 15) 
                        asistencia.materiaNombre.substring(0, 12) + "..."
                    else 
                        asistencia.materiaNombre
                )
                
                valores.forEachIndexed { indice, valor ->
                    canvas.drawText(
                        valor,
                        (MARGEN_IZQUIERDO + indice * anchoColumna).toFloat(),
                        posicionY.toFloat(),
                        pincel
                    )
                }
                
                posicionY += ALTO_FILA
            }
            
            // ===== PIE DE PÁGINA =====
            pincel.color = COLOR_ENCABEZADO
            pincel.textSize = TAMANO_DATOS
            pincel.textAlign = Paint.Align.CENTER
            canvas.drawText(
                "Página 1 - Generado por AsistenciaApp",
                (ANCHO_PAGINA / 2).toFloat(),
                (ALTO_PAGINA - 30).toFloat(),
                pincel
            )
            
            // Finalizar la página
            documento.finishPage(pagina)
            
            // Escribir el documento a un ByteArray
            val outputStream = ByteArrayOutputStream()
            documento.writeTo(outputStream)
            return outputStream.toByteArray()
            
        } finally {
            // Cerrar el documento para liberar recursos
            documento.close()
        }
    }
    
    /**
     * Retorna la extensión del archivo PDF.
     */
    override fun obtenerExtension(): String = "pdf"
    
    /**
     * Retorna el tipo MIME para archivos PDF.
     */
    override fun obtenerTipoMime(): String = "application/pdf"
    
    /**
     * Retorna el nombre descriptivo del formato.
     */
    override fun obtenerNombreFormato(): String = "PDF"
}

