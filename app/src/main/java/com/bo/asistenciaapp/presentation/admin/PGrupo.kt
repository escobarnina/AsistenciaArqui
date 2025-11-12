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
import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.data.repository.MateriaRepository
import com.bo.asistenciaapp.data.repository.UsuarioRepository
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.MateriaCU
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import com.bo.asistenciaapp.domain.viewmodel.GrupoUiState
import com.bo.asistenciaapp.domain.viewmodel.VMGrupo
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarGruposScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val grupoRepository = remember { GrupoRepository(database) }
    val materiaRepository = remember { MateriaRepository(database) }
    val usuarioRepository = remember { UsuarioRepository(database) }
    val grupoCU = remember { GrupoCU(grupoRepository) }
    val materiaCU = remember { MateriaCU(materiaRepository) }
    val usuarioCU = remember { UsuarioCU(usuarioRepository) }
    
    // ViewModel
    val viewModel: VMGrupo = viewModel {
        VMGrupo(grupoCU, materiaCU, usuarioCU)
    }
    
    val grupos by viewModel.grupos.collectAsState()
    val materias by viewModel.materias.collectAsState()
    val docentes by viewModel.docentes.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GrupoUiState.Success -> {
                if (state.mensaje != null) {
                    scope.launch { snackbarHostState.showSnackbar(state.mensaje) }
                }
            }
            is GrupoUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.mensaje) }
            }
            else -> {}
        }
    }

//    var docente by remember { mutableStateOf("") }
    var semestre by remember { mutableStateOf("") }
    var gestion by remember { mutableStateOf("") }
    var grupo by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var materiaSeleccionada by remember { mutableStateOf<Materia?>(null) }
    var docenteSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var expandedDocente by remember { mutableStateOf(false) }

    // Edicion
    var editGrupo by remember { mutableStateOf<Grupo?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editSigla by remember { mutableStateOf("") }
    var editNivel by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Gestion de Grupos", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height((8.dp)))

            // Formulario para Grupos
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = materiaSeleccionada?.nombre ?: "Seleccionar Materia",
                    onValueChange = {},
                    label = { Text("Materia") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    materias.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.nombre) },
                            onClick = {
                                materiaSeleccionada = m
                                expanded = false
                            }
                        )
                    }
                }
            }
            ExposedDropdownMenuBox(
                expanded = expandedDocente,
                onExpandedChange = { expandedDocente = !expandedDocente }
            ) {
                OutlinedTextField(
                    value = docenteSeleccionado?.nombres ?: "Seleccionar Docente",
                    onValueChange = {},
                    label = { Text("Docente") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDocente)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expandedDocente,
                    onDismissRequest = { expandedDocente = false }
                ) {
                    docentes.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d.nombres) },
                            onClick = {
                                docenteSeleccionado = d
                                expandedDocente = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = semestre, onValueChange = { semestre = it },
                label = { Text("Semestre") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = gestion, onValueChange = { gestion = it },
                label = { Text("Gestion") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = grupo, onValueChange = { grupo = it },
                label = { Text("Grupo") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = capacidad, onValueChange = { capacidad = it },
                label = { Text("Capacidad") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (semestre.isNotBlank() && gestion.isNotBlank() && capacidad.isNotBlank() && 
                        materiaSeleccionada != null && docenteSeleccionado != null && grupo.isNotBlank()) {
                        viewModel.agregarGrupo(
                            materiaSeleccionada!!.id,
                            materiaSeleccionada!!.nombre,
                            docenteSeleccionado!!.id,
                            "${docenteSeleccionado!!.nombres} ${docenteSeleccionado!!.apellidos}",
                            semestre.toInt(),
                            gestion.toInt(),
                            capacidad.toInt(),
                            grupo
                        )
                        semestre = ""; gestion = ""; grupo = ""; capacidad = ""; 
                        materiaSeleccionada = null; docenteSeleccionado = null
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Completa todos los campos") }
                    }
                },
                enabled = uiState !is GrupoUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState is GrupoUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Agregar Grupo")
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Lista de Grupo
            LazyColumn {
                items(grupos) { g: Grupo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                editGrupo = g
//                                editNombre = g.nombre
//                                editSigla = g.sigla
//                                editNivel = g.nivel.toString()
                            },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${g.materiaNombre}, Grupo ${g.grupo} - ${g.docenteNombre}")
                        IconButton(
                            onClick = { viewModel.eliminarGrupo(g.id) },
                            enabled = uiState !is GrupoUiState.Loading
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