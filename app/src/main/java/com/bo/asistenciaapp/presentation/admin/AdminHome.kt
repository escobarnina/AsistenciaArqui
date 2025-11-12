package com.bo.asistenciaapp.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.presentation.common.HomeLayout

@Composable
fun AdminHomeScreen(
    onLogout:() -> Unit,
    onGestionarUsuarios:() -> Unit,
    onGestionarMaterias:() -> Unit,
    onGestionarGrupos:() -> Unit,
    onGestionarHorarios:() -> Unit,
    onGestionarInscripciones:() -> Unit
) {
    HomeLayout { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Panel de Administración",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Button(
                onClick = onGestionarUsuarios,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestión de Usuarios")
            }
            Button(
                onClick = onGestionarMaterias,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestión de Materias")
            }
            Button(
                onClick = onGestionarGrupos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestión de Grupos")
            }
            Button(
                onClick = onGestionarHorarios,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestión de Horarios")
            }
            Button(
                onClick = onGestionarInscripciones,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestión de Inscripciones")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}





