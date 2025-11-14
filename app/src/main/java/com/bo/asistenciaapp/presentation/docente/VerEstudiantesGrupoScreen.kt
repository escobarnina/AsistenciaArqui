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
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.presentation.common.UserLayout

/**
 * Pantalla para mostrar los estudiantes inscritos en un grupo específico.
 * 
 * Permite al docente ver la lista de estudiantes inscritos en un grupo.
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Cards de estudiante)
 * - Organisms: Secciones completas (Lista de estudiantes)
 */
@Composable
fun VerEstudiantesGrupoScreen(
    grupoId: Int,
    grupoNombre: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val session = remember { UserSession(context) }
    val docenteId = session.getUserId()
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val grupoRepository = remember { GrupoRepository(database) }
    val inscripcionRepository = remember { InscripcionRepository(database) }
    
    // Obtener nombre del grupo
    val nombreGrupo = remember { mutableStateOf(grupoNombre) }
    
    // Obtener estudiantes del grupo
    val estudiantes = remember { mutableStateListOf<Usuario>() }
    val isLoading = remember { mutableStateOf(true) }
    
    LaunchedEffect(grupoId) {
        try {
            // Obtener nombre del grupo si no se proporcionó
            if (nombreGrupo.value.isEmpty()) {
                val grupos = grupoRepository.obtenerPorDocente(docenteId)
                val grupo = grupos.find { it.id == grupoId }
                nombreGrupo.value = grupo?.let { "${it.materiaNombre} - ${it.grupo}" } ?: "Grupo"
            }
            
            val estudiantesGrupo = inscripcionRepository.obtenerEstudiantesPorGrupo(grupoId)
            estudiantes.clear()
            estudiantes.addAll(estudiantesGrupo)
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
        }
    }
    
    UserLayout(
        title = "Estudiantes",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        VerEstudiantesGrupoContent(
            paddingValues = paddingValues,
            estudiantes = estudiantes,
            isLoading = isLoading.value,
            nombreGrupo = nombreGrupo.value,
            totalEstudiantes = estudiantes.size
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla de estudiantes del grupo.
 * 
 * Organismo que muestra la lista de estudiantes o estados de carga/vacío.
 */
@Composable
private fun VerEstudiantesGrupoContent(
    paddingValues: PaddingValues,
    estudiantes: List<Usuario>,
    isLoading: Boolean,
    nombreGrupo: String,
    totalEstudiantes: Int
) {
    when {
        isLoading -> {
            VerEstudiantesLoadingState(paddingValues = paddingValues)
        }
        estudiantes.isEmpty() -> {
            VerEstudiantesEmptyState(paddingValues = paddingValues, nombreGrupo = nombreGrupo)
        }
        else -> {
            VerEstudiantesList(
                paddingValues = paddingValues,
                estudiantes = estudiantes,
                nombreGrupo = nombreGrupo,
                totalEstudiantes = totalEstudiantes
            )
        }
    }
}

/**
 * Lista de estudiantes del grupo.
 * 
 * Organismo que muestra todos los estudiantes inscritos.
 */
@Composable
private fun VerEstudiantesList(
    paddingValues: PaddingValues,
    estudiantes: List<Usuario>,
    nombreGrupo: String,
    totalEstudiantes: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header con información del grupo
        item {
            VerGrupoInfoHeader(
                nombreGrupo = nombreGrupo,
                totalEstudiantes = totalEstudiantes
            )
        }
        
        // Lista de estudiantes
        items(estudiantes) { estudiante ->
            VerEstudianteCard(
                estudiante = estudiante
            )
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Card de estudiante del grupo.
 * 
 * Molécula que muestra la información del estudiante.
 */
@Composable
private fun VerEstudianteCard(
    estudiante: Usuario
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
                modifier = Modifier.weight(1f),
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
    }
}

/**
 * Estado de carga para la lista de estudiantes.
 * 
 * Molécula que muestra un indicador de carga.
 */
@Composable
private fun VerEstudiantesLoadingState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Header con información del grupo.
 * 
 * Molécula que muestra el nombre del grupo y el total de estudiantes.
 */
@Composable
private fun VerGrupoInfoHeader(
    nombreGrupo: String,
    totalEstudiantes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = nombreGrupo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "$totalEstudiantes estudiante${if (totalEstudiantes != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * Estado vacío para la lista de estudiantes.
 * 
 * Molécula que muestra un mensaje cuando no hay estudiantes.
 */
@Composable
private fun VerEstudiantesEmptyState(
    paddingValues: PaddingValues,
    nombreGrupo: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
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
                    text = "No hay estudiantes inscritos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = nombreGrupo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

