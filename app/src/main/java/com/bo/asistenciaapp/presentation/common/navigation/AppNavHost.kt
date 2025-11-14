package com.bo.asistenciaapp.presentation.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.presentation.admin.AdminHomeScreen
import com.bo.asistenciaapp.presentation.admin.GestionarGruposScreen
import com.bo.asistenciaapp.presentation.admin.GestionarHorarios
import com.bo.asistenciaapp.presentation.admin.GestionarMateriasScreen
import com.bo.asistenciaapp.presentation.admin.GestionarUsuariosScreen
import com.bo.asistenciaapp.presentation.alumno.AlumnoHomeScreen
import com.bo.asistenciaapp.presentation.alumno.GestionarAsistencia
import com.bo.asistenciaapp.presentation.alumno.GestionarInscripciones
import com.bo.asistenciaapp.presentation.alumno.VerBoletaScreen
import com.bo.asistenciaapp.presentation.docente.ConfigurarHorariosScreen
import com.bo.asistenciaapp.presentation.docente.DocenteHomeScreen
import com.bo.asistenciaapp.presentation.docente.VerEstudiantesGrupoScreen
import com.bo.asistenciaapp.presentation.docente.VerGruposDocenteScreen
import com.bo.asistenciaapp.presentation.login.LoginScreen

/**
 * Host de navegación principal de la aplicación AsistenciaApp.
 * 
 * Gestiona todas las rutas de navegación para los diferentes roles de usuario:
 * - Login: Pantalla de autenticación
 * - Admin: Pantallas de administración
 * - Docente: Pantallas del docente
 * - Alumno: Pantallas del estudiante
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Organisms: Grupos de rutas por rol (AdminRoutes, DocenteRoutes, AlumnoRoutes)
 * - Molecules: Rutas individuales agrupadas por funcionalidad
 * 
 * Determina automáticamente la pantalla inicial basándose en la sesión del usuario.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val session = remember { UserSession(context) }

    // Obtener información de la sesión del usuario de forma reactiva
    // Usamos mutableStateOf para que se actualice cuando cambie la sesión
    var userId by remember { mutableStateOf(session.getUserId()) }
    var userRol by remember { mutableStateOf(session.getUserRol()) }

    // Función para actualizar el estado de la sesión
    val updateSessionState = {
        userId = session.getUserId()
        userRol = session.getUserRol()
    }

    // Determinar la pantalla inicial según la sesión
    // Se recalcula cada vez que cambian userId o userRol
    val startDestination = remember(userId, userRol) {
        determineStartDestination(userId, userRol)
    }

    NavHost(
        navController = navController, 
        startDestination = startDestination
    ) {
        // Rutas de autenticación
        LoginRoutes(
            navController = navController,
            builder = this,
            session = session,
            onSessionChanged = updateSessionState
        )
        
        // Rutas de administrador
        AdminRoutes(
            navController = navController,
            session = session,
            builder = this,
            onLogout = updateSessionState
        )
        
        // Rutas de docente
        DocenteRoutes(
            navController = navController,
            session = session,
            builder = this,
            onLogout = updateSessionState
        )
        
        // Rutas de alumno
        AlumnoRoutes(
            navController = navController,
            session = session,
            builder = this,
            onLogout = updateSessionState
        )
    }
}

// ============================================================================
// ORGANISMS - Grupos de rutas por rol de usuario
// ============================================================================

/**
 * Rutas de autenticación (Login).
 * 
 * Organismo que agrupa todas las rutas relacionadas con el inicio de sesión.
 */
private fun LoginRoutes(
    navController: androidx.navigation.NavHostController,
    builder: NavGraphBuilder,
    session: UserSession,
    onSessionChanged: () -> Unit
) {
    builder.composable(NavRoutes.Login) {
        LoginScreen(
            onLoginSuccess = { usuario ->
                // La sesión ya se guardó en LoginScreen, actualizar el estado local
                onSessionChanged()
                // Navegar a la pantalla home correspondiente
                navigateToRoleHome(navController, usuario.rol)
            }
        )
    }
}

/**
 * Rutas de administrador.
 * 
 * Organismo que agrupa todas las rutas disponibles para usuarios con rol Admin.
 */
private fun AdminRoutes(
    navController: androidx.navigation.NavHostController,
    session: UserSession,
    builder: NavGraphBuilder,
    onLogout: () -> Unit
) {
    builder.composable(NavRoutes.AdminHome) {
        AdminHomeScreen(
            onLogout = {
                handleLogout(navController, session)
                onLogout() // Actualizar el estado después del logout
            },
            onGestionarUsuarios = { navController.navigate(NavRoutes.GestionUsuarios) },
            onGestionarMaterias = { navController.navigate(NavRoutes.GestionMaterias) },
            onGestionarGrupos = { navController.navigate(NavRoutes.GestionGrupos) },
            onGestionarHorarios = { navController.navigate(NavRoutes.GestionHorarios) },
            onGestionarInscripciones = { navController.navigate(NavRoutes.GestionarInscripciones) }
        )
    }
    
    builder.composable(NavRoutes.GestionUsuarios) {
        GestionarUsuariosScreen(
            onBack = { navController.popBackStack() }
        )
    }
    
    builder.composable(NavRoutes.GestionMaterias) {
        GestionarMateriasScreen(
            onBack = { navController.popBackStack() }
        )
    }
    
    builder.composable(NavRoutes.GestionGrupos) {
        GestionarGruposScreen(
            onBack = { navController.popBackStack() }
        )
    }
    
    builder.composable(NavRoutes.GestionHorarios) {
        GestionarHorarios(
            onBack = { navController.popBackStack() }
        )
    }
    
    builder.composable(NavRoutes.GestionarInscripciones) {
        // Obtener userId dinámicamente de la sesión
        val userId = session.getUserId()
        GestionarInscripciones(
            alumnoId = userId,
            semestreActual = 2,
            gestionActual = 2025,
            onBack = { navController.popBackStack() }
        )
    }
}

/**
 * Rutas de docente.
 * 
 * Organismo que agrupa todas las rutas disponibles para usuarios con rol Docente.
 */
private fun DocenteRoutes(
    navController: androidx.navigation.NavHostController,
    session: UserSession,
    builder: NavGraphBuilder,
    onLogout: () -> Unit
) {
    builder.composable(NavRoutes.DocenteHome) {
        DocenteHomeScreen(
            onLogout = {
                handleLogout(navController, session)
                onLogout() // Actualizar el estado después del logout
            },
            onVerGrupos = { navController.navigate(NavRoutes.DocenteGrupos) }
        )
    }
    
    builder.composable(NavRoutes.DocenteGrupos) {
        VerGruposDocenteScreen(
            onBack = { navController.popBackStack() },
            onVerEstudiantes = { grupoId ->
                navController.navigate("${NavRoutes.DocenteEstudiantes}/$grupoId")
            },
            onConfigurarHorarios = { grupoId ->
                navController.navigate("${NavRoutes.DocenteConfigurarHorarios}/$grupoId")
            }
        )
    }
    
    builder.composable("${NavRoutes.DocenteConfigurarHorarios}/{grupoId}") { backStackEntry ->
        val grupoId = backStackEntry.arguments?.getString("grupoId")?.toIntOrNull() ?: -1
        ConfigurarHorariosScreen(
            grupoId = grupoId,
            onBack = { navController.popBackStack() }
        )
    }
    
    builder.composable("${NavRoutes.DocenteEstudiantes}/{grupoId}") { backStackEntry ->
        val grupoId = backStackEntry.arguments?.getString("grupoId")?.toIntOrNull() ?: -1
        VerEstudiantesGrupoScreen(
            grupoId = grupoId,
            grupoNombre = "",
            onBack = { navController.popBackStack() }
        )
    }
}

/**
 * Rutas de alumno.
 * 
 * Organismo que agrupa todas las rutas disponibles para usuarios con rol Alumno.
 */
private fun AlumnoRoutes(
    navController: androidx.navigation.NavHostController,
    session: UserSession,
    builder: NavGraphBuilder,
    onLogout: () -> Unit
) {
    builder.composable(NavRoutes.AlumnoHome) {
        AlumnoHomeScreen(
            navController = navController,
            onLogout = {
                handleLogout(navController, session)
                onLogout() // Actualizar el estado después del logout
            },
            onGestionarInscripciones = { 
                navController.navigate(NavRoutes.GestionarInscripciones) 
            },
            onVerBoleta = {
                navController.navigate(NavRoutes.VerBoleta)
            }
        )
    }
    
    builder.composable(NavRoutes.GestionarInscripciones) {
        // Obtener userId dinámicamente de la sesión
        val userId = session.getUserId()
        GestionarInscripciones(
            alumnoId = userId,
            semestreActual = 2,
            gestionActual = 2025,
            onBack = { navController.popBackStack() }
        )
    }
    
    builder.composable(NavRoutes.VerBoleta) {
        // Obtener userId dinámicamente de la sesión
        val userId = session.getUserId()
        VerBoletaScreen(
            alumnoId = userId,
            onBack = { navController.popBackStack() }
        )
    }
    
    builder.composable(NavRoutes.GestionarAsistencias) {
        GestionarAsistencia(
            onBack = { navController.popBackStack() }
        )
    }
}

// ============================================================================
// MOLECULES - Funciones de utilidad y lógica de navegación
// ============================================================================

/**
 * Determina la pantalla inicial basándose en la sesión del usuario.
 * 
 * @param userId ID del usuario (-1 si no hay sesión)
 * @param userRol Rol del usuario (Admin, Docente, Alumno)
 * @return Ruta de destino inicial
 */
private fun determineStartDestination(userId: Int, userRol: String?): String {
    return when {
        userId != -1 && userRol == "Admin" -> NavRoutes.AdminHome
        userId != -1 && userRol == "Docente" -> NavRoutes.DocenteHome
        userId != -1 && userRol == "Alumno" -> NavRoutes.AlumnoHome
        else -> NavRoutes.Login
    }
}

/**
 * Navega a la pantalla home correspondiente según el rol del usuario.
 * 
 * @param navController Controlador de navegación
 * @param rol Rol del usuario autenticado
 */
private fun navigateToRoleHome(
    navController: androidx.navigation.NavHostController,
    rol: String
) {
    when (rol) {
        "Admin" -> navController.navigate(NavRoutes.AdminHome)
        "Docente" -> navController.navigate(NavRoutes.DocenteHome)
        else -> navController.navigate(NavRoutes.AlumnoHome)
    }
}

/**
 * Maneja el proceso de logout del usuario.
 * 
 * Limpia la sesión y navega de vuelta a la pantalla de login.
 * Limpia todo el back stack para evitar que el usuario pueda volver atrás.
 * 
 * @param navController Controlador de navegación
 * @param session Sesión del usuario
 */
private fun handleLogout(
    navController: androidx.navigation.NavHostController,
    session: UserSession
) {
    // Limpiar la sesión primero
    session.clear()
    // Limpiar todo el back stack y navegar a Login
    // Usamos popUpTo con el startDestination para limpiar todo el stack
    navController.navigate(NavRoutes.Login) {
        // Limpiar todo el back stack hasta Login (inclusive)
        popUpTo(NavRoutes.Login) { inclusive = true }
        // Evitar múltiples instancias de Login en el stack
        launchSingleTop = true
        // Limpiar el back stack completamente
        restoreState = false
    }
    // Nota: El NavHost se recreará automáticamente debido al key basado en userId/userRol
    // que ahora será -1 y null, forzando la recreación con el startDestination correcto
}

// ============================================================================
// ATOMS - Constantes y definiciones de rutas
// ============================================================================

/**
 * Objeto que contiene todas las rutas de navegación de la aplicación.
 * 
 * Centraliza las definiciones de rutas para facilitar el mantenimiento
 * y evitar errores de escritura.
 */
private object NavRoutes {
    // Autenticación
    const val Login = "login"
    
    // Admin
    const val AdminHome = "adminHome"
    const val GestionUsuarios = "gestionUsuarios"
    const val GestionMaterias = "gestionMaterias"
    const val GestionGrupos = "gestionGrupos"
    const val GestionHorarios = "gestionHorarios"
    
    // Docente
    const val DocenteHome = "docenteHome"
    const val DocenteGrupos = "docenteGrupos"
    const val DocenteEstudiantes = "docenteEstudiantes"
    const val DocenteConfigurarHorarios = "docenteConfigurarHorarios"
    
    // Alumno (también usado por Admin para gestionar inscripciones)
    const val AlumnoHome = "alumnoHome"
    const val GestionarInscripciones = "gestionarInscripciones"
    const val VerBoleta = "verBoleta"
    const val GestionarAsistencias = "gestionarAsistencias"
}


