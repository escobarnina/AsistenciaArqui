package com.bo.asistenciaapp.presentation.docente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Usuario
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
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                snackbarHostState.showSnackbar("Error al cargar estudiantes: ${e.message ?: "Error desconocido"}")
            } finally {
                isLoadingEstudiantes.value = false
            }
        } ?: run {
            estudiantesGrupo.clear()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Marcar Asistencias") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Selector de grupo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Seleccionar Grupo",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (isLoading.value) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        gruposDocente.forEach { grupo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = grupo.materiaNombre,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Grupo ${grupo.grupo} - Semestre ${grupo.semestre}/${grupo.gestion}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = grupoSeleccionado?.id == grupo.id,
                                    onClick = { grupoSeleccionado = grupo }
                                )
                            }
                        }
                    }
                }
            }
            
            // Lista de estudiantes
            if (grupoSeleccionado != null) {
                if (isLoadingEstudiantes.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (estudiantesGrupo.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
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
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(estudiantesGrupo) { estudiante ->
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
                                        onClick = {
                                            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                            scope.launch {
                                                try {
                                                    asistenciaRepository.registrar(estudiante.id, grupoSeleccionado!!.id, fecha)
                                                    snackbarHostState.showSnackbar("Asistencia marcada para ${estudiante.nombres}")
                                                } catch (e: Exception) {
                                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Marcar")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecciona un grupo para ver los estudiantes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

