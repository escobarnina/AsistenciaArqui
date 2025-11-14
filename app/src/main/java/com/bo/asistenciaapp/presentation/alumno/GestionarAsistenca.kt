package com.bo.asistenciaapp.presentation.alumno

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.usecase.AsistenciaCU
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.viewmodel.AsistenciaUiState
import com.bo.asistenciaapp.domain.viewmodel.GrupoConHorarios
import com.bo.asistenciaapp.domain.viewmodel.VMAsistencia
import com.bo.asistenciaapp.presentation.common.ToastUtils
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para gestionar la asistencia del estudiante.
 * 
 * Permite al estudiante:
 * - Marcar asistencia en los grupos en los que está inscrito
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Cards de grupo)
 * - Organisms: Secciones completas (Lista de grupos)
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
    
    val gruposDisponiblesAhora by viewModel.gruposDisponiblesAhora.collectAsState()
    val gruposProximos by viewModel.gruposProximos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // Estado para mostrar el diálogo de estado de asistencia
    var mostrarDialogoEstado by remember { mutableStateOf<String?>(null) }
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AsistenciaUiState.Success -> {
                if (state.estadoAsistencia != null) {
                    // Mostrar diálogo con el estado de asistencia
                    mostrarDialogoEstado = state.estadoAsistencia
                } else if (state.mensaje != null) {
                    ToastUtils.mostrarSuperior(context, state.mensaje)
                }
            }
            is AsistenciaUiState.Error -> {
                ToastUtils.mostrarSuperior(context, state.mensaje)
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
            gruposDisponiblesAhora = gruposDisponiblesAhora,
            gruposProximos = gruposProximos,
            uiState = uiState,
            onMarcarAsistencia = { grupoId ->
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                viewModel.marcarAsistencia(grupoId, fecha)
            }
        )
        
        // Diálogo de estado de asistencia
        mostrarDialogoEstado?.let { estado ->
            AsistenciaEstadoDialog(
                estado = estado,
                onDismiss = { 
                    mostrarDialogoEstado = null
                    viewModel.clearSuccess()
                }
            )
        }
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla de gestión de asistencia.
 * 
 * Organismo que muestra dos listas:
 * - Grupos disponibles ahora (según hora actual)
 * - Grupos próximos con sus horarios
 */
@Composable
private fun GestionarAsistenciaContent(
    paddingValues: PaddingValues,
    gruposDisponiblesAhora: List<GrupoConHorarios>,
    gruposProximos: List<GrupoConHorarios>,
    uiState: AsistenciaUiState,
    onMarcarAsistencia: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AsistenciaGruposDisponiblesSection(
            grupos = gruposDisponiblesAhora,
            uiState = uiState,
            onMarcarAsistencia = onMarcarAsistencia
        )
        
        AsistenciaGruposProximosSection(
            grupos = gruposProximos
        )
    }
}

/**
 * Sección de grupos disponibles para marcar asistencia AHORA.
 * 
 * Organismo que muestra la lista de grupos donde se puede marcar asistencia en este momento.
 */
@Composable
private fun AsistenciaGruposDisponiblesSection(
    grupos: List<GrupoConHorarios>,
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
                text = "Disponibles Ahora",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (grupos.isEmpty()) {
            AsistenciaEmptyState(
                icon = Icons.Default.Schedule,
                message = "No hay grupos disponibles para marcar asistencia en este momento"
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grupos.forEach { grupoConHorarios ->
                    AsistenciaGrupoCard(
                        grupo = grupoConHorarios.grupo,
                        isLoading = uiState is AsistenciaUiState.Loading,
                        onMarcarAsistencia = { onMarcarAsistencia(grupoConHorarios.grupo.id) }
                    )
                }
            }
        }
    }
}

/**
 * Sección de grupos próximos con sus horarios.
 * 
 * Organismo que muestra la lista de grupos próximos con información de días y horarios.
 */
@Composable
private fun AsistenciaGruposProximosSection(
    grupos: List<GrupoConHorarios>
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
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Próximos Grupos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (grupos.isEmpty()) {
            AsistenciaEmptyState(
                icon = Icons.Default.School,
                message = "No hay grupos próximos"
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grupos.forEach { grupoConHorarios ->
                    AsistenciaGrupoProximoCard(
                        grupoConHorarios = grupoConHorarios
                    )
                }
            }
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Card de grupo próximo con información de horarios.
 * 
 * Molécula que muestra la información del grupo y sus horarios.
 */
@Composable
private fun AsistenciaGrupoProximoCard(
    grupoConHorarios: GrupoConHorarios
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            imageVector = Icons.Default.School,
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
                        text = grupoConHorarios.grupo.materiaNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Grupo ${grupoConHorarios.grupo.grupo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

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
            containerColor = MaterialTheme.colorScheme.surface
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Grupo ${grupo.grupo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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

/**
 * Diálogo que muestra el estado de asistencia marcada.
 * 
 * Muestra si el estudiante está PRESENTE, llegó con RETRASO o tiene FALTA.
 */
@Composable
fun AsistenciaEstadoDialog(
    estado: String,
    onDismiss: () -> Unit
) {
    val estadoInfo = when (estado.uppercase()) {
        "PRESENTE" -> {
            EstadoAsistenciaInfo(
                titulo = "✓ Asistencia Registrada",
                mensaje = "Tu asistencia ha sido registrada correctamente.",
                icono = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.primary
            )
        }
        "RETRASO" -> {
            EstadoAsistenciaInfo(
                titulo = "⚠ Retraso Registrado",
                mensaje = "Has llegado con retraso. Tu asistencia ha sido registrada.",
                icono = Icons.Default.Schedule,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        "FALTA" -> {
            EstadoAsistenciaInfo(
                titulo = "✗ Falta Registrada",
                mensaje = "Has llegado muy tarde. Se registró como falta.",
                icono = Icons.Default.Error,
                color = MaterialTheme.colorScheme.error
            )
        }
        else -> {
            EstadoAsistenciaInfo(
                titulo = "Asistencia Registrada",
                mensaje = "Tu asistencia ha sido registrada.",
                icono = Icons.Default.Info,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = estadoInfo.icono,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = estadoInfo.color
            )
        },
        title = {
            Text(
                text = estadoInfo.titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = estadoInfo.mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = estadoInfo.color
                )
            ) {
                Text("Entendido")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Clase auxiliar para información del estado de asistencia
private data class EstadoAsistenciaInfo(
    val titulo: String,
    val mensaje: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)