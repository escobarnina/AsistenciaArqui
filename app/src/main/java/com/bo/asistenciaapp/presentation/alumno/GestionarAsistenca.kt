package com.bo.asistenciaapp.presentation.alumno

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.usecase.AsistenciaCU
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.viewmodel.AsistenciaUiState
import com.bo.asistenciaapp.domain.viewmodel.VMAsistencia
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para gestionar la asistencia del estudiante.
 * 
 * Permite al estudiante:
 * - Marcar asistencia en los grupos en los que está inscrito
 * - Ver su historial de asistencias
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Cards de grupo, Items de asistencia)
 * - Organisms: Secciones completas (Lista de grupos, Lista de asistencias)
 * 
 * @param onBack Callback cuando se presiona el botón de retroceso
 */
@Composable
fun GestionarAsistencia(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { UserSession(context) }
    val alumnoId = session.getUserId()
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val asistenciaRepository = remember { AsistenciaRepository(database) }
    val inscripcionRepository = remember { InscripcionRepository(database) }
    val grupoRepository = remember { GrupoRepository(database) }
    val asistenciaCU = remember { AsistenciaCU(asistenciaRepository) }
    val inscripcionCU = remember { InscripcionCU(inscripcionRepository) }
    val grupoCU = remember { GrupoCU(grupoRepository) }
    
    // ViewModel
    val viewModel: VMAsistencia = viewModel {
        VMAsistencia(asistenciaCU, inscripcionCU, grupoCU, alumnoId)
    }
    
    val grupos by viewModel.grupos.collectAsState()
    val asistencias by viewModel.asistencias.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AsistenciaUiState.Success -> {
                if (state.mensaje != null) {
                    scope.launch { snackbar.showSnackbar(state.mensaje) }
                }
            }
            is AsistenciaUiState.Error -> {
                scope.launch { snackbar.showSnackbar(state.mensaje) }
            }
            else -> {}
        }
    }

    UserLayout(
        title = "Marcar Asistencia",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        GestionarAsistenciaContent(
            paddingValues = paddingValues,
            grupos = grupos,
            asistencias = asistencias,
            uiState = uiState,
            onMarcarAsistencia = { grupoId ->
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                viewModel.marcarAsistencia(grupoId, fecha)
            },
            snackbarHostState = snackbar
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla de gestión de asistencia.
 * 
 * Organismo que combina las listas de grupos y asistencias.
 */
@Composable
private fun GestionarAsistenciaContent(
    paddingValues: PaddingValues,
    grupos: List<Grupo>,
    asistencias: List<Asistencia>,
    uiState: AsistenciaUiState,
    onMarcarAsistencia: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AsistenciaGruposSection(
                grupos = grupos,
                uiState = uiState,
                onMarcarAsistencia = onMarcarAsistencia
            )
            
            AsistenciaHistorialSection(asistencias = asistencias)
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Sección de grupos disponibles para marcar asistencia.
 * 
 * Organismo que muestra la lista de grupos con sus acciones.
 */
@Composable
private fun AsistenciaGruposSection(
    grupos: List<Grupo>,
    uiState: AsistenciaUiState,
    onMarcarAsistencia: (Int) -> Unit
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
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Grupos Disponibles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (grupos.isEmpty()) {
            AsistenciaEmptyState(
                icon = Icons.Default.School,
                message = "No tienes grupos disponibles"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grupos) { grupo ->
                    AsistenciaGrupoCard(
                        grupo = grupo,
                        isLoading = uiState is AsistenciaUiState.Loading,
                        onMarcarAsistencia = { onMarcarAsistencia(grupo.id) }
                    )
                }
            }
        }
    }
}

/**
 * Sección de historial de asistencias.
 * 
 * Organismo que muestra la lista de asistencias registradas.
 */
@Composable
private fun AsistenciaHistorialSection(
    asistencias: List<Asistencia>
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
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Mi Historial de Asistencias",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (asistencias.isEmpty()) {
            AsistenciaEmptyState(
                icon = Icons.Default.EventNote,
                message = "No hay asistencias registradas"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(asistencias) { asistencia ->
                    AsistenciaHistorialCard(asistencia = asistencia)
                }
            }
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Card de grupo para marcar asistencia.
 * 
 * Molécula que muestra la información del grupo y botón de acción.
 */
@Composable
private fun AsistenciaGrupoCard(
    grupo: Grupo,
    isLoading: Boolean,
    onMarcarAsistencia: () -> Unit
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
                            imageVector = Icons.Default.School,
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
                        text = grupo.materiaNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Grupo ${grupo.grupo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Button(
                onClick = onMarcarAsistencia,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
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
}

/**
 * Card de historial de asistencia.
 * 
 * Molécula que muestra la información de una asistencia registrada.
 */
@Composable
private fun AsistenciaHistorialCard(
    asistencia: Asistencia
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${asistencia.materiaNombre} - Grupo ${asistencia.grupo}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = asistencia.fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Estado vacío para listas.
 * 
 * Molécula que muestra un mensaje cuando no hay elementos.
 */
@Composable
private fun AsistenciaEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
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
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}