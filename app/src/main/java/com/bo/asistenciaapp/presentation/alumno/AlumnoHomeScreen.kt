package com.bo.asistenciaapp.presentation.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bo.asistenciaapp.presentation.common.HomeLayout

/**
 * Pantalla principal del estudiante.
 * 
 * Muestra las opciones principales disponibles para el alumno:
 * - Gestionar inscripciones
 * - Marcar asistencia
 * - Cerrar sesión
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Botones de acción, Cards)
 * - Organisms: Secciones completas (Header, Menu de opciones)
 * 
 * @param navController Controlador de navegación
 * @param onLogout Callback cuando se presiona cerrar sesión
 * @param onGestionarInscripciones Callback cuando se presiona gestionar inscripciones
 */
@Composable
fun AlumnoHomeScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    onGestionarInscripciones: () -> Unit,
) {
    HomeLayout { paddingValues ->
        AlumnoHomeContent(
            paddingValues = paddingValues,
            navController = navController,
            onLogout = onLogout,
            onGestionarInscripciones = onGestionarInscripciones
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla home del estudiante.
 * 
 * Organismo que combina el header y el menú de opciones.
 */
@Composable
private fun AlumnoHomeContent(
    paddingValues: PaddingValues,
    navController: NavHostController,
    onLogout: () -> Unit,
    onGestionarInscripciones: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AlumnoHomeHeader()
        Spacer(modifier = Modifier.height(8.dp))
        AlumnoHomeMenu(
            navController = navController,
            onGestionarInscripciones = onGestionarInscripciones,
            onLogout = onLogout
        )
    }
}

/**
 * Menú de opciones del estudiante.
 * 
 * Organismo que agrupa todas las acciones disponibles.
 */
@Composable
private fun AlumnoHomeMenu(
    navController: NavHostController,
    onGestionarInscripciones: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AlumnoActionCard(
            title = "Gestionar Inscripciones",
            description = "Inscríbete en materias y grupos",
            icon = Icons.Default.School,
            onClick = onGestionarInscripciones
        )
        
        AlumnoActionCard(
            title = "Marcar Asistencia",
            description = "Registra tu asistencia a clases",
            icon = Icons.Default.CheckCircle,
            onClick = { navController.navigate("gestionarAsistencias") }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        AlumnoLogoutButton(onClick = onLogout)
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Header de la pantalla home del estudiante.
 * 
 * Molécula que muestra el título y mensaje de bienvenida.
 */
@Composable
private fun AlumnoHomeHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Text(
            text = "Panel del Estudiante",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Card de acción para el menú del estudiante.
 * 
 * Molécula que combina icono, título, descripción y acción en un card interactivo.
 */
@Composable
private fun AlumnoActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Botón de cerrar sesión.
 * 
 * Molécula que combina un OutlinedButton con icono de logout.
 */
@Composable
private fun AlumnoLogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        )
    ) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Cerrar sesión",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}