package com.bo.asistenciaapp.presentation.docente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para que el docente marque asistencias de estudiantes.
 * 
 * Permite al docente:
 * - Seleccionar un grupo
 * - Ver la lista de estudiantes inscritos
 * - Marcar asistencia para cada estudiante
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Cards de grupo, Cards de estudiante)
 * - Organisms: Secciones completas (Selector de grupo, Lista de estudiantes)
 */
@Composable
fun MarcarAsistenciaDocenteScreen(
    docenteId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val grupoRepository = remember { GrupoRepository(database) }
    val inscripcionRepository = remember { InscripcionRepository(database) }
    val asistenciaRepository = remember { AsistenciaRepository(database) }
    
    // Estados
    val gruposDocente = remember { mutableStateListOf<Grupo>() }
    val estudiantesGrupo = remember { mutableStateListOf<Usuario>() }
    var grupoSeleccionado by remember { mutableStateOf<Grupo?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val isLoadingEstudiantes = remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Obtener grupos del docente
    LaunchedEffect(docenteId) {
        try {
            val grupos = grupoRepository.obtenerPorDocente(docenteId)
            gruposDocente.clear()
            gruposDocente.addAll(grupos)
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
        }
    }
    
    // Obtener estudiantes cuando se selecciona un grupo
    LaunchedEffect(grupoSeleccionado?.id) {
        grupoSeleccionado?.let { grupo ->
            isLoadingEstudiantes.value = true
            try {
                val estudiantes = inscripcionRepository.obtenerEstudiantesPorGrupo(grupo.id)
                estudiantesGrupo.clear()
                estudiantesGrupo.addAll(estudiantes)
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Error al cargar estudiantes: ${e.message ?: "Error desconocido"}")
                }
            } finally {
                isLoadingEstudiantes.value = false
            }
        } ?: run {
            estudiantesGrupo.clear()
        }
    }
    
    UserLayout(
        title = "Marcar Asistencias",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MarcarAsistenciaGrupoSelector(
                    grupos = gruposDocente,
                    grupoSeleccionado = grupoSeleccionado,
                    isLoading = isLoading.value,
                    onGrupoSeleccionado = { grupoSeleccionado = it }
                )
                
                if (grupoSeleccionado != null) {
                    MarcarAsistenciaEstudiantesList(
                        estudiantes = estudiantesGrupo,
                        grupoSeleccionado = grupoSeleccionado!!,
                        isLoading = isLoadingEstudiantes.value,
                        asistenciaRepository = asistenciaRepository,
                        snackbarHostState = snackbarHostState,
                        scope = scope
                    )
                } else {
                    MarcarAsistenciaEmptyState()
                }
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Selector de grupo para marcar asistencias.
 * 
 * Organismo que muestra los grupos disponibles y permite seleccionar uno.
 */
@Composable
private fun MarcarAsistenciaGrupoSelector(
    grupos: List<Grupo>,
    grupoSeleccionado: Grupo?,
    isLoading: Boolean,
    onGrupoSeleccionado: (Grupo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Seleccionar Grupo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (grupos.isEmpty()) {
                Text(
                    text = "No tienes grupos asignados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grupos.forEach { grupo ->
                        MarcarAsistenciaGrupoOption(
                            grupo = grupo,
                            isSelected = grupoSeleccionado?.id == grupo.id,
                            onSelect = { onGrupoSeleccionado(grupo) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lista de estudiantes para marcar asistencias.
 * 
 * Organismo que muestra los estudiantes del grupo seleccionado.
 */
@Composable
private fun MarcarAsistenciaEstudiantesList(
    estudiantes: List<Usuario>,
    grupoSeleccionado: Grupo,
    isLoading: Boolean,
    asistenciaRepository: AsistenciaRepository,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Estudiantes - ${grupoSeleccionado.materiaNombre}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            estudiantes.isEmpty() -> {
                MarcarAsistenciaEstudiantesEmptyState()
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estudiantes) { estudiante ->
                        MarcarAsistenciaEstudianteCard(
                            estudiante = estudiante,
                            grupoId = grupoSeleccionado.id,
                            asistenciaRepository = asistenciaRepository,
                            snackbarHostState = snackbarHostState,
                            scope = scope
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Opción de grupo en el selector.
 * 
 * Molécula que muestra la información del grupo y permite seleccionarlo.
 */
@Composable
private fun MarcarAsistenciaGrupoOption(
    grupo: Grupo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = grupo.materiaNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Grupo ${grupo.grupo} • S${grupo.semestre}/${grupo.gestion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            RadioButton(
                selected = isSelected,
                onClick = null
            )
        }
    }
}

/**
 * Card de estudiante para marcar asistencia.
 * 
 * Molécula que muestra la información del estudiante y botón para marcar asistencia.
 */
@Composable
private fun MarcarAsistenciaEstudianteCard(
    estudiante: Usuario,
    grupoId: Int,
    asistenciaRepository: AsistenciaRepository,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${estudiante.nombres} ${estudiante.apellidos}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Registro: ${estudiante.registro}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Button(
                onClick = {
                    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    scope.launch {
                        try {
                            asistenciaRepository.registrar(estudiante.id, grupoId, fecha)
                            snackbarHostState.showSnackbar("Asistencia marcada para ${estudiante.nombres}")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error: ${e.message ?: "Error desconocido"}")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Marcar",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * Estado vacío cuando no hay grupo seleccionado.
 * 
 * Molécula que muestra un mensaje para seleccionar un grupo.
 */
@Composable
private fun MarcarAsistenciaEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "Selecciona un grupo para ver los estudiantes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Estado vacío cuando no hay estudiantes.
 * 
 * Molécula que muestra un mensaje cuando no hay estudiantes inscritos.
 */
@Composable
private fun MarcarAsistenciaEstudiantesEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "No hay estudiantes inscritos en este grupo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

