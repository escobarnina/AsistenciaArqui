package com.bo.asistenciaapp.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun AdminHomeScreen(
    onLogout:() -> Unit,
    onGestionarUsuarios:() -> Unit,
    onGestionarMaterias:() -> Unit,
    onGestionarGrupos:() -> Unit,
    onGestionarHorarios:() -> Unit,
    onGestionarInscripciones:() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Panel de Administracion", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = onGestionarUsuarios, modifier = Modifier.fillMaxWidth()) {
            Text("Gestion de Usuarios")
        }
        Button(onClick = onGestionarMaterias, modifier = Modifier.fillMaxWidth()) {
            Text("Gestion de Materias")
        }
        Button(onClick = onGestionarGrupos, modifier = Modifier.fillMaxWidth()) {
            Text("Gestion de Grupos")
        }
        Button(onClick = onGestionarHorarios, modifier = Modifier.fillMaxWidth()) {
            Text("Gestion de Horarios")
        }
        Button(onClick = onGestionarInscripciones, modifier = Modifier.fillMaxWidth()) {
            Text("Gestion de Inscripciones")
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesion")
        }
    }
}





