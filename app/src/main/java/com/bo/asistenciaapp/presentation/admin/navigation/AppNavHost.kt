package com.bo.asistenciaapp.presentation.admin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import com.bo.asistenciaapp.presentation.login.LoginScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var session = remember { UserSession(context) }

    // Obtener el usuario desde el almacenamiento
    var userId = session.getUserId()
    val userRol = session.getUserRol()
    val userName = session.getUserName()

    // Redirigiar a la pantalla correspondiente
    val startDestination = when {
        userId != -1 && userRol == "Admin" -> "adminHome"
        userId != -1 && userRol == "Docente" -> "docenteHome"
        userId != -1 && userRol == "Alumno" -> "alumnoHome"
        else -> "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { usuario ->
                    when (usuario.rol) {
                        "Admin" -> navController.navigate("adminHome")
                        "Docente" -> navController.navigate("docenteHome")
                        else -> {
                            navController.navigate("alumnoHome")
                        }
                    }
                }
            )
        }
        composable("adminHome") {
            AdminHomeScreen(
                onLogout = {
                    userId = -1
                    session.clear()
                    navController.popBackStack("login", inclusive = false)
                           },
                onGestionarUsuarios = { navController.navigate("gestionUsuarios") },
                onGestionarMaterias = {  navController.navigate("gestionMaterias")},
                onGestionarGrupos = {  navController.navigate("gestionGrupos")},
                onGestionarHorarios = { navController.navigate("gestionHorarios")},
                onGestionarInscripciones = { navController.navigate("gestionarInscripciones") }

            )
        }
        composable("gestionUsuarios") {
            GestionarUsuariosScreen(
                onBack = {navController.popBackStack()}
            )
        }
        composable("alumnoHome") {
            AlumnoHomeScreen(
                navController,
                onLogout = {navController.popBackStack("login", inclusive = false)},
                onGestionarInscripciones = { navController.navigate("gestionarInscripciones") },
            )
        }
        composable("gestionMaterias") {
            GestionarMateriasScreen(onBack = {navController.popBackStack()})
        }
        composable("gestionGrupos") {
            GestionarGruposScreen(onBack = {navController.popBackStack()})
        }
        composable("gestionHorarios") {
            GestionarHorarios(onBack = {navController.popBackStack()})
        }
        composable("gestionarInscripciones") {
            GestionarInscripciones(userId,2, 2025,onBack = {navController.popBackStack()})
        }
        composable("gestionarAsistencias") {
            GestionarAsistencia(onBack = {navController.popBackStack()})
        }
    }
}