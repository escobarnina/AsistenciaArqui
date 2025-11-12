package com.bo.asistenciaapp.presentation.docente

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.presentation.common.HomeLayout

/**
 * Pantalla principal del docente.
 * 
 * Muestra las opciones disponibles para el docente:
 * - Ver sus grupos asignados
 * - Marcar asistencias de estudiantes
 * - Ver estadísticas de asistencia
 */
@Composable
fun DocenteHomeScreen(
    onLogout: () -> Unit,
    onVerGrupos: () -> Unit,
    onMarcarAsistencias: () -> Unit
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
                text = "Panel del Docente",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Button(
                onClick = onVerGrupos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mis Grupos")
            }
            
            Button(
                onClick = onMarcarAsistencias,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Marcar Asistencias")
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

