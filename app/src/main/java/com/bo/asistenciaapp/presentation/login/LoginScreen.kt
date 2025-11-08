package com.bo.asistenciaapp.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.domain.model.Usuario

@Composable
fun LoginScreen(onLoginSuccess: (Usuario) -> Unit) {
    val context = LocalContext.current
    val session = remember { UserSession(context) }
    val db = remember { AppDatabase(context) }

    var username by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = {username = it},
            label = {Text("Usuario")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = contrasena,
            onValueChange = {contrasena = it},
            label = {Text("Password")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val user = db.validarUsuario(username, contrasena)
                if (user != null) {
                    session.saveUser(user.id,user.nombres, user.rol)
                    onLoginSuccess(user)
                } else {
                    resultado = user ?: "Credenciales inválidas"
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        resultado?.let {
            Spacer(Modifier.height(16.dp))
            Text(text = if (it == "Credenciales inválidas")
                it else "Bienvenido, tu rol es $it")
        }
    }
}


