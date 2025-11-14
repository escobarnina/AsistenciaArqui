package com.bo.asistenciaapp.presentation.docente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.data.repository.HorarioRepository
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.usecase.ConfigurarGrupoCU
import com.bo.asistenciaapp.domain.usecase.ConfigurarHorarioCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.launch

/**
 * Diálogo para editar la configuración de un grupo.
 * 
 * Permite al docente configurar:
 * - Tipo de estrategia de asistencia (PRESENTE, RETRASO, FALTA)
 * - Horario de inicio del grupo (primer horario configurado)
 * 
 * ## Patrón Strategy - Configuración UI:
 * Este componente permite a los docentes configurar qué estrategia utilizará
 * el grupo para calcular el estado de asistencia, haciendo el sistema completamente
 * configurable desde la interfaz de usuario.
 * 
 * @param grupo Grupo a editar
 * @param configurarGrupoCU Caso de uso para actualizar configuración del grupo
 * @param configurarHorarioCU Caso de uso para gestionar horarios
 * @param onDismiss Callback cuando se cierra el diálogo
 * @param onSuccess Callback cuando se actualiza exitosamente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarGrupoDialog(
    grupo: Grupo,
    configurarGrupoCU: ConfigurarGrupoCU,
    configurarHorarioCU: ConfigurarHorarioCU,
    onDismiss: () -> Unit,
    onSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estado del formulario
    var tipoEstrategiaSeleccionado by remember { mutableStateOf(grupo.tipoEstrategia) }
    var horariosGrupo by remember { mutableStateOf(configurarHorarioCU.obtenerHorariosGrupo(grupo.id)) }
    var horaInicioActual by remember { mutableStateOf(obtenerHoraInicio(horariosGrupo)) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // Actualizar hora de inicio cuando cambian los horarios
    LaunchedEffect(horariosGrupo) {
        horaInicioActual = obtenerHoraInicio(horariosGrupo)
    }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar Grupo",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column {
                Text(
                    text = "Editar Grupo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${grupo.materiaNombre} - Grupo ${grupo.grupo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección: Tipo de Estrategia
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tipo de Estrategia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Selector de tipo de estrategia
                        val tiposEstrategia = remember { ConfigurarGrupoCU.TIPOS_ESTRATEGIA_VALIDOS }
                        var expandedEstrategia by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedEstrategia,
                            onExpandedChange = { expandedEstrategia = it }
                        ) {
                            OutlinedTextField(
                                value = obtenerNombreEstrategia(tipoEstrategiaSeleccionado),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Estrategia Actual") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstrategia) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expandedEstrategia,
                                onDismissRequest = { expandedEstrategia = false }
                            ) {
                                tiposEstrategia.forEachIndexed { index, estrategia ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = obtenerNombreEstrategia(estrategia),
                                                fontWeight = if (estrategia == tipoEstrategiaSeleccionado) FontWeight.SemiBold else FontWeight.Normal,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = obtenerIconoEstrategia(estrategia),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = obtenerColorIconoEstrategia(estrategia)
                                            )
                                        },
                                        onClick = {
                                            tipoEstrategiaSeleccionado = estrategia
                                            expandedEstrategia = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Información breve sobre la estrategia seleccionada
                        Surface(
                            color = obtenerColorEstrategia(tipoEstrategiaSeleccionado),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = obtenerDescripcionEstrategia(tipoEstrategiaSeleccionado),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Sección: Horario de Inicio
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Horario de Inicio",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        if (horaInicioActual.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Hora de Inicio Actual",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = horaInicioActual,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            
                            if (horariosGrupo.isNotEmpty()) {
                                Text(
                                    text = horariosGrupo.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "No hay horarios configurados. Usa el botón de horarios para agregar uno.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Mensaje de error
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                // Mensaje de éxito
                if (showSuccessMessage) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = "✅ Configuración actualizada exitosamente",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!configurarGrupoCU.esTipoEstrategiaValido(tipoEstrategiaSeleccionado)) {
                        errorMessage = "Tipo de estrategia inválido"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    scope.launch {
                        val resultado = configurarGrupoCU.configurarTipoEstrategia(
                            grupoId = grupo.id,
                            tipoEstrategia = tipoEstrategiaSeleccionado
                        )
                        
                        isLoading = false
                        
                        when (resultado) {
                            is ValidationResult.Success -> {
                                showSuccessMessage = true
                                snackbarHostState.showSnackbar(
                                    message = "Estrategia actualizada a ${obtenerNombreEstrategia(tipoEstrategiaSeleccionado)}",
                                    duration = SnackbarDuration.Short
                                )
                                onSuccess?.invoke()
                                // Recargar horarios
                                horariosGrupo = configurarHorarioCU.obtenerHorariosGrupo(grupo.id)
                                // Cerrar después de un breve delay
                                kotlinx.coroutines.delay(800)
                                onDismiss()
                            }
                            is ValidationResult.Error -> {
                                errorMessage = resultado.message
                                snackbarHostState.showSnackbar(
                                    message = "Error: ${resultado.message}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    }
                },
                enabled = !isLoading && errorMessage == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
    
    // Snackbar Host para mensajes
    SnackbarHost(hostState = snackbarHostState)
}

/**
 * Obtiene el nombre amigable de una estrategia.
 */
private fun obtenerNombreEstrategia(tipoEstrategia: String): String {
    return when (tipoEstrategia) {
        "PRESENTE" -> "Presente"
        "RETRASO" -> "Retraso"
        "FALTA" -> "Falta"
        else -> tipoEstrategia
    }
}

/**
 * Obtiene la descripción corta de una estrategia.
 */
private fun obtenerDescripcionEstrategia(tipoEstrategia: String): String {
    return when (tipoEstrategia) {
        "PRESENTE" -> "Siempre marca como presente"
        "RETRASO" -> "Marca retraso si llega después del horario"
        "FALTA" -> "Marca falta si llega después del horario + tolerancia"
        else -> "Estrategia desconocida"
    }
}

/**
 * Obtiene el icono asociado a una estrategia.
 */
private fun obtenerIconoEstrategia(tipoEstrategia: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (tipoEstrategia) {
        "PRESENTE" -> Icons.Default.CheckCircle
        "RETRASO" -> Icons.Default.Schedule
        "FALTA" -> Icons.Default.Close
        else -> Icons.Default.Info
    }
}

/**
 * Obtiene el color del icono de una estrategia.
 */
@Composable
private fun obtenerColorIconoEstrategia(tipoEstrategia: String): androidx.compose.ui.graphics.Color {
    return when (tipoEstrategia) {
        "PRESENTE" -> MaterialTheme.colorScheme.primary
        "RETRASO" -> MaterialTheme.colorScheme.tertiary
        "FALTA" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * Obtiene el color asociado a una estrategia.
 */
@Composable
private fun obtenerColorEstrategia(tipoEstrategia: String): androidx.compose.ui.graphics.Color {
    return when (tipoEstrategia) {
        "PRESENTE" -> MaterialTheme.colorScheme.primaryContainer
        "RETRASO" -> MaterialTheme.colorScheme.tertiaryContainer
        "FALTA" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

/**
 * Obtiene la hora de inicio del primer horario configurado.
 */
private fun obtenerHoraInicio(horarios: List<String>): String {
    if (horarios.isEmpty()) return ""
    // Los horarios están en formato "Día HH:mm-HH:mm"
    // Extraemos la primera hora
    return horarios.firstOrNull()?.let { horario ->
        val partes = horario.split(" ")
        if (partes.size >= 2) {
            partes[1].split("-").firstOrNull() ?: ""
        } else {
            ""
        }
    } ?: ""
}

