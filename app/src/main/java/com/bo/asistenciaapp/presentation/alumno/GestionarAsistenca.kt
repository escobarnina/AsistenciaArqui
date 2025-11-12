package com.bo.asistenciaapp.presentation.alumno

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.bo.asistenciaapp.domain.viewmodel.AsistenciaUiState
import com.bo.asistenciaapp.domain.viewmodel.VMAsistencia
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Marcar Asistencia", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(grupos) { grupo ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${grupo.materiaNombre} - Grupo ${grupo.grupo}")
                        Button(
                            onClick = {
                                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                viewModel.marcarAsistencia(grupo.id, fecha)
                            },
                            enabled = uiState !is AsistenciaUiState.Loading
                        ) {
                            if (uiState is AsistenciaUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text("Marcar")
                            }
                        }
                    }
                    Divider()
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Mis Asistencias", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(asistencias) { a ->
                    Text("${a.materiaNombre} - ${a.grupo} - ${a.fecha}")
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}