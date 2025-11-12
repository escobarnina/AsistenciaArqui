package com.bo.asistenciaapp.presentation.admin

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
import com.bo.asistenciaapp.data.repository.MateriaRepository
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.usecase.MateriaCU
import com.bo.asistenciaapp.domain.viewmodel.MateriaUiState
import com.bo.asistenciaapp.domain.viewmodel.VMMateria
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarMateriasScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val materiaRepository = remember { MateriaRepository(database) }
    val materiaCU = remember { MateriaCU(materiaRepository) }
    
    // ViewModel
    val viewModel: VMMateria = viewModel {
        VMMateria(materiaCU)
    }
    
    val materias by viewModel.materias.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is MateriaUiState.Success -> {
                if (state.mensaje != null) {
                    scope.launch { snackbarHostState.showSnackbar(state.mensaje) }
                }
            }
            is MateriaUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.mensaje) }
            }
            else -> {}
        }
    }

    var nombre by remember { mutableStateOf("") }
    var sigla by remember { mutableStateOf("") }
    var nivel by remember { mutableStateOf("") }

    // Edicion
    var editMateria by remember { mutableStateOf<Materia?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editSigla by remember { mutableStateOf("") }
    var editNivel by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Gestión de Materias", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            // Formulario para Materias
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sigla, onValueChange = { sigla = it },
                label = { Text("Sigla") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nivel, onValueChange = { nivel = it },
                label = { Text("Nivel") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && sigla.isNotBlank() && nivel.isNotBlank()) {
                        viewModel.agregarMateria(nombre, sigla, nivel.toInt())
                        nombre = ""; sigla = ""; nivel = ""
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Completa todos los campos") }
                    }
                },
                enabled = uiState !is MateriaUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState is MateriaUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Agregar Materia")
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Lista de Materias
            LazyColumn {
                items(materias) { u: Materia ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                editMateria = u
                                editNombre = u.nombre
                                editSigla = u.sigla
                                editNivel = u.nivel.toString()
                            },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${u.sigla} - ${u.nombre}")
                        IconButton(
                            onClick = { viewModel.eliminarMateria(u.id) },
                            enabled = uiState !is MateriaUiState.Loading
                        ) {
                            Icon(Icons.Default.Delete, "Eliminar")
                        }
                    }
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }


}