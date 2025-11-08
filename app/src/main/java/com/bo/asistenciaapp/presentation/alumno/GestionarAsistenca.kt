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
import com.bo.asistenciaapp.data.local.UserSession
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarAsistencia(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { UserSession(context) }
    val db = remember { AppDatabase(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val alumnoId = session.getUserId()

    // grupos en los que está inscrito
    var misGrupos by remember { mutableStateOf(db.obtenerAsistenciasPorAlumno(alumnoId)) }
    var misAsistencias by remember { mutableStateOf(db.obtenerAsistenciasPorAlumno(alumnoId)) }

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
                items(misGrupos) { grupo ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Grupo ${grupo.materiaNombre} ${grupo.grupo}")
                        Button(onClick = {
                            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            if (db.puedeMarcarAsistencia(alumnoId, grupo.id)) {
                                db.registrarAsistencia(alumnoId, grupo.id, fecha)
                                misAsistencias = db.obtenerAsistenciasPorAlumno(alumnoId)
                                scope.launch {
                                    snackbar.showSnackbar("Asistencia registrada")
                                }
                            } else {
                                scope.launch {
                                    snackbar.showSnackbar("No puedes marcar: fuera de horario o no inscrito")
                                }
                            }
                        }) {
                            Text("Marcar")
                        }
                    }
                    Divider()
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Mis Asistencias", style = MaterialTheme.typography.titleMedium)
            misAsistencias.forEach { a ->
                Text("Grupo ${a.grupoId} - ${a.fecha}")
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}