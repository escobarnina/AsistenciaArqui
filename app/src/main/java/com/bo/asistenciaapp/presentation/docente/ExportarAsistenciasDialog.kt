package com.bo.asistenciaapp.presentation.docente

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bo.asistenciaapp.data.export.adapter.AsistenciaExcelAdapter
import com.bo.asistenciaapp.data.export.adapter.AsistenciaPDFAdapter
import com.bo.asistenciaapp.data.export.adapter.DataExportAdapter
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.model.ExportResult
import com.bo.asistenciaapp.domain.usecase.ExportarAsistenciaCU
import com.bo.asistenciaapp.presentation.common.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Diálogo para exportar asistencias en diferentes formatos.
 * 
 * ## Patrón Adapter - Integración en UI
 * Este componente permite al usuario seleccionar el formato de exportación
 * y delega la generación del archivo al UseCase con el adapter apropiado.
 * 
 * ## Características:
 * - Botones para seleccionar formato (Excel, PDF)
 * - Indicador de progreso durante la exportación
 * - Mensajes de éxito/error
 * - Guarda el archivo automáticamente en Downloads
 * - Compatible con Android 10+ (Scoped Storage)
 * 
 * @param idGrupo ID del grupo del cual exportar las asistencias
 * @param exportarCU Caso de uso de exportación
 * @param onDismiss Callback cuando se cierra el diálogo
 */
@Composable
fun ExportarAsistenciasDialog(
    idGrupo: Int,
    exportarCU: ExportarAsistenciaCU,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Estados
    var exportando by remember { mutableStateOf(false) }
    var formatoSeleccionado by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = { if (!exportando) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ===== TÍTULO =====
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Exportar Asistencias",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // ===== INDICADOR DE PROGRESO =====
                if (exportando) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Exportando a $formatoSeleccionado...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    // ===== BOTONES DE FORMATO =====
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Excel
                        BotonFormato(
                            icono = Icons.Default.TableChart,
                            titulo = "Exportar a Excel",
                            color = Color(0xFF217346),  // Verde Excel
                            onClick = {
                                formatoSeleccionado = "Excel"
                                exportando = true
                                coroutineScope.launch {
                                    exportarAsistencias(
                                        context = context,
                                        idGrupo = idGrupo,
                                        adapter = AsistenciaExcelAdapter(),
                                        exportarCU = exportarCU,
                                        onSuccess = {
                                            exportando = false
                                            onDismiss()
                                        },
                                        onError = { mensaje ->
                                            exportando = false
                                            ToastUtils.mostrarSuperior(context, mensaje)
                                        }
                                    )
                                }
                            }
                        )
                        
                        // Botón PDF
                        BotonFormato(
                            icono = Icons.Default.PictureAsPdf,
                            titulo = "Exportar a PDF",
                            color = Color(0xFFD32F2F),  // Rojo PDF
                            onClick = {
                                formatoSeleccionado = "PDF"
                                exportando = true
                                coroutineScope.launch {
                                    exportarAsistencias(
                                        context = context,
                                        idGrupo = idGrupo,
                                        adapter = AsistenciaPDFAdapter(),
                                        exportarCU = exportarCU,
                                        onSuccess = {
                                            exportando = false
                                            onDismiss()
                                        },
                                        onError = { mensaje ->
                                            exportando = false
                                            ToastUtils.mostrarSuperior(context, mensaje)
                                        }
                                    )
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ===== BOTÓN CANCELAR =====
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

/**
 * Componente reutilizable para cada botón de formato.
 * 
 * Muestra un botón atractivo con icono y título del formato de exportación.
 */
@Composable
private fun BotonFormato(
    icono: ImageVector,
    titulo: String,
    color: Color,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con color específico del formato
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Título
            Text(
                text = titulo,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Flecha indicadora
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Función suspendida que maneja el proceso de exportación.
 * 
 * ## Flujo:
 * 1. Llama al UseCase con el adapter correspondiente
 * 2. Si es exitoso, guarda el archivo en Downloads
 * 3. Muestra un mensaje al usuario
 * 4. Ejecuta los callbacks apropiados
 * 
 * @param context Contexto de Android
 * @param idGrupo ID del grupo a exportar
 * @param adapter Adapter del formato seleccionado
 * @param exportarCU Caso de uso de exportación
 * @param onSuccess Callback ejecutado si la exportación es exitosa
 * @param onError Callback ejecutado si hay un error, recibe el mensaje
 */
private suspend fun exportarAsistencias(
    context: Context,
    idGrupo: Int,
    adapter: DataExportAdapter<Asistencia>,
    exportarCU: ExportarAsistenciaCU,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            // ===== 1. EXPORTAR USANDO EL USECASE =====
            // ⭐ Aquí se aplica el patrón Adapter
            // El diálogo no conoce los detalles del formato, solo pasa el adapter
            val resultado = exportarCU.exportar(idGrupo, adapter)
            
            // ===== 2. MANEJAR EL RESULTADO =====
            withContext(Dispatchers.Main) {
                when (resultado) {
                    is ExportResult.Success -> {
                        // Guardar el archivo
                        val rutaArchivo = guardarArchivo(
                            context = context,
                            datos = resultado.datos,
                            nombreArchivo = resultado.nombreCompleto,
                            tipoMime = resultado.tipoMime
                        )
                        
                        if (rutaArchivo != null) {
                            // Mostrar mensaje de éxito
                            ToastUtils.mostrarSuperior(
                                context,
                                "✓ Archivo exportado exitosamente\n" +
                                "${resultado.nombreCompleto} (${resultado.tamanoFormateado()})\n" +
                                "Ubicación: Downloads"
                            )
                            
                            onSuccess()
                        } else {
                            onError("Error al guardar el archivo")
                        }
                    }
                    
                    is ExportResult.Error -> {
                        // Mostrar mensaje de error
                        onError(resultado.mensaje)
                    }
                }
            }
            
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Error inesperado: ${e.message}")
            }
        }
    }
}

/**
 * Guarda un archivo en la carpeta Downloads del dispositivo.
 * 
 * Compatible con Android 10+ (Scoped Storage) y versiones anteriores.
 * 
 * @param context Contexto de Android
 * @param datos ByteArray con el contenido del archivo
 * @param nombreArchivo Nombre completo del archivo (con extensión)
 * @param tipoMime Tipo MIME del archivo
 * @return Ruta del archivo guardado, o null si hay error
 */
private fun guardarArchivo(
    context: Context,
    datos: ByteArray,
    nombreArchivo: String,
    tipoMime: String
): String? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Usar MediaStore (Scoped Storage)
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Downloads.MIME_TYPE, tipoMime)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(datos)
                    outputStream.flush()
                }
                it.toString()
            }
            
        } else {
            // Android 9 o inferior - Usar File API tradicional
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            
            val archivo = File(downloadsDir, nombreArchivo)
            FileOutputStream(archivo).use { outputStream ->
                outputStream.write(datos)
                outputStream.flush()
            }
            
            archivo.absolutePath
        }
        
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Versión simplificada del diálogo para uso rápido.
 * 
 * Función de conveniencia que crea el estado necesario internamente.
 */
@Composable
fun ExportarAsistenciasButton(
    idGrupo: Int,
    exportarCU: ExportarAsistenciaCU,
    modifier: Modifier = Modifier
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    
    // Botón para abrir el diálogo
    Button(
        onClick = { mostrarDialogo = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Exportar Asistencias")
    }
    
    // Mostrar diálogo cuando se requiera
    if (mostrarDialogo) {
        ExportarAsistenciasDialog(
            idGrupo = idGrupo,
            exportarCU = exportarCU,
            onDismiss = { mostrarDialogo = false }
        )
    }
}

