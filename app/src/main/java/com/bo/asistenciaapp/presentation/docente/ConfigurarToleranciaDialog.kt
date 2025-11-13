package com.bo.asistenciaapp.presentation.docente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.usecase.ConfigurarGrupoCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.launch

/**
 * Diálogo para configurar la tolerancia de un grupo.
 * 
 * ## Patrón Strategy - Configuración UI:
 * Este componente permite a los docentes configurar la tolerancia que utilizarán
 * las estrategias de asistencia para calcular el estado (PRESENTE/RETRASO/FALTA).
 * 
 * ## Características:
 * - Validación en tiempo real (0-60 minutos)
 * - Muestra tolerancia actual del grupo
 * - Slider para selección rápida
 * - Input numérico para precisión
 * - Indicador de nivel de política (Estricto/Estándar/Flexible)
 * - Feedback visual del resultado
 * 
 * @param grupo Grupo a configurar
 * @param configurarGrupoCU Caso de uso para actualizar tolerancia
 * @param onDismiss Callback cuando se cierra el diálogo
 * @param onSuccess Callback cuando se actualiza exitosamente (opcional)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarToleranciaDialog(
    grupo: Grupo,
    configurarGrupoCU: ConfigurarGrupoCU,
    onDismiss: () -> Unit,
    onSuccess: (() -> Unit)? = null
) {
    var toleranciaActual by remember { mutableStateOf(grupo.toleranciaMinutos) }
    var toleranciaInput by remember { mutableStateOf(grupo.toleranciaMinutos.toString()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Obtener rango válido del Use Case
    val (minTolerancia, maxTolerancia) = configurarGrupoCU.obtenerRangoValido()
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Configurar Tolerancia",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column {
                Text(
                    text = "Configurar Tolerancia",
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información actual
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Tolerancia Actual",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${grupo.toleranciaMinutos} minutos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Slider para selección rápida
                Column {
                    Text(
                        text = "Nueva Tolerancia: $toleranciaActual minutos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = toleranciaActual.toFloat(),
                        onValueChange = { 
                            toleranciaActual = it.toInt()
                            toleranciaInput = toleranciaActual.toString()
                        },
                        valueRange = minTolerancia.toFloat()..maxTolerancia.toFloat(),
                        steps = maxTolerancia - minTolerancia - 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Indicadores de rango
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$minTolerancia min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$maxTolerancia min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Input numérico manual
                OutlinedTextField(
                    value = toleranciaInput,
                    onValueChange = { newValue ->
                        toleranciaInput = newValue
                        // Validar y actualizar el slider
                        newValue.toIntOrNull()?.let { valor ->
                            if (valor in minTolerancia..maxTolerancia) {
                                toleranciaActual = valor
                                errorMessage = null
                            } else {
                                errorMessage = "Debe estar entre $minTolerancia y $maxTolerancia"
                            }
                        }
                    },
                    label = { Text("Minutos (manual)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = if (errorMessage != null) {
                        { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Indicador de nivel de política
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = obtenerColorPolitica(toleranciaActual)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = obtenerNivelPolitica(toleranciaActual),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = obtenerDescripcionPolitica(toleranciaActual),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Información adicional
                Text(
                    text = "💡 Las estrategias de asistencia usarán esta tolerancia para determinar si un estudiante está presente, llegó tarde o faltó.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Mensaje de éxito
                if (showSuccessMessage) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = "✅ Tolerancia actualizada exitosamente",
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
                    if (!configurarGrupoCU.esToleranciaValida(toleranciaActual)) {
                        errorMessage = "Tolerancia inválida"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    scope.launch {
                        val resultado = configurarGrupoCU.configurarTolerancia(
                            grupoId = grupo.id,
                            toleranciaMinutos = toleranciaActual
                        )
                        
                        isLoading = false
                        
                        when (resultado) {
                            is ValidationResult.Success -> {
                                showSuccessMessage = true
                                snackbarHostState.showSnackbar(
                                    message = "Tolerancia actualizada a $toleranciaActual minutos",
                                    duration = SnackbarDuration.Short
                                )
                                onSuccess?.invoke()
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
 * Obtiene el nivel de política según la tolerancia.
 */
private fun obtenerNivelPolitica(tolerancia: Int): String {
    return when (tolerancia) {
        in 0..5 -> "🔴 Muy Estricto"
        in 6..10 -> "🟠 Estricto"
        in 11..15 -> "🟡 Estándar"
        in 16..25 -> "🟢 Flexible"
        else -> "🔵 Muy Flexible"
    }
}

/**
 * Obtiene la descripción de la política según la tolerancia.
 */
private fun obtenerDescripcionPolitica(tolerancia: Int): String {
    return when (tolerancia) {
        in 0..5 -> "Puntualidad estricta"
        in 6..10 -> "Política estándar institucional"
        in 11..15 -> "Permite pequeños retrasos"
        in 16..25 -> "Política permisiva"
        else -> "Máxima flexibilidad"
    }
}

/**
 * Obtiene el color de fondo según la política.
 */
@Composable
private fun obtenerColorPolitica(tolerancia: Int): androidx.compose.ui.graphics.Color {
    return when (tolerancia) {
        in 0..5 -> MaterialTheme.colorScheme.errorContainer
        in 6..10 -> MaterialTheme.colorScheme.tertiaryContainer
        in 11..15 -> MaterialTheme.colorScheme.secondaryContainer
        in 16..25 -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

