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
import com.bo.asistenciaapp.data.repository.HorarioRepository
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.viewmodel.GrupoConHorariosInscripcion
import com.bo.asistenciaapp.domain.viewmodel.VMInscripcion
import com.bo.asistenciaapp.presentation.common.UserLayout

/**
 * Pantalla para visualizar la boleta de inscripción del estudiante.
 * 
 * Muestra todas las materias en las que el alumno está inscrito con sus horarios.
 * Esta pantalla es solo de visualización (read-only).
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Cards de materia inscrita)
 * - Organisms: Secciones completas (Lista de boleta)
 * 
 * @param alumnoId ID del estudiante
 * @param onBack Callback cuando se presiona el botón de retroceso
 */
@Composable
fun VerBoletaScreen(
    alumnoId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val inscripcionRepository = remember { InscripcionRepository(database) }
    val grupoRepository = remember { com.bo.asistenciaapp.data.repository.GrupoRepository(database) }
    val horarioRepository = remember { HorarioRepository(database) }
    val inscripcionCU = remember { InscripcionCU(inscripcionRepository) }
    val grupoCU = remember { com.bo.asistenciaapp.domain.usecase.GrupoCU(grupoRepository) }
    
    // ViewModel
    val viewModel: VMInscripcion = viewModel {
        VMInscripcion(inscripcionCU, grupoCU, alumnoId, horarioRepository)
    }
    
    val gruposInscritos by viewModel.gruposInscritos.collectAsState()

    UserLayout(
        title = "Mi Boleta de Inscripción",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        VerBoletaContent(
            paddingValues = paddingValues,
            gruposInscritos = gruposInscritos
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla de boleta.
 * 
 * Organismo que muestra la lista de materias inscritas con horarios.
 */
@Composable
private fun VerBoletaContent(
    paddingValues: PaddingValues,
    gruposInscritos: List<GrupoConHorariosInscripcion>
) {
    if (gruposInscritos.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            BoletaEmptyState()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(gruposInscritos) { grupoConHorarios ->
                BoletaMateriaCard(
                    grupoConHorarios = grupoConHorarios
                )
            }
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Card que muestra una materia inscrita con sus horarios.
 * 
 * Molécula que muestra la información completa de una materia inscrita.
 */
@Composable
private fun BoletaMateriaCard(
    grupoConHorarios: GrupoConHorariosInscripcion
) {
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
            
            // Información del docente
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Docente: ${grupoConHorarios.grupo.docenteNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Horarios
            if (grupoConHorarios.horarios.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Horarios:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    for (horario in grupoConHorarios.horarios) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
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
        }
    }
}

/**
 * Estado vacío cuando no hay materias inscritas.
 * 
 * Molécula que muestra un mensaje cuando la boleta está vacía.
 */
@Composable
private fun BoletaEmptyState() {
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
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "No tienes materias inscritas",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Inscríbete en grupos desde la sección de inscripciones",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

