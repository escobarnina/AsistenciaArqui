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
import com.bo.asistenciaapp.data.local.AppDatabase
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
    val db = remember { AppDatabase(context) }

    var grupos by remember { mutableStateOf(db.obtenerGrupos()) }
    var boletas by remember { mutableStateOf(db.obtenerBoletasPorAlumno(alumnoId)) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                        Button(onClick = {
                            try {
                                if (db.tieneCruceDeHorario(alumnoId, g.id)) {
                                    scope.launch {
                                        snackbar.showSnackbar("Cruce de horario detectado")
                                    }
                                } else {
                                    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(Date())
                                    db.registrarBoleta(alumnoId, g.id, fecha, semestreActual, gestionActual)
                                    boletas = db.obtenerBoletasPorAlumno(alumnoId)
                                    scope.launch {
                                        snackbar.showSnackbar("Inscripción realizada")
                                    }
                                }
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbar.showSnackbar("Error: ${e.message}")
                                }
                            }
                        }) {
                            Text("Inscribirse")
                        }
                    }
                    Divider()
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Boleta de inscripcion", style = MaterialTheme.typography.titleMedium)
            boletas.forEach { b ->
                Text("${b.materiaNombre} ${b.grupo} - ${b.dia} (${b.horario})")
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}