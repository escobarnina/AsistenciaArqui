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
import com.bo.asistenciaapp.domain.usecase.ConfigurarGrupoCU
import com.bo.asistenciaapp.domain.usecase.ConfigurarHorarioCU
import com.bo.asistenciaapp.presentation.common.ToastUtils
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch

/**
 * Pantalla para configurar horarios y nivel de tolerancia de un grupo.
 * 
 * Permite configurar:
 * - Múltiples horarios por día (no limitado a Lunes-Viernes)
 * - Nivel de tolerancia (Estricto, Flexible, Muy Flexible)
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos, Botones)
 * - Molecules: Componentes compuestos (Campos de hora, Badges, Opciones)
 * - Organisms: Secciones completas (Cards de día, Sección de tolerancia)
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
    
    // Estados - Cambiar a lista de horarios por día
    var grupo by remember { mutableStateOf<Grupo?>(null) }
    var horariosPorDia by remember { mutableStateOf<Map<String, List<Pair<String, String>>>>(emptyMap()) }
    var nivelTolerancia by remember { mutableStateOf("RETRASO") }
    
    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    
    // Cargar datos del grupo y horarios existentes
    LaunchedEffect(grupoId) {
        try {
            grupo = grupoRepository.obtenerPorId(grupoId)
            grupo?.tipoEstrategia?.let { nivelTolerancia = it }
            
            // Cargar horarios existentes agrupados por día
            val horariosExistentes = horarioRepository.obtenerPorGrupo(grupoId)
            val horariosAgrupados = mutableMapOf<String, MutableList<Pair<String, String>>>()
            horariosExistentes.forEach { horario ->
                val dia = horario.dia
                if (horariosAgrupados[dia] == null) {
                    horariosAgrupados[dia] = mutableListOf()
                }
                horariosAgrupados[dia]?.add(Pair(horario.horaInicio, horario.horaFin))
            }
            horariosPorDia = horariosAgrupados.mapValues { it.value.toList() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    UserLayout(
        title = grupo?.let { "${it.materiaNombre} - ${it.grupo}" } ?: "Configurar Horarios",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        ConfigurarHorariosContent(
            paddingValues = paddingValues,
            horariosPorDia = horariosPorDia,
            nivelTolerancia = nivelTolerancia,
            diasSemana = diasSemana,
            onAgregarHorario = { dia ->
                val nuevaLista = horariosPorDia[dia]?.toMutableList() ?: mutableListOf()
                nuevaLista.add(Pair("", ""))
                horariosPorDia = horariosPorDia.toMutableMap().apply {
                    put(dia, nuevaLista.toList())
                }
            },
            onEliminarHorario = { dia, index ->
                val nuevaLista = horariosPorDia[dia]?.toMutableList() ?: mutableListOf()
                nuevaLista.removeAt(index)
                val nuevoMapa = horariosPorDia.toMutableMap()
                if (nuevaLista.isEmpty()) {
                    nuevoMapa.remove(dia)
                } else {
                    nuevoMapa[dia] = nuevaLista.toList()
                }
                horariosPorDia = nuevoMapa
            },
            onHorarioChange = { dia, index, inicio, fin ->
                val nuevaLista = horariosPorDia[dia]?.toMutableList() ?: mutableListOf()
                if (index < nuevaLista.size) {
                    nuevaLista[index] = Pair(inicio, fin)
                    horariosPorDia = horariosPorDia.toMutableMap().apply {
                        put(dia, nuevaLista.toList())
                    }
                }
            },
            onNivelToleranciaChange = { nivelTolerancia = it },
            onGuardar = {
                scope.launch {
                    try {
                        grupo?.let {
                            configurarGrupoCU.configurarTipoEstrategia(grupoId, nivelTolerancia)
                        }
                        
                        horarioRepository.eliminarPorGrupo(grupoId)
                        
                        horariosPorDia.forEach { (dia, horarios) ->
                            horarios.forEach { (inicio, fin) ->
                                if (inicio.isNotEmpty() && fin.isNotEmpty()) {
                                    val result = configurarHorarioCU.configurarHorario(
                                        grupoId, dia, inicio, fin
                                    )
                                    if (result is com.bo.asistenciaapp.domain.utils.ValidationResult.Error) {
                                        ToastUtils.mostrarSuperior(context, "Error en $dia: ${result.message}")
                                        return@launch
                                    }
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

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla de configuración de horarios.
 * 
 * Organismo que organiza las secciones de tolerancia y horarios.
 */
@Composable
private fun ConfigurarHorariosContent(
    paddingValues: PaddingValues,
    horariosPorDia: Map<String, List<Pair<String, String>>>,
    nivelTolerancia: String,
    diasSemana: List<String>,
    onAgregarHorario: (String) -> Unit,
    onEliminarHorario: (String, Int) -> Unit,
    onHorarioChange: (String, Int, String, String) -> Unit,
    onNivelToleranciaChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        // Sección de nivel de tolerancia
        ToleranciaSection(
            nivelTolerancia = nivelTolerancia,
            onNivelToleranciaChange = onNivelToleranciaChange
        )
        
        // Sección de horarios
        HorariosSection(
            horariosPorDia = horariosPorDia,
            diasSemana = diasSemana,
            onAgregarHorario = onAgregarHorario,
            onEliminarHorario = onEliminarHorario,
            onHorarioChange = onHorarioChange
        )
        }
        
        // Botón guardar (fuera del scroll para que siempre sea visible)
        GuardarButton(onClick = onGuardar)
    }
}

/**
 * Sección de configuración de nivel de tolerancia.
 * 
 * Organismo que muestra las opciones de tolerancia.
 */
@Composable
private fun ToleranciaSection(
    nivelTolerancia: String,
    onNivelToleranciaChange: (String) -> Unit
) {
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
            ToleranciaHeader()
            
            Text(
                text = "Selecciona el nivel de tolerancia para este grupo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToleranciaOption(
                    titulo = "Estricto",
                    descripcion = "Marca falta después de 15 minutos",
                    seleccionado = nivelTolerancia == "FALTA",
                    onClick = { onNivelToleranciaChange("FALTA") },
                    color = MaterialTheme.colorScheme.error
                )
                
                ToleranciaOption(
                    titulo = "Flexible",
                    descripcion = "Marca retraso entre 16-30 minutos",
                    seleccionado = nivelTolerancia == "RETRASO",
                    onClick = { onNivelToleranciaChange("RETRASO") },
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                ToleranciaOption(
                    titulo = "Muy Flexible",
                    descripcion = "Siempre marca como presente",
                    seleccionado = nivelTolerancia == "PRESENTE",
                    onClick = { onNivelToleranciaChange("PRESENTE") },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Sección de horarios por día.
 * 
 * Organismo que muestra los días con sus horarios.
 */
@Composable
private fun HorariosSection(
    horariosPorDia: Map<String, List<Pair<String, String>>>,
    diasSemana: List<String>,
    onAgregarHorario: (String) -> Unit,
    onEliminarHorario: (String, Int) -> Unit,
    onHorarioChange: (String, Int, String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(text = "Horarios de Clase")
        
        diasSemana.forEach { dia ->
            DiaHorariosCard(
                dia = dia,
                horarios = horariosPorDia[dia]?.toMutableList() ?: mutableListOf(),
                onAgregarHorario = { onAgregarHorario(dia) },
                onEliminarHorario = { index -> onEliminarHorario(dia, index) },
                onHorarioChange = { index, inicio, fin -> onHorarioChange(dia, index, inicio, fin) }
            )
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Header de la sección de tolerancia.
 * 
 * Molécula que muestra el título con icono.
 */
@Composable
private fun ToleranciaHeader() {
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
}

/**
 * Opción de nivel de tolerancia.
 * 
 * Molécula que representa una opción seleccionable.
 */
@Composable
private fun ToleranciaOption(
    titulo: String,
    descripcion: String,
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

/**
 * Card de día con múltiples horarios.
 * 
 * Molécula que muestra un día con todos sus horarios y permite agregar/eliminar.
 */
@Composable
private fun DiaHorariosCard(
    dia: String,
    horarios: MutableList<Pair<String, String>>,
    onAgregarHorario: () -> Unit,
    onEliminarHorario: (Int) -> Unit,
    onHorarioChange: (Int, String, String) -> Unit
) {
    val tieneHorarios = horarios.any { it.first.isNotEmpty() && it.second.isNotEmpty() }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tieneHorarios) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (tieneHorarios) 
            androidx.compose.foundation.BorderStroke(
                1.dp, 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header del día
            DiaHeader(
                dia = dia,
                tieneHorarios = tieneHorarios,
                cantidadHorarios = horarios.count { it.first.isNotEmpty() && it.second.isNotEmpty() }
            )
            
            // Lista de horarios
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                horarios.forEachIndexed { index, (inicio, fin) ->
                    HorarioItem(
                        horaInicio = inicio,
                        horaFin = fin,
                        onInicioChange = { onHorarioChange(index, it, fin) },
                        onFinChange = { onHorarioChange(index, inicio, it) },
                        onEliminar = { onEliminarHorario(index) },
                        mostrarEliminar = horarios.size > 1
                    )
                }
            }
            
            // Botón agregar horario
            AgregarHorarioButton(onClick = onAgregarHorario)
        }
    }
}

/**
 * Header del día con badge de estado.
 * 
 * Molécula que muestra el nombre del día y su estado.
 */
@Composable
private fun DiaHeader(
    dia: String,
    tieneHorarios: Boolean,
    cantidadHorarios: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dia,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (tieneHorarios)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        DiaStatusBadge(tieneHorarios = tieneHorarios, cantidadHorarios = cantidadHorarios)
    }
}

/**
 * Badge de estado del día.
 * 
 * Molécula que muestra si el día tiene horarios configurados.
 */
@Composable
private fun DiaStatusBadge(
    tieneHorarios: Boolean,
    cantidadHorarios: Int
) {
    if (tieneHorarios) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Horario configurado",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (cantidadHorarios > 1) "$cantidadHorarios horarios" else "Configurado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Agregar horario",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "Sin horario",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Item de horario individual.
 * 
 * Molécula que muestra un horario con campos de inicio y fin.
 */
@Composable
private fun HorarioItem(
    horaInicio: String,
    horaFin: String,
    onInicioChange: (String) -> Unit,
    onFinChange: (String) -> Unit,
    onEliminar: () -> Unit,
    mostrarEliminar: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Campos de hora
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HoraTextField(
                value = horaInicio,
                onValueChange = { if (it.length <= 5) onInicioChange(it) },
                label = "Inicio",
                placeholder = "HH:mm",
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.padding(top = 8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            HoraTextField(
                value = horaFin,
                onValueChange = { if (it.length <= 5) onFinChange(it) },
                label = "Fin",
                placeholder = "HH:mm",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Botón eliminar
        if (mostrarEliminar) {
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar horario",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Campo de texto para hora.
 * 
 * Molécula que representa un campo de entrada de hora.
 */
@Composable
private fun HoraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Schedule, null)
            }
        )
    }
}

/**
 * Botón para agregar un nuevo horario.
 * 
 * Molécula que permite agregar un horario adicional.
 */
@Composable
private fun AgregarHorarioButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Agregar Horario",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * Botón de guardar configuración.
 * 
 * Molécula que guarda todos los cambios.
 */
@Composable
private fun GuardarButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
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

// ============================================================================
// ATOMS - Elementos básicos reutilizables
// ============================================================================

/**
 * Título de sección.
 * 
 * Átomo que muestra un título de sección.
 */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}
