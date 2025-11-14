package com.bo.asistenciaapp.presentation.docente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.data.repository.UsuarioRepository
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.ExportarAsistenciaCU
import com.bo.asistenciaapp.presentation.common.HomeLayout

/**
 * Pantalla principal del docente.
 * 
 * Muestra las opciones disponibles para el docente:
 * - Ver sus grupos asignados
 * - Exportar asistencias (usando Patrón Adapter)
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Botones de acción, Cards)
 * - Organisms: Secciones completas (Header, Menu de opciones)
 * 
 * @param onLogout Callback cuando se presiona cerrar sesión
 * @param onVerGrupos Callback cuando se presiona ver grupos
 */
@Composable
fun DocenteHomeScreen(
    onLogout: () -> Unit,
    onVerGrupos: () -> Unit
) {
    val context = LocalContext.current
    
    // Inicializar dependencias para exportación (Patrón Adapter)
    val db = remember { AppDatabase.getInstance(context) }
    val asistenciaRepository = remember { AsistenciaRepository(db) }
    val exportarCU = remember { ExportarAsistenciaCU(asistenciaRepository) }
    
    // Obtener sesión del usuario para el ID del docente
    val userSession = remember { UserSession(context) }
    val idDocente = userSession.getUserId()
    val usuarioRepository = remember { UsuarioRepository(db) }
    
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    
    LaunchedEffect(idDocente) {
        if (idDocente != -1) {
            usuario = usuarioRepository.obtenerPorId(idDocente)
        }
    }
    
    // Estado para controlar el diálogo de exportación
    var mostrarDialogoExportar by remember { mutableStateOf(false) }
    
    HomeLayout { paddingValues ->
        DocenteHomeContent(
            paddingValues = paddingValues,
            usuario = usuario,
            onLogout = onLogout,
            onVerGrupos = onVerGrupos,
            onExportarAsistencias = {
                // Al hacer clic, necesitamos seleccionar un grupo primero
                // Por ahora, abrimos el diálogo directamente
                // TODO: En el futuro, mostrar selector de grupos
                mostrarDialogoExportar = true
            }
        )
    }
    
    // Diálogo de exportación (Patrón Adapter)
    if (mostrarDialogoExportar) {
        // Obtener los grupos del docente
        val grupos = remember { db.grupoDao.obtenerPorDocente(idDocente) }
        
        if (grupos.isNotEmpty()) {
            ExportarAsistenciasDialog(
                idGrupo = grupos.first().id, // Primer grupo por defecto
                exportarCU = exportarCU,
                onDismiss = { mostrarDialogoExportar = false }
            )
        } else {
            // Mostrar mensaje si no hay grupos
            AlertDialog(
                onDismissRequest = { mostrarDialogoExportar = false },
                title = { Text("Sin grupos asignados") },
                text = { Text("No tienes grupos asignados para exportar asistencias.") },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogoExportar = false }) {
                        Text("Entendido")
                    }
                }
            )
        }
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Contenido principal de la pantalla home del docente.
 * 
 * Organismo que combina el header y el menú de opciones.
 */
@Composable
private fun DocenteHomeContent(
    paddingValues: PaddingValues,
    usuario: Usuario?,
    onLogout: () -> Unit,
    onVerGrupos: () -> Unit,
    onExportarAsistencias: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DocenteHomeHeader(usuario = usuario)
        DocenteHomeMenu(
            onVerGrupos = onVerGrupos,
            onExportarAsistencias = onExportarAsistencias,
            onLogout = onLogout
        )
    }
}

/**
 * Menú de opciones del docente.
 * 
 * Organismo que agrupa todas las acciones disponibles.
 */
@Composable
private fun DocenteHomeMenu(
    onVerGrupos: () -> Unit,
    onExportarAsistencias: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DocenteActionCard(
            title = "Mis Grupos",
            description = "Ver grupos asignados y estudiantes",
            icon = Icons.Default.School,
            onClick = onVerGrupos
        )
        
        // ⭐ Botón de Exportar Asistencias (Patrón Adapter)
        DocenteActionCard(
            title = "Exportar Asistencias",
            description = "Generar reportes en Excel o PDF",
            icon = Icons.Default.FileDownload,
            onClick = onExportarAsistencias
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        DocenteLogoutButton(onClick = onLogout)
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Header de la pantalla home del docente.
 * 
 * Molécula que muestra el título, datos del usuario y mensaje de bienvenida.
 */
@Composable
private fun DocenteHomeHeader(usuario: Usuario?) {
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
            text = "Panel del Docente",
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
 * Card de acción para el menú del docente.
 * 
 * Molécula que combina icono, título, descripción y acción en un card interactivo.
 */
@Composable
private fun DocenteActionCard(
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
private fun DocenteLogoutButton(onClick: () -> Unit) {
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

