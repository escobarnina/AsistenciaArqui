package com.bo.asistenciaapp.presentation.docente

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
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
    var adapterSeleccionado by remember { mutableStateOf<DataExportAdapter<Asistencia>?>(null) }
    
    // Verificar si se necesitan permisos (solo para Android 9 y anteriores)
    val necesitaPermisos = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    
    // Función para verificar permisos dinámicamente
    fun tienePermisos(): Boolean {
        return if (necesitaPermisos) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 10+ no necesita permisos para MediaStore.Downloads
        }
    }
    
    // Launcher para solicitar permisos
    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        if (esConcedido) {
            // Permiso concedido, proceder con la exportación
            adapterSeleccionado?.let { adapter ->
                formatoSeleccionado = when (adapter) {
                    is AsistenciaExcelAdapter -> "Excel"
                    is AsistenciaPDFAdapter -> "PDF"
                    else -> "Archivo"
                }
                exportando = true
                coroutineScope.launch {
                    exportarAsistencias(
                        context = context,
                        idGrupo = idGrupo,
                        adapter = adapter,
                        exportarCU = exportarCU,
                        onSuccess = {
                            exportando = false
                            adapterSeleccionado = null
                            onDismiss()
                        },
                        onError = { mensaje ->
                            exportando = false
                            adapterSeleccionado = null
                            ToastUtils.mostrarSuperior(context, mensaje)
                        }
                    )
                }
            }
        } else {
            // Permiso denegado
            ToastUtils.mostrarSuperior(
                context,
                "Se necesita permiso de almacenamiento para exportar archivos"
            )
            adapterSeleccionado = null
        }
    }
    
    // Función para iniciar exportación (con verificación de permisos)
    fun iniciarExportacion(adapter: DataExportAdapter<Asistencia>) {
        if (necesitaPermisos && !tienePermisos()) {
            // Guardar el adapter para usarlo después de obtener permisos
            adapterSeleccionado = adapter
            // Solicitar permiso
            launcherPermisos.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            // Ya tiene permisos o no los necesita, proceder directamente
            formatoSeleccionado = when (adapter) {
                is AsistenciaExcelAdapter -> "Excel"
                is AsistenciaPDFAdapter -> "PDF"
                else -> "Archivo"
            }
            exportando = true
            coroutineScope.launch {
                exportarAsistencias(
                    context = context,
                    idGrupo = idGrupo,
                    adapter = adapter,
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
    }
    
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
                                iniciarExportacion(AsistenciaExcelAdapter())
                            }
                        )
                        
                        // Botón PDF
                        BotonFormato(
                            icono = Icons.Default.PictureAsPdf,
                            titulo = "Exportar a PDF",
                            color = Color(0xFFD32F2F),  // Rojo PDF
                            onClick = {
                                iniciarExportacion(AsistenciaPDFAdapter())
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
                            val mensajeExito = "✓ Archivo exportado exitosamente\n" +
                                "${resultado.nombreCompleto} (${resultado.tamanoFormateado()})\n" +
                                "Guardado en: Descargas"
                            
                            Log.d("ExportarAsistencias", "Mensaje de éxito: $mensajeExito")
                            Log.d("ExportarAsistencias", "URI del archivo: $rutaArchivo")
                            
                            // Mostrar Toast con mensaje claro
                            ToastUtils.mostrarSuperior(
                                context,
                                mensajeExito,
                                Toast.LENGTH_LONG
                            )
                            
                            // Abrir el explorador de archivos en la ubicación del archivo
                            try {
                                abrirExploradorArchivos(context, rutaArchivo, resultado.nombreCompleto)
                            } catch (e: Exception) {
                                Log.e("ExportarAsistencias", "Error al abrir explorador: ${e.message}", e)
                            }
                            
                            onSuccess()
                        } else {
                            Log.e("ExportarAsistencias", "Error: rutaArchivo es null")
                            onError("Error al guardar el archivo. Verifica los permisos de almacenamiento.")
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
            // NO se necesitan permisos para escribir en MediaStore.Downloads
            Log.d("ExportarAsistencias", "Guardando archivo en Android 10+: $nombreArchivo")
            
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Downloads.MIME_TYPE, tipoMime)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            if (uri == null) {
                Log.e("ExportarAsistencias", "Error: No se pudo crear el URI para el archivo")
                return null
            }
            
            Log.d("ExportarAsistencias", "URI creado: $uri")
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(datos)
                outputStream.flush()
                Log.d("ExportarAsistencias", "Archivo escrito exitosamente: ${datos.size} bytes")
            } ?: run {
                Log.e("ExportarAsistencias", "Error: No se pudo abrir OutputStream")
                return null
            }
            
            uri.toString()
            
        } else {
            // Android 9 o inferior - Usar File API tradicional
            // Requiere permiso WRITE_EXTERNAL_STORAGE
            Log.d("ExportarAsistencias", "Guardando archivo en Android 9 o inferior: $nombreArchivo")
            
            // Verificar permiso antes de escribir
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("ExportarAsistencias", "Error: No se tiene permiso de escritura")
                return null
            }
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            
            // Crear directorio si no existe
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val archivo = File(downloadsDir, nombreArchivo)
            FileOutputStream(archivo).use { outputStream ->
                outputStream.write(datos)
                outputStream.flush()
                Log.d("ExportarAsistencias", "Archivo escrito exitosamente: ${archivo.absolutePath}")
            }
            
            archivo.absolutePath
        }
        
    } catch (e: Exception) {
        Log.e("ExportarAsistencias", "Error al guardar archivo: ${e.message}", e)
        e.printStackTrace()
        null
    }
}

/**
 * Abre el explorador de archivos del dispositivo en la ubicación del archivo descargado.
 * 
 * Intenta abrir el archivo directamente, y si no es posible, abre la carpeta Downloads.
 * 
 * @param context Contexto de Android
 * @param uriArchivo URI del archivo guardado (puede ser content:// o file://)
 * @param nombreArchivo Nombre del archivo para mostrar en el intent
 */
private fun abrirExploradorArchivos(
    context: Context,
    uriArchivo: String,
    nombreArchivo: String
) {
    try {
        val uri = Uri.parse(uriArchivo)
        
        // Intentar abrir el archivo directamente con ACTION_VIEW
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(nombreArchivo))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // Verificar si hay una app que pueda manejar este intent
        if (intent.resolveActivity(context.packageManager) != null) {
            try {
                context.startActivity(intent)
                Log.d("ExportarAsistencias", "Explorador de archivos abierto con el archivo")
                return
            } catch (e: Exception) {
                Log.w("ExportarAsistencias", "No se pudo abrir el archivo directamente: ${e.message}")
            }
        }
        
        // Si no se pudo abrir el archivo, intentar abrir la carpeta Downloads
        // Método 1: Intentar con ACTION_GET_CONTENT para abrir el explorador de archivos
        val intentExplorador = Intent(Intent.ACTION_GET_CONTENT).apply {
            setType("*/*")
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        if (intentExplorador.resolveActivity(context.packageManager) != null) {
            try {
                // Intentar abrir con un chooser para que el usuario seleccione el explorador
                val chooser = Intent.createChooser(intentExplorador, "Abrir explorador de archivos")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                Log.d("ExportarAsistencias", "Explorador de archivos abierto con chooser")
                return
            } catch (e: Exception) {
                Log.w("ExportarAsistencias", "No se pudo abrir con chooser: ${e.message}")
            }
        }
        
        // Método 2: Intentar abrir directamente la carpeta Downloads (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val intentDownloads = Intent(Intent.ACTION_VIEW).apply {
                    setData(downloadsUri)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                if (intentDownloads.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intentDownloads)
                    Log.d("ExportarAsistencias", "Carpeta Downloads abierta (Android 10+)")
                    return
                }
            } catch (e: Exception) {
                Log.w("ExportarAsistencias", "No se pudo abrir Downloads (Android 10+): ${e.message}")
            }
        } else {
            // Método 3: Android 9 o inferior - Intentar con File URI
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                
                if (downloadsDir.exists()) {
                    val fileUri = Uri.fromFile(downloadsDir)
                    val intentDownloads = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(fileUri, "resource/folder")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    
                    if (intentDownloads.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intentDownloads)
                        Log.d("ExportarAsistencias", "Carpeta Downloads abierta (Android 9-)")
                        return
                    }
                }
            } catch (e: Exception) {
                Log.w("ExportarAsistencias", "No se pudo abrir Downloads (Android 9-): ${e.message}")
            }
        }
        
        // Si todo falla, mostrar un mensaje informativo
        Log.w("ExportarAsistencias", "No se pudo abrir el explorador de archivos")
        ToastUtils.mostrarInferior(
            context,
            "Archivo guardado en Descargas. Abre el explorador de archivos manualmente.",
            Toast.LENGTH_SHORT
        )
        
    } catch (e: Exception) {
        Log.e("ExportarAsistencias", "Error al abrir explorador: ${e.message}", e)
        ToastUtils.mostrarInferior(
            context,
            "Archivo guardado en Descargas",
            Toast.LENGTH_SHORT
        )
    }
}

/**
 * Obtiene el tipo MIME basado en la extensión del archivo.
 * 
 * @param nombreArchivo Nombre del archivo con extensión
 * @return Tipo MIME correspondiente
 */
private fun getMimeType(nombreArchivo: String): String {
    return when {
        nombreArchivo.endsWith(".xlsx", ignoreCase = true) -> 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        nombreArchivo.endsWith(".pdf", ignoreCase = true) -> 
            "application/pdf"
        nombreArchivo.endsWith(".xls", ignoreCase = true) -> 
            "application/vnd.ms-excel"
        nombreArchivo.endsWith(".csv", ignoreCase = true) -> 
            "text/csv"
        else -> "*/*"
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

