package com.bo.asistenciaapp.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.presentation.common.HomeLayout

/**
 * Pantalla principal del administrador.
 * 
 * Muestra las opciones disponibles para el administrador:
 * - Gestión de Usuarios
 * - Gestión de Materias
 * - Gestión de Grupos
 * - Gestión de Horarios
 * - Gestión de Inscripciones
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Botones de acción, Cards)
 * - Organisms: Secciones completas (Header, Menu de opciones)
 * 
 * @param onLogout Callback cuando se presiona cerrar sesión
 * @param onGestionarUsuarios Callback cuando se presiona gestión de usuarios
 * @param onGestionarMaterias Callback cuando se presiona gestión de materias
 * @param onGestionarGrupos Callback cuando se presiona gestión de grupos
 * @param onGestionarHorarios Callback cuando se presiona gestión de horarios
 * @param onGestionarInscripciones Callback cuando se presiona gestión de inscripciones
 */
@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit,
    onGestionarUsuarios: () -> Unit,
    onGestionarMaterias: () -> Unit,
    onGestionarGrupos: () -> Unit,
    onGestionarHorarios: () -> Unit,
    onGestionarInscripciones: () -> Unit
) {
    HomeLayout { paddingValues ->
        AdminHomeContent(
            paddingValues = paddingValues,
            onLogout = onLogout,
            onGestionarUsuarios = onGestionarUsuarios,
            onGestionarMaterias = onGestionarMaterias,
            onGestionarGrupos = onGestionarGrupos,
            onGestionarHorarios = onGestionarHorarios,
            onGestionarInscripciones = onGestionarInscripciones
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla home del administrador.
 * 
 * Organismo que combina el header y el menú de opciones.
 */
@Composable
private fun AdminHomeContent(
    paddingValues: PaddingValues,
    onLogout: () -> Unit,
    onGestionarUsuarios: () -> Unit,
    onGestionarMaterias: () -> Unit,
    onGestionarGrupos: () -> Unit,
    onGestionarHorarios: () -> Unit,
    onGestionarInscripciones: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AdminHomeHeader()
        Spacer(modifier = Modifier.height(8.dp))
        AdminHomeMenu(
            onGestionarUsuarios = onGestionarUsuarios,
            onGestionarMaterias = onGestionarMaterias,
            onGestionarGrupos = onGestionarGrupos,
            onGestionarHorarios = onGestionarHorarios,
            onGestionarInscripciones = onGestionarInscripciones,
            onLogout = onLogout
        )
    }
}

/**
 * Menú de opciones del administrador.
 * 
 * Organismo que agrupa todas las acciones disponibles.
 */
@Composable
private fun AdminHomeMenu(
    onGestionarUsuarios: () -> Unit,
    onGestionarMaterias: () -> Unit,
    onGestionarGrupos: () -> Unit,
    onGestionarHorarios: () -> Unit,
    onGestionarInscripciones: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdminActionCard(
            title = "Gestión de Usuarios",
            description = "Administrar usuarios del sistema",
            icon = Icons.Default.People,
            onClick = onGestionarUsuarios
        )
        
        AdminActionCard(
            title = "Gestión de Materias",
            description = "Administrar materias académicas",
            icon = Icons.Default.Book,
            onClick = onGestionarMaterias
        )
        
        AdminActionCard(
            title = "Gestión de Grupos",
            description = "Administrar grupos de materias",
            icon = Icons.Default.School,
            onClick = onGestionarGrupos
        )
        
        AdminActionCard(
            title = "Gestión de Horarios",
            description = "Administrar horarios de clases",
            icon = Icons.Default.Schedule,
            onClick = onGestionarHorarios
        )
        
        AdminActionCard(
            title = "Gestión de Inscripciones",
            description = "Ver inscripciones de estudiantes",
            icon = Icons.AutoMirrored.Filled.Assignment,
            onClick = onGestionarInscripciones
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        AdminLogoutButton(onClick = onLogout)
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Header de la pantalla home del administrador.
 * 
 * Molécula que muestra el título y mensaje de bienvenida.
 */
@Composable
private fun AdminHomeHeader() {
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
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Text(
            text = "Panel de Administración",
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
 * Card de acción para el menú del administrador.
 * 
 * Molécula que combina icono, título, descripción y acción en un card interactivo.
 */
@Composable
private fun AdminActionCard(
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
private fun AdminLogoutButton(onClick: () -> Unit) {
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
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
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





