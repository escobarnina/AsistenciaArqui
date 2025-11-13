package com.bo.asistenciaapp.presentation.alumno

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.domain.strategy.attendance.EstrategiaPresente
import com.bo.asistenciaapp.domain.strategy.attendance.EstrategiaRetraso
import com.bo.asistenciaapp.domain.strategy.attendance.EstrategiaFalta
import com.bo.asistenciaapp.domain.usecase.MarcarAsistenciaTestCU
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de PRUEBA del Patrón Strategy para marcar asistencia.
 * 
 * ## 🎯 OBJETIVO:
 * Permitir a los alumnos probar diferentes estrategias de asistencia
 * en MODO TESTING (sin restricciones de horario).
 * 
 * ## 📐 PATRÓN STRATEGY - DEMOSTRACIÓN:
 * Esta pantalla permite:
 * - Seleccionar una estrategia (Presente, Retraso, Falta)
 * - Simular diferentes horas de marcado y de inicio
 * - Ver el resultado calculado por la estrategia
 * - Demostrar visualmente cómo funciona el patrón Strategy
 * 
 * @param onBack Callback para volver atrás
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarcarAsistenciaTestScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val session = remember { UserSession(context) }
    val alumnoId = session.getUserId()
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val asistenciaRepository = remember { AsistenciaRepository(database) }
    val marcarAsistenciaTestCU = remember { MarcarAsistenciaTestCU(asistenciaRepository) }
    
    // Estado del formulario
    var grupoIdStr by remember { mutableStateOf("1") }
    var estrategiaSeleccionada by remember { mutableStateOf("Retraso") }
    var horaMarcado by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("08:00") }
    var isLoading by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<com.bo.asistenciaapp.domain.usecase.ResultadoAsistenciaTest?>(null) }
    var mostrarResultado by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Obtener hora actual
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
        horaMarcado = horaActual
    }
    
    UserLayout(
        title = "🧪 Prueba del Strategy",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado explicativo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Modo de Prueba - Patrón Strategy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Este modo permite probar cómo diferentes estrategias calculan el estado de asistencia sin restricciones de horario.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Selector de estrategia
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1️⃣ Selecciona una Estrategia",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = estrategiaSeleccionada == "Presente",
                            onClick = { estrategiaSeleccionada = "Presente" },
                            label = { Text("Presente") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = estrategiaSeleccionada == "Retraso",
                            onClick = { estrategiaSeleccionada = "Retraso" },
                            label = { Text("Retraso") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = estrategiaSeleccionada == "Falta",
                            onClick = { estrategiaSeleccionada = "Falta" },
                            label = { Text("Falta") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text(
                        text = when (estrategiaSeleccionada) {
                            "Presente" -> "✅ Política flexible: Siempre marca PRESENTE"
                            "Retraso" -> "⏱️ Política estándar: Evalúa tiempo de llegada"
                            "Falta" -> "❌ Política estricta: Similar a Retraso"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Configuración de horarios
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2️⃣ Configura los Horarios",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = grupoIdStr,
                        onValueChange = { grupoIdStr = it.filter { char -> char.isDigit() } },
                        label = { Text("ID del Grupo") },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Hora de Inicio de Clase (HH:mm)") },
                        placeholder = { Text("08:00") },
                        leadingIcon = {
                            Icon(Icons.Default.Start, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = horaMarcado,
                        onValueChange = { horaMarcado = it },
                        label = { Text("Hora de Marcado (HH:mm)") },
                        placeholder = { Text("08:15") },
                        leadingIcon = {
                            Icon(Icons.Default.Timer, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Botones rápidos
                    Text(
                        text = "Pruebas Rápidas:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                horaInicio = "08:00"
                                horaMarcado = "08:05"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("A Tiempo\n(5 min)", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = {
                                horaInicio = "08:00"
                                horaMarcado = "08:15"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retraso\n(15 min)", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = {
                                horaInicio = "08:00"
                                horaMarcado = "08:45"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tarde\n(45 min)", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            
            // Botón de marcar asistencia
            Button(
                onClick = {
                    if (grupoIdStr.isEmpty() || horaInicio.isEmpty() || horaMarcado.isEmpty()) {
                        Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val grupoId = grupoIdStr.toIntOrNull() ?: return@Button
                    
                    isLoading = true
                    coroutineScope.launch {
                        // Establecer estrategia seleccionada
                        val estrategia = when (estrategiaSeleccionada) {
                            "Presente" -> EstrategiaPresente()
                            "Retraso" -> EstrategiaRetraso()
                            "Falta" -> EstrategiaFalta()
                            else -> EstrategiaRetraso()
                        }
                        marcarAsistenciaTestCU.setEstrategia(estrategia)
                        
                        // Marcar asistencia
                        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val res = marcarAsistenciaTestCU.marcarAsistenciaTest(
                            alumnoId = alumnoId,
                            grupoId = grupoId,
                            fecha = fechaActual,
                            horaMarcado = horaMarcado,
                            horaInicio = horaInicio
                        )
                        
                        isLoading = false
                        resultado = res
                        mostrarResultado = true
                        
                        if (!res.exito) {
                            Toast.makeText(context, res.mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isLoading && grupoIdStr.isNotEmpty() && horaInicio.isNotEmpty() && horaMarcado.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar Asistencia en Modo TEST", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            // Mostrar resultado
            if (mostrarResultado && resultado != null) {
                ResultadoAsistenciaCard(resultado!!)
            }
        }
    }
}

/**
 * Card que muestra el resultado detallado del marcado de asistencia.
 * Evidencia visual del Patrón Strategy.
 */
@Composable
private fun ResultadoAsistenciaCard(resultado: com.bo.asistenciaapp.domain.usecase.ResultadoAsistenciaTest) {
    val colorEstado = when (resultado.estado) {
        "PRESENTE" -> Color(0xFF4CAF50) // Verde
        "RETRASO" -> Color(0xFFFF9800) // Naranja
        "FALTA" -> Color(0xFFF44336) // Rojo
        else -> MaterialTheme.colorScheme.outline
    }
    
    val iconEstado = when (resultado.estado) {
        "PRESENTE" -> Icons.Default.CheckCircle
        "RETRASO" -> Icons.Default.Schedule
        "FALTA" -> Icons.Default.Cancel
        else -> Icons.Default.Info
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (resultado.exito) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado del resultado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (resultado.exito) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (resultado.exito) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = if (resultado.exito) "✅ Asistencia Registrada" else "❌ Error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = resultado.mensaje,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (resultado.exito && resultado.estado != null) {
                HorizontalDivider()
                
                // Estado calculado por la estrategia
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorEstado.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = iconEstado,
                                contentDescription = null,
                                tint = colorEstado,
                                modifier = Modifier.size(48.dp)
                            )
                            Column {
                                Text(
                                    text = "Estado Calculado:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = resultado.estado,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorEstado
                                )
                            }
                        }
                    }
                }
                
                // Detalles del cálculo
                Text(
                    text = "📐 Detalles del Patrón Strategy:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetalleItem(
                        label = "Estrategia Utilizada:",
                        valor = resultado.estrategiaUsada ?: "N/A"
                    )
                    DetalleItem(
                        label = "Tolerancia del Grupo:",
                        valor = "${resultado.toleranciaMinutos} minutos"
                    )
                    DetalleItem(
                        label = "Diferencia de Tiempo:",
                        valor = "${resultado.diferencia} minutos"
                    )
                }
                
                // Explicación del cálculo
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
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ℹ️ Explicación:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = when (resultado.estado) {
                                "PRESENTE" -> "La estrategia ${resultado.estrategiaUsada} determinó que el estudiante llegó a tiempo (dentro de los ${resultado.toleranciaMinutos} minutos de tolerancia)."
                                "RETRASO" -> "La estrategia ${resultado.estrategiaUsada} determinó que el estudiante llegó con retraso (${resultado.diferencia} minutos de diferencia, superó la tolerancia de ${resultado.toleranciaMinutos} min pero no el límite de falta)."
                                "FALTA" -> "La estrategia ${resultado.estrategiaUsada} determinó que el estudiante llegó muy tarde (${resultado.diferencia} minutos de diferencia, superó el límite permitido)."
                                else -> "Estado indeterminado."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleItem(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

