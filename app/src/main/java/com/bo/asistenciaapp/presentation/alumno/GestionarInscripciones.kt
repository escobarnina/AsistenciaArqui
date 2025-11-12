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
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.model.Boleta
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.viewmodel.InscripcionUiState
import com.bo.asistenciaapp.domain.viewmodel.VMInscripcion
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para gestionar las inscripciones del estudiante.
 * 
 * Permite al estudiante:
 * - Inscribirse en grupos disponibles
 * - Ver su boleta de inscripción con horarios
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Cards de grupo, Items de boleta)
 * - Organisms: Secciones completas (Lista de grupos, Lista de boleta)
 * 
 * @param alumnoId ID del estudiante
 * @param semestreActual Semestre actual
 * @param gestionActual Gestión (año) actual
 * @param onBack Callback cuando se presiona el botón de retroceso
 */
@Composable
fun GestionarInscripciones(
    alumnoId: Int,
    semestreActual: Int,
    gestionActual: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val inscripcionRepository = remember { InscripcionRepository(database) }
    val grupoRepository = remember { GrupoRepository(database) }
    val inscripcionCU = remember { InscripcionCU(inscripcionRepository) }
    val grupoCU = remember { GrupoCU(grupoRepository) }
    
    // ViewModel
    val viewModel: VMInscripcion = viewModel {
        VMInscripcion(inscripcionCU, grupoCU, alumnoId)
    }
    
    val grupos by viewModel.grupos.collectAsState()
    val boletas by viewModel.boletas.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is InscripcionUiState.Success -> {
                if (state.mensaje != null) {
                    scope.launch { snackbar.showSnackbar(state.mensaje) }
                }
            }
            is InscripcionUiState.Error -> {
                scope.launch { snackbar.showSnackbar(state.mensaje) }
            }
            else -> {}
        }
    }

    UserLayout(
        title = "Inscripción de Materias",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        GestionarInscripcionesContent(
            paddingValues = paddingValues,
            grupos = grupos,
            boletas = boletas,
            uiState = uiState,
            semestreActual = semestreActual,
            gestionActual = gestionActual,
            onInscribirse = { grupoId ->
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                viewModel.registrarInscripcion(grupoId, fecha, semestreActual, gestionActual)
            },
            snackbarHostState = snackbar
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla de gestión de inscripciones.
 * 
 * Organismo que combina las listas de grupos y boleta.
 */
@Composable
private fun GestionarInscripcionesContent(
    paddingValues: PaddingValues,
    grupos: List<Grupo>,
    boletas: List<Boleta>,
    uiState: InscripcionUiState,
    semestreActual: Int,
    gestionActual: Int,
    onInscribirse: (Int) -> Unit,
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
            InscripcionesGruposSection(
                grupos = grupos,
                uiState = uiState,
                onInscribirse = onInscribirse
            )
            
            InscripcionesBoletaSection(boletas = boletas)
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Sección de grupos disponibles para inscripción.
 * 
 * Organismo que muestra la lista de grupos con sus acciones.
 */
@Composable
private fun InscripcionesGruposSection(
    grupos: List<Grupo>,
    uiState: InscripcionUiState,
    onInscribirse: (Int) -> Unit
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
                imageVector = Icons.Default.School,
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
            InscripcionesEmptyState(
                icon = Icons.Default.Book,
                message = "No hay grupos disponibles para inscripción"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grupos) { grupo ->
                    InscripcionesGrupoCard(
                        grupo = grupo,
                        isLoading = uiState is InscripcionUiState.Loading,
                        onInscribirse = { onInscribirse(grupo.id) }
                    )
                }
            }
        }
    }
}

/**
 * Sección de boleta de inscripción.
 * 
 * Organismo que muestra la lista de materias inscritas con horarios.
 */
@Composable
private fun InscripcionesBoletaSection(
    boletas: List<Boleta>
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
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Mi Boleta de Inscripción",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (boletas.isEmpty()) {
            InscripcionesEmptyState(
                icon = Icons.Default.Description,
                message = "No tienes materias inscritas"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(boletas) { boleta ->
                    InscripcionesBoletaCard(boleta = boleta)
                }
            }
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Card de grupo para inscripción.
 * 
 * Molécula que muestra la información del grupo y botón de acción.
 */
@Composable
private fun InscripcionesGrupoCard(
    grupo: Grupo,
    isLoading: Boolean,
    onInscribirse: () -> Unit
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Book,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Grupo ${grupo.grupo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = grupo.docenteNombre,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Button(
                onClick = onInscribirse,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inscribiendo...",
                        style = MaterialTheme.typography.labelMedium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inscribirse",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/**
 * Card de boleta de inscripción.
 * 
 * Molécula que muestra la información de una materia inscrita con horario.
 */
@Composable
private fun InscripcionesBoletaCard(
    boleta: Boleta
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
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${boleta.materiaNombre} - Grupo ${boleta.grupo}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = boleta.dia,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = boleta.horario,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
private fun InscripcionesEmptyState(
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