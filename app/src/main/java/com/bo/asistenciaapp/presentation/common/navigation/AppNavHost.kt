package com.bo.asistenciaapp.presentation.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.presentation.admin.AdminHomeScreen
import com.bo.asistenciaapp.presentation.admin.GestionarGruposScreen
import com.bo.asistenciaapp.presentation.admin.GestionarHorarios
import com.bo.asistenciaapp.presentation.admin.GestionarMateriasScreen
import com.bo.asistenciaapp.presentation.admin.GestionarUsuariosScreen
import com.bo.asistenciaapp.presentation.alumno.AlumnoHomeScreen
import com.bo.asistenciaapp.presentation.alumno.GestionarAsistencia
import com.bo.asistenciaapp.presentation.alumno.GestionarInscripciones
import com.bo.asistenciaapp.presentation.docente.DocenteHomeScreen
import com.bo.asistenciaapp.presentation.docente.MarcarAsistenciaDocenteScreen
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

    // Obtener información de la sesión del usuario
    val userId = session.getUserId()
    val userRol = session.getUserRol()

    // Determinar la pantalla inicial según la sesión
    val startDestination = determineStartDestination(userId, userRol)

    NavHost(navController = navController, startDestination = startDestination) {
        // Rutas de autenticación
        LoginRoutes(
            navController = navController,
            builder = this
        )
        
        // Rutas de administrador
        AdminRoutes(
            navController = navController,
            session = session,
            userId = userId,
            builder = this
        )
        
        // Rutas de docente
        DocenteRoutes(
            navController = navController,
            session = session,
            userId = userId,
            builder = this
        )
        
        // Rutas de alumno
        AlumnoRoutes(
            navController = navController,
            userId = userId,
            builder = this
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
    builder: NavGraphBuilder
) {
    builder.composable(NavRoutes.Login) {
        LoginScreen(
            onLoginSuccess = { usuario ->
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
    userId: Int,
    builder: NavGraphBuilder
) {
    builder.composable(NavRoutes.AdminHome) {
        AdminHomeScreen(
            onLogout = {
                handleLogout(navController, session)
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
    userId: Int,
    builder: NavGraphBuilder
) {
    builder.composable(NavRoutes.DocenteHome) {
        DocenteHomeScreen(
            onLogout = {
                handleLogout(navController, session)
            },
            onVerGrupos = { navController.navigate(NavRoutes.DocenteGrupos) },
            onMarcarAsistencias = { navController.navigate(NavRoutes.DocenteMarcarAsistencias) }
        )
    }
    
    builder.composable(NavRoutes.DocenteGrupos) {
        VerGruposDocenteScreen(
            onBack = { navController.popBackStack() },
            onVerEstudiantes = { grupoId ->
                navController.navigate("${NavRoutes.DocenteEstudiantes}/$grupoId")
            }
        )
    }
    
    builder.composable("${NavRoutes.DocenteEstudiantes}/{grupoId}") { backStackEntry ->
        val grupoId = backStackEntry.arguments?.getString("grupoId")?.toIntOrNull() ?: -1
        VerEstudiantesGrupoScreen(
            grupoId = grupoId,
            grupoNombre = "",
            onBack = { navController.popBackStack() },
            onMarcarAsistencia = { estudianteId, grupoId ->
                navController.navigate(NavRoutes.DocenteMarcarAsistencias)
            }
        )
    }
    
    builder.composable(NavRoutes.DocenteMarcarAsistencias) {
        MarcarAsistenciaDocenteScreen(
            docenteId = userId,
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
    userId: Int,
    builder: NavGraphBuilder
) {
    builder.composable(NavRoutes.AlumnoHome) {
        AlumnoHomeScreen(
            navController = navController,
            onLogout = {
                navController.popBackStack(NavRoutes.Login, inclusive = false)
            },
            onGestionarInscripciones = { 
                navController.navigate(NavRoutes.GestionarInscripciones) 
            }
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
 * 
 * @param navController Controlador de navegación
 * @param session Sesión del usuario
 */
private fun handleLogout(
    navController: androidx.navigation.NavHostController,
    session: UserSession
) {
    session.clear()
    navController.popBackStack(NavRoutes.Login, inclusive = false)
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
    const val GestionarInscripciones = "gestionarInscripciones"
    
    // Docente
    const val DocenteHome = "docenteHome"
    const val DocenteGrupos = "docenteGrupos"
    const val DocenteEstudiantes = "docenteEstudiantes"
    const val DocenteMarcarAsistencias = "docenteMarcarAsistencias"
    
    // Alumno
    const val AlumnoHome = "alumnoHome"
    const val GestionarAsistencias = "gestionarAsistencias"
}


