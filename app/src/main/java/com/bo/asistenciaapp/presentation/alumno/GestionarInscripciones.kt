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
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.InscripcionRepository
import com.bo.asistenciaapp.domain.model.Boleta
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.viewmodel.InscripcionUiState
import com.bo.asistenciaapp.domain.viewmodel.VMInscripcion
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarInscripciones(
    alumnoId: Int,
    semestreActual: Int,
    gestionActual: Int,
    onBack: () -> Unit) {
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

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Inscripción de Materias", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(grupos) { g ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${g.materiaNombre} ${g.grupo} - Docente ${g.docenteNombre}")
                        Button(
                            onClick = {
                                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(Date())
                                viewModel.registrarInscripcion(g.id, fecha, semestreActual, gestionActual)
                            },
                            enabled = uiState !is InscripcionUiState.Loading
                        ) {
                            if (uiState is InscripcionUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text("Inscribirse")
                            }
                        }
                    }
                    Divider()
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Boleta de inscripcion", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(boletas) { b ->
                    Text("${b.materiaNombre} ${b.grupo} - ${b.dia} (${b.horario})")
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}