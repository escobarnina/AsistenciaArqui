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
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.presentation.common.UserLayout

/**
 * Pantalla para mostrar los grupos asignados al docente.
 * 
 * Permite al docente ver todos sus grupos y navegar a ver los estudiantes
 * inscritos en cada grupo.
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Cards de grupo)
 * - Organisms: Secciones completas (Lista de grupos)
 */
@Composable
fun VerGruposDocenteScreen(
    onBack: () -> Unit,
    onVerEstudiantes: (Int) -> Unit
) {
    val context = LocalContext.current
    val session = remember { UserSession(context) }
    val docenteId = session.getUserId()
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val grupoRepository = remember { GrupoRepository(database) }
    
    // Obtener grupos del docente
    val gruposDocente = remember { mutableStateListOf<Grupo>() }
    val isLoading = remember { mutableStateOf(true) }
    
    LaunchedEffect(docenteId) {
        try {
            val todosGrupos = grupoRepository.obtenerPorDocente(docenteId)
            gruposDocente.clear()
            gruposDocente.addAll(todosGrupos)
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
        }
    }
    
    UserLayout(
        title = "Mis Grupos",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        VerGruposDocenteContent(
            paddingValues = paddingValues,
            grupos = gruposDocente,
            isLoading = isLoading.value,
            onVerEstudiantes = onVerEstudiantes
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla de grupos del docente.
 * 
 * Organismo que muestra la lista de grupos o estados de carga/vacío.
 */
@Composable
private fun VerGruposDocenteContent(
    paddingValues: PaddingValues,
    grupos: List<Grupo>,
    isLoading: Boolean,
    onVerEstudiantes: (Int) -> Unit
) {
    when {
        isLoading -> {
            VerGruposLoadingState(paddingValues = paddingValues)
        }
        grupos.isEmpty() -> {
            VerGruposEmptyState(paddingValues = paddingValues)
        }
        else -> {
            VerGruposList(
                paddingValues = paddingValues,
                grupos = grupos,
                onVerEstudiantes = onVerEstudiantes
            )
        }
    }
}

/**
 * Lista de grupos del docente.
 * 
 * Organismo que muestra todos los grupos asignados.
 */
@Composable
private fun VerGruposList(
    paddingValues: PaddingValues,
    grupos: List<Grupo>,
    onVerEstudiantes: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(grupos) { grupo ->
            VerGrupoCard(
                grupo = grupo,
                onClick = { onVerEstudiantes(grupo.id) }
            )
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Card de grupo del docente.
 * 
 * Molécula que muestra la información del grupo y permite navegar a estudiantes.
 */
@Composable
private fun VerGrupoCard(
    grupo: Grupo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = grupo.materiaNombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Grupo ${grupo.grupo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    
                    Text(
                        text = "S${grupo.semestre}/${grupo.gestion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${grupo.nroInscritos}/${grupo.capacidad} estudiantes",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Estado de carga para la lista de grupos.
 * 
 * Molécula que muestra un indicador de carga.
 */
@Composable
private fun VerGruposLoadingState(paddingValues: PaddingValues) {
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
 * Estado vacío para la lista de grupos.
 * 
 * Molécula que muestra un mensaje cuando no hay grupos.
 */
@Composable
private fun VerGruposEmptyState(paddingValues: PaddingValues) {
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
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "No tienes grupos asignados",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

