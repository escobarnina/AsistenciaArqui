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
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.presentation.common.UserLayout

/**
 * Pantalla para mostrar los grupos asignados al docente.
 * 
 * Permite al docente ver todos sus grupos y navegar a ver los estudiantes
 * inscritos en cada grupo.
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
        if (isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (gruposDocente.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No tienes grupos asignados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gruposDocente) { grupo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onVerEstudiantes(grupo.id) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = grupo.materiaNombre,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Grupo: ${grupo.grupo}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Semestre: ${grupo.semestre} - Gestión: ${grupo.gestion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Inscritos: ${grupo.nroInscritos}/${grupo.capacidad}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

