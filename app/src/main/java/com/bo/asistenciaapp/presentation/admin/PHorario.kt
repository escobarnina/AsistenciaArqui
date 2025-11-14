package com.bo.asistenciaapp.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.HorarioRepository
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Horario
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.HorarioCU
import com.bo.asistenciaapp.domain.viewmodel.HorarioUiState
import com.bo.asistenciaapp.domain.viewmodel.VMHorario
import com.bo.asistenciaapp.presentation.common.ToastUtils
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlin.text.isNotBlank

/**
 * Pantalla de gestión de horarios para administrador.
 *
 * Permite:
 * - Agregar nuevos horarios
 * - Ver lista de horarios existentes
 * - Eliminar horarios
 *
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos, TextFields)
 * - Molecules: Componentes compuestos (Cards de horario, Formulario)
 * - Organisms: Secciones completas (Lista de horarios, Formulario de creación)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarHorarios(onBack: () -> Unit) {
    val context = LocalContext.current

    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val horarioRepository = remember { HorarioRepository(database) }
    val grupoRepository = remember { GrupoRepository(database) }
    val horarioCU = remember { HorarioCU(horarioRepository) }
    val grupoCU = remember { GrupoCU(grupoRepository) }

    // ViewModel
    val viewModel: VMHorario = viewModel { VMHorario(horarioCU, grupoCU) }

    val horarios by viewModel.horarios.collectAsState()
    val grupos by viewModel.grupos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is HorarioUiState.Success -> {
                if (state.mensaje != null) {
                    ToastUtils.mostrarSuperior(context, state.mensaje)
                }
            }
            is HorarioUiState.Error -> {
                ToastUtils.mostrarSuperior(context, state.mensaje)
            }
            else -> {}
        }
    }

    // Campos del formulario
    var grupoSeleccionado by remember { mutableStateOf<Grupo?>(null) }
    var expandedGrupo by remember { mutableStateOf(false) }
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    var diaSeleccionado by remember { mutableStateOf(dias.first()) }
    var expandedDia by remember { mutableStateOf(false) }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }

    UserLayout(title = "Gestión de Horarios", showBackButton = true, onBack = onBack) {
            paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HorarioFormSection(
                    grupoSeleccionado = grupoSeleccionado,
                    diaSeleccionado = diaSeleccionado,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    expandedGrupo = expandedGrupo,
                    expandedDia = expandedDia,
                    grupos = grupos,
                    dias = dias,
                    onGrupoSeleccionadoChange = { grupoSeleccionado = it },
                    onDiaSeleccionadoChange = { diaSeleccionado = it },
                    onHoraInicioChange = { horaInicio = it },
                    onHoraFinChange = { horaFin = it },
                    onExpandedGrupoChange = { expandedGrupo = it },
                    onExpandedDiaChange = { expandedDia = it },
                    onSubmit = {
                        if (grupoSeleccionado != null &&
                                        horaInicio.isNotBlank() &&
                                        horaFin.isNotBlank()
                        ) {
                            viewModel.agregarHorario(
                                    grupoSeleccionado!!.id,
                                    diaSeleccionado,
                                    horaInicio,
                                    horaFin
                            )
                            horaInicio = ""
                            horaFin = ""
                        } else {
                            ToastUtils.mostrarSuperior(context, "Complete todos los campos")
                        }
                    },
                    isLoading = uiState is HorarioUiState.Loading
            )

            HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
            )

            HorarioListSection(
                    horarios = horarios,
                    onDelete = { viewModel.eliminarHorario(it) },
                    isLoading = uiState is HorarioUiState.Loading
            )
        }
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Sección del formulario para agregar horarios.
 *
 * Organismo que combina campos de entrada y botón de acción.
 */
@Composable
private fun HorarioFormSection(
        grupoSeleccionado: Grupo?,
        diaSeleccionado: String,
        horaInicio: String,
        horaFin: String,
        expandedGrupo: Boolean,
        expandedDia: Boolean,
        grupos: List<Grupo>,
        dias: List<String>,
        onGrupoSeleccionadoChange: (Grupo?) -> Unit,
        onDiaSeleccionadoChange: (String) -> Unit,
        onHoraInicioChange: (String) -> Unit,
        onHoraFinChange: (String) -> Unit,
        onExpandedGrupoChange: (Boolean) -> Unit,
        onExpandedDiaChange: (Boolean) -> Unit,
        onSubmit: () -> Unit,
        isLoading: Boolean
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                )
                Text(
                        text = "Nuevo Horario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorarioGrupoDropdown(
                    value = grupoSeleccionado,
                    expanded = expandedGrupo,
                    grupos = grupos,
                    onValueChange = onGrupoSeleccionadoChange,
                    onExpandedChange = onExpandedGrupoChange
            )

            HorarioDiaDropdown(
                    value = diaSeleccionado,
                    expanded = expandedDia,
                    dias = dias,
                    onValueChange = onDiaSeleccionadoChange,
                    onExpandedChange = onExpandedDiaChange
            )

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorarioHoraInicioField(
                        value = horaInicio,
                        onValueChange = onHoraInicioChange,
                        modifier = Modifier.weight(1f)
                )
                HorarioHoraFinField(
                        value = horaFin,
                        onValueChange = onHoraFinChange,
                        modifier = Modifier.weight(1f)
                )
            }

            HorarioSubmitButton(onClick = onSubmit, isLoading = isLoading)
        }
    }
}

/**
 * Sección de lista de horarios.
 *
 * Organismo que muestra todos los horarios existentes.
 */
@Composable
private fun HorarioListSection(
        horarios: List<Horario>,
        onDelete: (Int) -> Unit,
        isLoading: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
            )
            Text(
                    text = "Horarios Registrados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (horarios.isEmpty()) {
            HorarioEmptyState()
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(horarios) { horario ->
                    HorarioCard(
                            horario = horario,
                            onDelete = { onDelete(horario.id) },
                            isLoading = isLoading
                    )
                }
            }
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/** Dropdown para seleccionar el grupo del horario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorarioGrupoDropdown(
        value: Grupo?,
        expanded: Boolean,
        grupos: List<Grupo>,
        onValueChange: (Grupo?) -> Unit,
        onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
                value = value?.let { "${it.materiaNombre} ${it.grupo}" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Grupo") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.School, contentDescription = null)
                },
                placeholder = { Text("Seleccionar Grupo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            grupos.forEach { grupo ->
                DropdownMenuItem(
                        text = {
                            Text("${grupo.materiaNombre} ${grupo.grupo} - ${grupo.docenteNombre}")
                        },
                        onClick = {
                            onValueChange(grupo)
                            onExpandedChange(false)
                        }
                )
            }
        }
    }
}

/** Dropdown para seleccionar el día del horario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorarioDiaDropdown(
        value: String,
        expanded: Boolean,
        dias: List<String>,
        onValueChange: (String) -> Unit,
        onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text("Día") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            dias.forEach { dia ->
                DropdownMenuItem(
                        text = { Text(dia) },
                        onClick = {
                            onValueChange(dia)
                            onExpandedChange(false)
                        }
                )
            }
        }
    }
}

/** Campo de texto para la hora de inicio del horario. */
@Composable
private fun HorarioHoraInicioField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Hora Inicio") },
            placeholder = { Text("HH:mm") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
            },
            modifier = modifier,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
    )
}

/** Campo de texto para la hora de fin del horario. */
@Composable
private fun HorarioHoraFinField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Hora Fin") },
            placeholder = { Text("HH:mm") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
            },
            modifier = modifier,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
    )
}

/** Botón para enviar el formulario de horario. */
@Composable
private fun HorarioSubmitButton(onClick: () -> Unit, isLoading: Boolean) {
    Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar Horario")
        }
    }
}

/** Card que representa un horario en la lista. */
@Composable
private fun HorarioCard(horario: Horario, onDelete: () -> Unit, isLoading: Boolean) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                            text = "${horario.materia} ${horario.grupo}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                    text = horario.dia,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                                text = "${horario.horaInicio} - ${horario.horaFin}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            IconButton(onClick = onDelete, enabled = !isLoading) {
                Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Estado vacío cuando no hay horarios. */
@Composable
private fun HorarioEmptyState() {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                    text = "No hay horarios registrados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
            )
            Text(
                    text = "Agrega un nuevo horario usando el formulario superior",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
