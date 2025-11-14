package com.bo.asistenciaapp.presentation.alumno

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
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.viewmodel.GrupoConHorariosInscripcion
import com.bo.asistenciaapp.domain.viewmodel.InscripcionUiState
import com.bo.asistenciaapp.domain.viewmodel.VMInscripcion
import com.bo.asistenciaapp.presentation.common.ToastUtils
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para gestionar las inscripciones del estudiante.
 * 
 * Permite al estudiante:
 * - Inscribirse en grupos disponibles
 * 
 * Nota: Para ver la boleta de inscripción, usar la pantalla VerBoletaScreen.
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Cards de grupo)
 * - Organisms: Secciones completas (Lista de grupos disponibles)
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
    val horarioRepository = remember { com.bo.asistenciaapp.data.repository.HorarioRepository(database) }
    val inscripcionCU = remember { InscripcionCU(inscripcionRepository) }
    val grupoCU = remember { GrupoCU(grupoRepository) }
    
    // ViewModel
    val viewModel: VMInscripcion = viewModel {
        VMInscripcion(inscripcionCU, grupoCU, alumnoId, horarioRepository)
    }
    
    val gruposDisponibles by viewModel.gruposDisponibles.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is InscripcionUiState.Success -> {
                if (state.mensaje != null) {
                    ToastUtils.mostrarSuperior(context, state.mensaje)
                }
            }
            is InscripcionUiState.Error -> {
                ToastUtils.mostrarSuperior(context, state.mensaje)
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
            gruposDisponibles = gruposDisponibles,
            uiState = uiState,
            semestreActual = semestreActual,
            gestionActual = gestionActual,
            onInscribirse = { grupoId ->
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                viewModel.registrarInscripcion(grupoId, fecha, semestreActual, gestionActual)
            }
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla de gestión de inscripciones.
 * 
 * Organismo que muestra:
 * - Grupos disponibles para inscribirse
 */
@Composable
private fun GestionarInscripcionesContent(
    paddingValues: PaddingValues,
    gruposDisponibles: List<GrupoConHorariosInscripcion>,
    uiState: InscripcionUiState,
    semestreActual: Int,
    gestionActual: Int,
    onInscribirse: (Int) -> Unit
) {
    InscripcionesGruposDisponiblesSection(
        paddingValues = paddingValues,
        grupos = gruposDisponibles,
        uiState = uiState,
        onInscribirse = onInscribirse
    )
}

/**
 * Sección de grupos disponibles para inscripción.
 * 
 * Organismo que muestra la lista de grupos disponibles con información de cupos y horarios.
 */
@Composable
private fun InscripcionesGruposDisponiblesSection(
    paddingValues: PaddingValues,
    grupos: List<GrupoConHorariosInscripcion>,
    uiState: InscripcionUiState,
    onInscribirse: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header de la sección
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Grupos Disponibles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Estado vacío o lista de grupos
        if (grupos.isEmpty()) {
            item {
                InscripcionesEmptyState(
                    icon = Icons.Default.Book,
                    message = "No hay grupos disponibles para inscripción"
                )
            }
        } else {
            items(grupos) { grupoConHorarios ->
                InscripcionesGrupoDisponibleCard(
                    grupoConHorarios = grupoConHorarios,
                    isLoading = uiState is InscripcionUiState.Loading,
                    onInscribirse = { onInscribirse(grupoConHorarios.grupo.id) }
                )
            }
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Card de grupo disponible para inscripción.
 * 
 * Molécula que muestra la información del grupo con cupos disponibles y horarios.
 */
@Composable
private fun InscripcionesGrupoDisponibleCard(
    grupoConHorarios: GrupoConHorariosInscripcion,
    isLoading: Boolean,
    onInscribirse: () -> Unit
) {
    val grupo = grupoConHorarios.grupo
    val cuposDisponibles = grupo.capacidad - grupo.nroInscritos
    val tieneCupo = cuposDisponibles > 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
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
                        text = grupo.materiaNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Grupo ${grupo.grupo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = grupo.docenteNombre,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Información de cupos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (tieneCupo) Icons.Default.Group else Icons.Default.GroupOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (tieneCupo) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (tieneCupo) {
                        "$cuposDisponibles cupos disponibles (${grupo.nroInscritos}/${grupo.capacidad} inscritos)"
                    } else {
                        "Grupo lleno (${grupo.capacidad}/${grupo.capacidad} inscritos)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (tieneCupo) FontWeight.Normal else FontWeight.Medium,
                    color = if (tieneCupo) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error
                )
            }
            
            // Horarios
            if (grupoConHorarios.horarios.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Horarios:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    grupoConHorarios.horarios.forEach { horario ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "${horario.dia}: ${horario.horaInicio} - ${horario.horaFin}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Sin horarios asignados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            Button(
                onClick = onInscribirse,
                enabled = !isLoading && tieneCupo,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        text = if (tieneCupo) "Inscribirse" else "Sin cupos",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
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
            containerColor = MaterialTheme.colorScheme.surface
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
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}