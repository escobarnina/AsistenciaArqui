package com.bo.asistenciaapp.presentation.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun AlumnoHomeScreen(
    navController: NavHostController,
    onLogout:() -> Unit,
    onGestionarInscripciones:() -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bienvenido alumno", style = MaterialTheme.typography.headlineSmall)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Button(onClick = onGestionarInscripciones, modifier = Modifier.fillMaxWidth()) {
                Text("Gestionar Inscripciones")
            }
            Button(onClick = { navController.navigate("gestionarAsistencias") }) {
                Text("Marcar Asistencia")
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesion")
            }
        }
    }
}