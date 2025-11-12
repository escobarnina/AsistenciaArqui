package com.bo.asistenciaapp.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bo.asistenciaapp.R
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.data.repository.UsuarioRepository
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import com.bo.asistenciaapp.domain.viewmodel.LoginUiState
import com.bo.asistenciaapp.domain.viewmodel.VMLogin

/**
 * Pantalla de inicio de sesión de la aplicación AsistenciaApp.
 * 
 * Permite a los usuarios autenticarse ingresando su usuario y contraseña.
 * Utiliza VMLogin para manejar la lógica de autenticación y estados de la UI.
 * 
 * @param onLoginSuccess Callback que se ejecuta cuando el login es exitoso,
 *                       recibe el Usuario autenticado como parámetro
 */
@Composable
fun LoginScreen(onLoginSuccess: (com.bo.asistenciaapp.domain.model.Usuario) -> Unit) {
    val context = LocalContext.current
    
    // Inicializar dependencias (en producción usar inyección de dependencias)
    val database = remember { AppDatabase.getInstance(context) }
    val usuarioRepository = remember { UsuarioRepository(database) }
    val usuarioCU = remember { UsuarioCU(usuarioRepository) }
    val userSession = remember { UserSession(context) }
    
    // ViewModel para manejar la lógica de login
    val viewModel: VMLogin = viewModel {
        VMLogin(usuarioCU, userSession)
    }
    
    // Estado de la UI
    val uiState by viewModel.uiState.collectAsState()
    
    // Estado local de los campos de texto
    var username by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Manejar estados de éxito y error
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> {
                onLoginSuccess(state.usuario)
            }
            is LoginUiState.Error -> {
                // El error se muestra en la UI automáticamente
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Campo de usuario
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                // Limpiar error cuando el usuario empiece a escribir
                if (uiState is LoginUiState.Error) {
                    viewModel.clearError()
                }
            },
            label = { Text(stringResource(R.string.login_username)) },
            placeholder = { Text(stringResource(R.string.login_username_hint)) },
            singleLine = true,
            enabled = uiState !is LoginUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Campo de contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { 
                contrasena = it
                // Limpiar error cuando el usuario empiece a escribir
                if (uiState is LoginUiState.Error) {
                    viewModel.clearError()
                }
            },
            label = { Text(stringResource(R.string.login_password)) },
            placeholder = { Text(stringResource(R.string.login_password_hint)) },
            singleLine = true,
            enabled = uiState !is LoginUiState.Loading,
            visualTransformation = if (passwordVisible) VisualTransformation.None 
                                  else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility 
                                     else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" 
                                            else "Mostrar contraseña"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botón de login
        Button(
            onClick = { viewModel.login(username, contrasena) },
            enabled = uiState !is LoginUiState.Loading && 
                     username.isNotBlank() && 
                     contrasena.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciando sesión...")
            } else {
                Text(
                    text = stringResource(R.string.login_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        
        // Mostrar mensaje de error
        if (uiState is LoginUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (uiState as LoginUiState.Error).mensaje,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}


