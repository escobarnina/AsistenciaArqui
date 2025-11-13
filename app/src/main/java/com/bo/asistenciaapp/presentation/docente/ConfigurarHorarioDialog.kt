package com.bo.asistenciaapp.presentation.docente

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.usecase.ConfigurarHorarioCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.launch

/**
 * Diálogo para configurar horarios de clase de un grupo.
 * 
 * ## PROPÓSITO:
 * Permite al docente definir los horarios de clase (día y hora)
 * para que el sistema pueda validar cuándo se puede marcar asistencia.
 * 
 * @param grupo Grupo al que se le configurarán horarios
 * @param configurarHorarioCU Caso de uso para gestionar horarios
 * @param onDismiss Callback al cerrar el diálogo
 * @param onSuccess Callback al configurar exitosamente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarHorarioDialog(
    grupo: Grupo,
    configurarHorarioCU: ConfigurarHorarioCU,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado del formulario
    var diaSeleccionado by remember { mutableStateOf(ConfigurarHorarioCU.DIAS_VALIDOS.first()) }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var horariosActuales by remember { mutableStateOf(configurarHorarioCU.obtenerHorariosGrupo(grupo.id)) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Sugerencias de hora
    val horasSugeridas = listOf(
        "07:00", "08:00", "09:00", "10:00", "11:00", "12:00",
        "13:00", "14:00", "15:00", "16:00", "17:00", "18:00",
        "19:00", "20:00", "21:00"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configurar Horarios",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${grupo.materiaNombre} - Grupo ${grupo.grupo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Horarios actuales
                if (horariosActuales.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📅 Horarios Configurados:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            horariosActuales.forEach { horario ->
                                Text(
                                    text = "• $horario",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val result = configurarHorarioCU.limpiarHorarios(grupo.id)
                                if (result.isValid) {
                                    horariosActuales = emptyList()
                                    Toast.makeText(context, "Horarios limpiados", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar Horarios")
                    }
                    
                    HorizontalDivider()
                }
                
                // Formulario nuevo horario
                Text(
                    text = "Agregar Nuevo Horario:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Selector de día
                var expandedDia by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedDia,
                    onExpandedChange = { expandedDia = it }
                ) {
                    OutlinedTextField(
                        value = diaSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día de la Semana") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDia) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedDia,
                        onDismissRequest = { expandedDia = false }
                    ) {
                        ConfigurarHorarioCU.DIAS_VALIDOS.forEach { dia ->
                            DropdownMenuItem(
                                text = { Text(dia) },
                                onClick = {
                                    diaSeleccionado = dia
                                    expandedDia = false
                                }
                            )
                        }
                    }
                }
                
                // Hora de inicio (selector)
                var expandedHoraInicio by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedHoraInicio,
                    onExpandedChange = { expandedHoraInicio = it }
                ) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Hora de Inicio (HH:mm)") },
                        placeholder = { Text("08:00") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHoraInicio) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedHoraInicio,
                        onDismissRequest = { expandedHoraInicio = false }
                    ) {
                        horasSugeridas.forEach { hora ->
                            DropdownMenuItem(
                                text = { Text(hora) },
                                onClick = {
                                    horaInicio = hora
                                    expandedHoraInicio = false
                                }
                            )
                        }
                    }
                }
                
                // Hora de fin (selector)
                var expandedHoraFin by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedHoraFin,
                    onExpandedChange = { expandedHoraFin = it }
                ) {
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { horaFin = it },
                        label = { Text("Hora de Fin (HH:mm)") },
                        placeholder = { Text("10:00") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHoraFin) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedHoraFin,
                        onDismissRequest = { expandedHoraFin = false }
                    ) {
                        horasSugeridas.forEach { hora ->
                            DropdownMenuItem(
                                text = { Text(hora) },
                                onClick = {
                                    horaFin = hora
                                    expandedHoraFin = false
                                }
                            )
                        }
                    }
                }
                
                // Mensaje de error
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (horaInicio.isEmpty() || horaFin.isEmpty()) {
                        errorMessage = "Debe completar todas las horas"
                        return@Button
                    }
                    
                    if (!isLoading) {
                        isLoading = true
                        coroutineScope.launch {
                            val result = configurarHorarioCU.configurarHorario(
                                grupoId = grupo.id,
                                dia = diaSeleccionado,
                                horaInicio = horaInicio,
                                horaFin = horaFin
                            )
                            isLoading = false
                            when (result) {
                                is ValidationResult.Success -> {
                                    Toast.makeText(
                                        context,
                                        "Horario agregado: $diaSeleccionado $horaInicio-$horaFin",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Recargar horarios
                                    horariosActuales = configurarHorarioCU.obtenerHorariosGrupo(grupo.id)
                                    // Limpiar formulario
                                    horaInicio = ""
                                    horaFin = ""
                                    errorMessage = null
                                    onSuccess()
                                }
                                is ValidationResult.Error -> {
                                    errorMessage = result.errorMessage
                                    snackbarHostState.showSnackbar(
                                        message = result.errorMessage ?: "Error desconocido",
                                        actionLabel = "Cerrar",
                                        duration = SnackbarDuration.Short
                                    )
                                    Log.e("ConfigurarHorarioDialog", "Error: ${result.errorMessage}")
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading && horaInicio.isNotEmpty() && horaFin.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f)
    )
    
    SnackbarHost(hostState = snackbarHostState)
}

