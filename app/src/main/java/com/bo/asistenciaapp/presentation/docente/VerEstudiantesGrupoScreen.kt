package com.bo.asistenciaapp.presentation.docente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
 * Permite al docente ver la lista de estudiantes inscritos en un grupo
 * y acceder a marcar sus asistencias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerEstudiantesGrupoScreen(
    grupoId: Int,
    grupoNombre: String,
    onBack: () -> Unit,
    onMarcarAsistencia: (Int, Int) -> Unit
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
        title = "Estudiantes - ${nombreGrupo.value}",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        if (isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (estudiantes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay estudiantes inscritos en este grupo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(estudiantes) { estudiante ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${estudiante.nombres} ${estudiante.apellidos}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Registro: ${estudiante.registro}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { onMarcarAsistencia(estudiante.id, grupoId) }
                            ) {
                                Text("Marcar")
                            }
                        }
                    }
                }
            }
        }
    }
}

