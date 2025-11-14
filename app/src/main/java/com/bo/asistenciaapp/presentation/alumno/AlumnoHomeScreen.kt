package com.bo.asistenciaapp.presentation.alumno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.data.repository.UsuarioRepository
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.presentation.common.HomeLayout

/**
 * Pantalla principal del estudiante.
 * 
 * Muestra las opciones principales disponibles para el alumno:
 * - Ver boleta de inscripción
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
 * @param onVerBoleta Callback cuando se presiona ver boleta
 */
@Composable
fun AlumnoHomeScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    onGestionarInscripciones: () -> Unit,
    onVerBoleta: () -> Unit,
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val database = remember { AppDatabase.getInstance(context) }
    val usuarioRepository = remember { UsuarioRepository(database) }
    
    val userId = userSession.getUserId()
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    
    LaunchedEffect(userId) {
        if (userId != -1) {
            usuario = usuarioRepository.obtenerPorId(userId)
        }
    }
    
    HomeLayout { paddingValues ->
        AlumnoHomeContent(
            paddingValues = paddingValues,
            navController = navController,
            usuario = usuario,
            onLogout = onLogout,
            onGestionarInscripciones = onGestionarInscripciones,
            onVerBoleta = onVerBoleta
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
    usuario: Usuario?,
    onLogout: () -> Unit,
    onGestionarInscripciones: () -> Unit,
    onVerBoleta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AlumnoHomeHeader(usuario = usuario)
        Spacer(modifier = Modifier.height(8.dp))
        AlumnoHomeMenu(
            navController = navController,
            onGestionarInscripciones = onGestionarInscripciones,
            onVerBoleta = onVerBoleta,
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
    onVerBoleta: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AlumnoActionCard(
            title = "Ver Boleta",
            description = "Consulta tus materias inscritas",
            icon = Icons.Default.Description,
            onClick = onVerBoleta
        )
        
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
 * Molécula que muestra el título, datos del usuario y mensaje de bienvenida.
 */
@Composable
private fun AlumnoHomeHeader(usuario: Usuario?) {
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
        
        if (usuario != null) {
            Text(
                text = "${usuario.nombres} ${usuario.apellidos}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Registro: ${usuario.registro}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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