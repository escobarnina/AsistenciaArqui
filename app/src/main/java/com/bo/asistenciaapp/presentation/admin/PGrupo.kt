package com.bo.asistenciaapp.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShortText
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
import com.bo.asistenciaapp.data.repository.MateriaRepository
import com.bo.asistenciaapp.data.repository.UsuarioRepository
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.MateriaCU
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import com.bo.asistenciaapp.domain.viewmodel.GrupoUiState
import com.bo.asistenciaapp.domain.viewmodel.VMGrupo
import com.bo.asistenciaapp.presentation.common.ToastUtils
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlin.text.isNotBlank

/**
 * Pantalla de gestión de grupos para administrador.
 *
 * Permite:
 * - Agregar nuevos grupos
 * - Ver lista de grupos existentes
 * - Eliminar grupos
 *
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos, TextFields)
 * - Molecules: Componentes compuestos (Cards de grupo, Formulario)
 * - Organisms: Secciones completas (Lista de grupos, Formulario de creación)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarGruposScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val grupoRepository = remember { GrupoRepository(database) }
    val materiaRepository = remember { MateriaRepository(database) }
    val usuarioRepository = remember { UsuarioRepository(database) }
    val grupoCU = remember { GrupoCU(grupoRepository) }
    val materiaCU = remember { MateriaCU(materiaRepository) }
    val usuarioCU = remember { UsuarioCU(usuarioRepository) }

    // ViewModel
    val viewModel: VMGrupo = viewModel { VMGrupo(grupoCU, materiaCU, usuarioCU) }

    val grupos by viewModel.grupos.collectAsState()
    val materias by viewModel.materias.collectAsState()
    val docentes by viewModel.docentes.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GrupoUiState.Success -> {
                if (state.mensaje != null) {
                    ToastUtils.mostrarSuperior(context, state.mensaje)
                }
            }
            is GrupoUiState.Error -> {
                ToastUtils.mostrarSuperior(context, state.mensaje)
            }
            else -> {}
        }
    }

    var semestre by remember { mutableStateOf("") }
    var gestion by remember { mutableStateOf("") }
    var grupo by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var expandedMateria by remember { mutableStateOf(false) }
    var materiaSeleccionada by remember { mutableStateOf<Materia?>(null) }
    var docenteSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var expandedDocente by remember { mutableStateOf(false) }

    UserLayout(title = "Gestión de Grupos", showBackButton = true, onBack = onBack) { paddingValues
        ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GrupoFormSection(
                    semestre = semestre,
                    gestion = gestion,
                    grupo = grupo,
                    capacidad = capacidad,
                    materiaSeleccionada = materiaSeleccionada,
                    docenteSeleccionado = docenteSeleccionado,
                    expandedMateria = expandedMateria,
                    expandedDocente = expandedDocente,
                    materias = materias,
                    docentes = docentes,
                    onSemestreChange = { semestre = it },
                    onGestionChange = { gestion = it },
                    onGrupoChange = { grupo = it },
                    onCapacidadChange = { capacidad = it },
                    onMateriaSeleccionadaChange = { materiaSeleccionada = it },
                    onDocenteSeleccionadoChange = { docenteSeleccionado = it },
                    onExpandedMateriaChange = { expandedMateria = it },
                    onExpandedDocenteChange = { expandedDocente = it },
                    onSubmit = {
                        if (semestre.isNotBlank() &&
                                        gestion.isNotBlank() &&
                                        capacidad.isNotBlank() &&
                                        materiaSeleccionada != null &&
                                        docenteSeleccionado != null &&
                                        grupo.isNotBlank()
                        ) {
                            viewModel.agregarGrupo(
                                    materiaSeleccionada!!.id,
                                    materiaSeleccionada!!.nombre,
                                    docenteSeleccionado!!.id,
                                    "${docenteSeleccionado!!.nombres} ${docenteSeleccionado!!.apellidos}",
                                    semestre.toInt(),
                                    gestion.toInt(),
                                    capacidad.toInt(),
                                    grupo
                            )
                            semestre = ""
                            gestion = ""
                            grupo = ""
                            capacidad = ""
                            materiaSeleccionada = null
                            docenteSeleccionado = null
                        } else {
                            ToastUtils.mostrarSuperior(context, "Completa todos los campos")
                        }
                    },
                    isLoading = uiState is GrupoUiState.Loading
            )

            HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
            )

            GrupoListSection(
                    grupos = grupos,
                    onDelete = { viewModel.eliminarGrupo(it) },
                    isLoading = uiState is GrupoUiState.Loading
            )
        }
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Sección del formulario para agregar grupos.
 *
 * Organismo que combina campos de entrada y botón de acción.
 */
@Composable
private fun GrupoFormSection(
        semestre: String,
        gestion: String,
        grupo: String,
        capacidad: String,
        materiaSeleccionada: Materia?,
        docenteSeleccionado: Usuario?,
        expandedMateria: Boolean,
        expandedDocente: Boolean,
        materias: List<Materia>,
        docentes: List<Usuario>,
        onSemestreChange: (String) -> Unit,
        onGestionChange: (String) -> Unit,
        onGrupoChange: (String) -> Unit,
        onCapacidadChange: (String) -> Unit,
        onMateriaSeleccionadaChange: (Materia?) -> Unit,
        onDocenteSeleccionadoChange: (Usuario?) -> Unit,
        onExpandedMateriaChange: (Boolean) -> Unit,
        onExpandedDocenteChange: (Boolean) -> Unit,
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
                        text = "Nuevo Grupo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            GrupoMateriaDropdown(
                    value = materiaSeleccionada,
                    expanded = expandedMateria,
                    materias = materias,
                    onValueChange = onMateriaSeleccionadaChange,
                    onExpandedChange = onExpandedMateriaChange
            )

            GrupoDocenteDropdown(
                    value = docenteSeleccionado,
                    expanded = expandedDocente,
                    docentes = docentes,
                    onValueChange = onDocenteSeleccionadoChange,
                    onExpandedChange = onExpandedDocenteChange
            )

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GrupoSemestreField(
                        value = semestre,
                        onValueChange = onSemestreChange,
                        modifier = Modifier.weight(1f)
                )
                GrupoGestionField(
                        value = gestion,
                        onValueChange = onGestionChange,
                        modifier = Modifier.weight(1f)
                )
            }

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GrupoNombreField(
                        value = grupo,
                        onValueChange = onGrupoChange,
                        modifier = Modifier.weight(1f)
                )
                GrupoCapacidadField(
                        value = capacidad,
                        onValueChange = onCapacidadChange,
                        modifier = Modifier.weight(1f)
                )
            }

            GrupoSubmitButton(onClick = onSubmit, isLoading = isLoading)
        }
    }
}

/**
 * Sección de lista de grupos.
 *
 * Organismo que muestra todos los grupos existentes.
 */
@Composable
private fun GrupoListSection(grupos: List<Grupo>, onDelete: (Int) -> Unit, isLoading: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
            )
            Text(
                    text = "Grupos Registrados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (grupos.isEmpty()) {
            GrupoEmptyState()
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grupos) { grupo ->
                    GrupoCard(
                            grupo = grupo,
                            onDelete = { onDelete(grupo.id) },
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

/** Dropdown para seleccionar la materia del grupo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrupoMateriaDropdown(
        value: Materia?,
        expanded: Boolean,
        materias: List<Materia>,
        onValueChange: (Materia?) -> Unit,
        onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
                value = value?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Materia") },
                leadingIcon = { Icon(imageVector = Icons.Default.Book, contentDescription = null) },
                placeholder = { Text("Seleccionar Materia") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            materias.forEach { materia ->
                DropdownMenuItem(
                        text = { Text("${materia.sigla} - ${materia.nombre}") },
                        onClick = {
                            onValueChange(materia)
                            onExpandedChange(false)
                        }
                )
            }
        }
    }
}

/** Dropdown para seleccionar el docente del grupo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrupoDocenteDropdown(
        value: Usuario?,
        expanded: Boolean,
        docentes: List<Usuario>,
        onValueChange: (Usuario?) -> Unit,
        onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
                value = value?.let { "${it.nombres} ${it.apellidos}" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Docente") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                placeholder = { Text("Seleccionar Docente") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            docentes.forEach { docente ->
                DropdownMenuItem(
                        text = { Text("${docente.nombres} ${docente.apellidos}") },
                        onClick = {
                            onValueChange(docente)
                            onExpandedChange(false)
                        }
                )
            }
        }
    }
}

/** Campo de texto para el semestre del grupo. */
@Composable
private fun GrupoSemestreField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Semestre") },
            leadingIcon = { Icon(imageVector = Icons.Default.Numbers, contentDescription = null) },
            modifier = modifier,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
    )
}

/** Campo de texto para la gestión del grupo. */
@Composable
private fun GrupoGestionField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Gestión") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
            },
            modifier = modifier,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
    )
}

/** Campo de texto para el nombre del grupo. */
@Composable
private fun GrupoNombreField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Grupo") },
            leadingIcon = {
                Icon(imageVector = Icons.AutoMirrored.Filled.ShortText, contentDescription = null)
            },
            modifier = modifier,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
    )
}

/** Campo de texto para la capacidad del grupo. */
@Composable
private fun GrupoCapacidadField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Capacidad") },
            leadingIcon = { Icon(imageVector = Icons.Default.Group, contentDescription = null) },
            modifier = modifier,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
    )
}

/** Botón para enviar el formulario de grupo. */
@Composable
private fun GrupoSubmitButton(onClick: () -> Unit, isLoading: Boolean) {
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
            Text("Agregar Grupo")
        }
    }
}

/** Card que representa un grupo en la lista. */
@Composable
private fun GrupoCard(grupo: Grupo, onDelete: () -> Unit, isLoading: Boolean) {
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
                        color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                            text = grupo.materiaNombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                            text = "Grupo ${grupo.grupo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                    )
                    Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "Semestre ${grupo.semestre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text = "•",
                                color =
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.5f
                                        )
                        )
                        Text(
                                text = "Gestión ${grupo.gestion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text = "•",
                                color =
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.5f
                                        )
                        )
                        Text(
                                text = "Capacidad: ${grupo.capacidad}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                            text = grupo.docenteNombre,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                    )
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

/** Estado vacío cuando no hay grupos. */
@Composable
private fun GrupoEmptyState() {
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
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                    text = "No hay grupos registrados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
            )
            Text(
                    text = "Agrega un nuevo grupo usando el formulario superior",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
