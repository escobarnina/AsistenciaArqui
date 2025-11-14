package com.bo.asistenciaapp.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Layout común para todas las pantallas de usuario (Admin, Docente, Alumno).
 * 
 * Proporciona una estructura consistente con:
 * - TopAppBar con título y navegación
 * - Manejo del tema Material Design 3
 * - Espaciado y padding consistente
 * - Botón de logout opcional
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (TopAppBar, Botones de acción)
 * - Organisms: Layouts completos (UserLayout, HomeLayout)
 * 
 * @param title Título de la pantalla
 * @param showBackButton Si es true, muestra botón de retroceso
 * @param showLogout Si es true, muestra botón de logout en el TopAppBar
 * @param onBack Callback cuando se presiona el botón de retroceso
 * @param onLogout Callback cuando se presiona el botón de logout
 * @param actions Acciones adicionales para el TopAppBar
 * @param content Contenido principal de la pantalla
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLayout(
    title: String,
    showBackButton: Boolean = false,
    showLogout: Boolean = false,
    onBack: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            UserTopAppBar(
                title = title,
                showBackButton = showBackButton,
                showLogout = showLogout,
                onBack = onBack,
                onLogout = onLogout,
                actions = actions
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        UserLayoutContent(
            paddingValues = paddingValues,
            content = content
        )
    }
}

/**
 * Layout simplificado para pantallas home sin TopAppBar.
 * 
 * Útil para pantallas principales donde el contenido necesita
 * más espacio y el título está integrado en el contenido.
 * 
 * @param content Contenido principal de la pantalla
 */
@Composable
fun HomeLayout(
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        UserLayoutContent(
            paddingValues = paddingValues,
            content = content
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * TopAppBar personalizado para pantallas de usuario.
 * 
 * Organismo que combina título, navegación y acciones en una barra superior consistente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserTopAppBar(
    title: String,
    showBackButton: Boolean,
    showLogout: Boolean,
    onBack: (() -> Unit)?,
    onLogout: (() -> Unit)?,
    actions: @Composable (RowScope.() -> Unit)
) {
    TopAppBar(
        title = {
            UserTopAppBarTitle(title = title)
        },
        navigationIcon = {
            if (showBackButton && onBack != null) {
                UserBackButton(onClick = onBack)
            }
        },
        actions = {
            actions()
            if (showLogout && onLogout != null) {
                UserLogoutButton(onClick = onLogout)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * Contenedor principal del contenido del layout.
 * 
 * Organismo que maneja el padding y espaciado del contenido.
 * El contenido recibe los paddingValues para aplicarlos según necesite.
 */
@Composable
private fun UserLayoutContent(
    paddingValues: PaddingValues,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        content(paddingValues)
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Título del TopAppBar.
 * 
 * Molécula que muestra el título con estilo consistente.
 */
@Composable
private fun UserTopAppBarTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
}

/**
 * Botón de retroceso para el TopAppBar.
 * 
 * Molécula que combina un IconButton con el icono de flecha hacia atrás.
 */
@Composable
private fun UserBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Volver"
        )
    }
}

/**
 * Botón de logout para el TopAppBar.
 * 
 * Molécula que combina un TextButton con el texto "Salir".
 */
@Composable
private fun UserLogoutButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text("Salir")
    }
}

