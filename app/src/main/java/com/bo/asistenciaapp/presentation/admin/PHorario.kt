package com.bo.asistenciaapp.presentation.admin

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Grupo
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import com.bo.asistenciaapp.domain.model.Horario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarHorarios(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase(context) }

    var horarios by remember { mutableStateOf(db.obtenerHorarios()) }
    var grupos by remember { mutableStateOf(db.obtenerGrupos()) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Campos del formulario
    var grupoSeleccionado by remember { mutableStateOf<Grupo?>(null) }
    var expandedGrupo by remember { mutableStateOf(false) }
    val dias = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado")
    var diaSeleccionado by remember { mutableStateOf(dias.first()) }
    var expandedDia by remember { mutableStateOf(false) }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Gestion de Horarios", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            // Selector de Grupo
            ExposedDropdownMenuBox(
                expanded = expandedGrupo,
                onExpandedChange = { expandedGrupo = !expandedGrupo }
            ) {
                OutlinedTextField(
                    value = grupoSeleccionado?.let { "${it.materiaNombre} ${it.grupo}" }  ?: "Seleccionar Grupo",
                    onValueChange = {},
                    label = { Text("Grupo") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGrupo)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expandedGrupo,
                    onDismissRequest = { expandedGrupo = false }
                ) {
                    grupos.forEach { g ->
                        DropdownMenuItem(
                            text = { Text("${g.materiaNombre} ${g.grupo} ${g.docenteNombre}") },
                            onClick = {
                                grupoSeleccionado = g
                                expandedGrupo = false
                            }
                        )
                    }
                }
            }

            // Selector de Dia
            ExposedDropdownMenuBox(
                expanded = expandedDia,
                onExpandedChange = { expandedDia = !expandedDia }
            ) {
                OutlinedTextField(
                    value = diaSeleccionado,
                    onValueChange = {},
                    label = { Text("Dia") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDia)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expandedDia,
                    onDismissRequest = { expandedDia = false }
                ) {
                    dias.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d) },
                            onClick = {
                                diaSeleccionado = d
                                expandedDia = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = horaInicio,
                onValueChange = {horaInicio = it},
                label = {Text("Hora Inicio HH:mm")},
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = horaFin,
                onValueChange = {horaFin = it},
                label = {Text("Hora Fin HH:mm")},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (grupoSeleccionado != null && horaInicio.isNotBlank() && horaFin.isNotBlank()) {
                    try {
                        db.agregarHorario(
                            grupoSeleccionado!!.id,
                            diaSeleccionado,
                            horaInicio,
                            horaFin
                        )
                        horarios = db.obtenerHorarios()
//                        horaInicio = ""; horaFin = ""
                        scope.launch { snackbar.showSnackbar("Horario agregado correctamente") }
                    } catch (e: Exception) {
                        scope.launch { snackbar.showSnackbar("Complete todos los campos") }
                    }
                }
            }, Modifier.fillMaxWidth()) {
                Text("Agregar Horario")
            }
            OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) {
                Text("Volver")
            }

            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(horarios) { h: Horario ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${h.materia} ${h.grupo} - ${h.dia} ${h.horaInicio} - ${h.horaFin}")
                        IconButton(onClick = {
                            db.eliminarHorario(h.id)
                            horarios = db.obtenerHorarios()
                            scope.launch { snackbar.showSnackbar("Horario eliminado") }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    }

}