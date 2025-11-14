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
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.HorarioRepository
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Horario
import com.bo.asistenciaapp.domain.usecase.ConfigurarGrupoCU
import com.bo.asistenciaapp.domain.usecase.ConfigurarHorarioCU
import com.bo.asistenciaapp.presentation.common.ToastUtils
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch

/**
 * Pantalla para configurar horarios y nivel de tolerancia de un grupo.
 * 
 * Permite configurar:
 * - Horarios de lunes a viernes (hora inicio y fin)
 * - Nivel de tolerancia (Estricto, Flexible, Muy Flexible)
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 */
@Composable
fun ConfigurarHorariosScreen(
    grupoId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val grupoRepository = remember { GrupoRepository(database) }
    val horarioRepository = remember { HorarioRepository(database) }
    val configurarGrupoCU = remember { ConfigurarGrupoCU(grupoRepository) }
    val configurarHorarioCU = remember { ConfigurarHorarioCU(horarioRepository) }
    
    val scope = rememberCoroutineScope()
    
    // Estados
    var grupo by remember { mutableStateOf<Grupo?>(null) }
    val horarios = remember { mutableStateMapOf<String, Pair<String, String>>() }
    var nivelTolerancia by remember { mutableStateOf("RETRASO") } // Default: Flexible
    
    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    
    // Cargar datos del grupo y horarios existentes
    LaunchedEffect(grupoId) {
        try {
            // Obtener grupo por ID
            grupo = grupoRepository.obtenerPorId(grupoId)
            grupo?.tipoEstrategia?.let { nivelTolerancia = it }
            
            // Obtener horarios existentes
            val horariosExistentes = horarioRepository.obtenerPorGrupo(grupoId)
            horariosExistentes.forEach { horario ->
                if (horario.dia in diasSemana) {
                    horarios[horario.dia] = Pair(horario.horaInicio, horario.horaFin)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        UserLayout(
            title = grupo?.let { "${it.materiaNombre} - ${it.grupo}" } ?: "Configurar Horarios",
            showBackButton = true,
            onBack = onBack
        ) { paddingValues ->
            ConfigurarHorariosContent(
                paddingValues = paddingValues,
                grupo = grupo,
                horarios = horarios,
                nivelTolerancia = nivelTolerancia,
                diasSemana = diasSemana,
                onHorarioChange = { dia, inicio, fin ->
                    horarios[dia] = Pair(inicio, fin)
                },
                onNivelToleranciaChange = { nivelTolerancia = it },
                onGuardar = {
                    scope.launch {
                        try {
                            // Guardar nivel de tolerancia (estrategia)
                            grupo?.let {
                                configurarGrupoCU.configurarTipoEstrategia(grupoId, nivelTolerancia)
                            }
                            
                            // Eliminar horarios existentes del grupo
                            horarioRepository.eliminarPorGrupo(grupoId)
                            
                            // Guardar nuevos horarios
                            horarios.forEach { (dia, horas) ->
                                if (horas.first.isNotEmpty() && horas.second.isNotEmpty()) {
                                    val result = configurarHorarioCU.configurarHorario(
                                        grupoId, dia, horas.first, horas.second
                                    )
                                    if (result is com.bo.asistenciaapp.domain.utils.ValidationResult.Error) {
                                        ToastUtils.mostrarSuperior(context, "Error en $dia: ${result.message}")
                                        return@launch
                                    }
                                }
                            }
                            
                            ToastUtils.mostrarSuperior(context, "Horarios guardados exitosamente")
                            onBack()
                        } catch (e: Exception) {
                            ToastUtils.mostrarSuperior(context, "Error: ${e.message ?: "Error desconocido"}")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ConfigurarHorariosContent(
    paddingValues: PaddingValues,
    grupo: Grupo?,
    horarios: MutableMap<String, Pair<String, String>>,
    nivelTolerancia: String,
    diasSemana: List<String>,
    onHorarioChange: (String, String, String) -> Unit,
    onNivelToleranciaChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Selector de nivel de tolerancia
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Nivel de Tolerancia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Text(
                    text = "Selecciona el nivel de tolerancia para este grupo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NivelToleranciaOption(
                        titulo = "Estricto",
                        descripcion = "Marca falta después de 15 minutos",
                        valor = "FALTA",
                        seleccionado = nivelTolerancia == "FALTA",
                        onClick = { onNivelToleranciaChange("FALTA") },
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    NivelToleranciaOption(
                        titulo = "Flexible",
                        descripcion = "Marca retraso entre 16-30 minutos",
                        valor = "RETRASO",
                        seleccionado = nivelTolerancia == "RETRASO",
                        onClick = { onNivelToleranciaChange("RETRASO") },
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    NivelToleranciaOption(
                        titulo = "Muy Flexible",
                        descripcion = "Siempre marca como presente",
                        valor = "PRESENTE",
                        seleccionado = nivelTolerancia == "PRESENTE",
                        onClick = { onNivelToleranciaChange("PRESENTE") },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Horarios por día
        Text(
            text = "Horarios de Clase",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        diasSemana.forEach { dia ->
            HorarioDiaCard(
                dia = dia,
                horaInicio = horarios[dia]?.first ?: "",
                horaFin = horarios[dia]?.second ?: "",
                onInicioChange = { onHorarioChange(dia, it, horarios[dia]?.second ?: "") },
                onFinChange = { onHorarioChange(dia, horarios[dia]?.first ?: "", it) }
            )
        }
        
        // Botón guardar
        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Guardar Configuración",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun NivelToleranciaOption(
    titulo: String,
    descripcion: String,
    valor: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) 
                color.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (seleccionado) 
            androidx.compose.foundation.BorderStroke(2.dp, color)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            RadioButton(
                selected = seleccionado,
                onClick = null
            )
        }
    }
}

@Composable
private fun HorarioDiaCard(
    dia: String,
    horaInicio: String,
    horaFin: String,
    onInicioChange: (String) -> Unit,
    onFinChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = dia,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hora inicio
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Inicio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { if (it.length <= 5) onInicioChange(it) },
                        placeholder = { Text("08:00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, null)
                        }
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(top = 16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                // Hora fin
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Fin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { if (it.length <= 5) onFinChange(it) },
                        placeholder = { Text("10:00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, null)
                        }
                    )
                }
            }
        }
    }
}

