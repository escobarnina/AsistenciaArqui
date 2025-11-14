package com.bo.asistenciaapp.presentation.login

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos)
 * - Molecules: Componentes compuestos (Campos de texto, Botones)
 * - Organisms: Secciones completas (Formulario, Card principal)
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

    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoginFormCard(
                username = username,
                password = contrasena,
                passwordVisible = passwordVisible,
                uiState = uiState,
                onUsernameChange = { 
                    username = it
                    if (uiState is LoginUiState.Error) {
                        viewModel.clearError()
                    }
                },
                onPasswordChange = { 
                    contrasena = it
                    if (uiState is LoginUiState.Error) {
                        viewModel.clearError()
                    }
                },
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                onLoginClick = { viewModel.login(username, contrasena) },
                onErrorDismiss = { viewModel.clearError() }
            )
        }
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Card principal del formulario de login.
 * 
 * Organismo que contiene todos los elementos del formulario de autenticación.
 */
@Composable
private fun LoginFormCard(
    username: String,
    password: String,
    passwordVisible: Boolean,
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LoginLogo()
            LoginHeader()
            Spacer(modifier = Modifier.height(8.dp))
            LoginUsernameField(
                username = username,
                enabled = uiState !is LoginUiState.Loading,
                onValueChange = onUsernameChange
            )
            LoginPasswordField(
                password = password,
                passwordVisible = passwordVisible,
                enabled = uiState !is LoginUiState.Loading,
                onValueChange = onPasswordChange,
                onVisibilityToggle = onPasswordVisibilityToggle
            )
            LoginButton(
                uiState = uiState,
                username = username,
                password = password,
                onClick = onLoginClick
            )
            LoginErrorCard(
                uiState = uiState,
                onDismiss = onErrorDismiss
            )
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Logo/Icono de la aplicación.
 * 
 * Molécula que muestra el icono de la app en un contenedor estilizado.
 */
@Composable
private fun LoginLogo() {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Encabezado con título y subtítulo.
 * 
 * Molécula que muestra el título principal y descripción de la aplicación.
 */
@Composable
private fun LoginHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Sistema de Gestión Académica",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Campo de texto para el nombre de usuario.
 * 
 * Molécula que combina un OutlinedTextField con icono de Person.
 */
@Composable
private fun LoginUsernameField(
    username: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = username,
        onValueChange = onValueChange,
        label = { 
            Text(
                stringResource(R.string.login_username),
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        placeholder = { 
            Text(
                stringResource(R.string.login_username_hint),
                style = MaterialTheme.typography.bodySmall
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * Campo de texto para la contraseña.
 * 
 * Molécula que combina un OutlinedTextField con iconos de Lock y Visibility.
 */
@Composable
private fun LoginPasswordField(
    password: String,
    passwordVisible: Boolean,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onValueChange,
        label = { 
            Text(
                stringResource(R.string.login_password),
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        placeholder = { 
            Text(
                stringResource(R.string.login_password_hint),
                style = MaterialTheme.typography.bodySmall
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onVisibilityToggle,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility 
                                 else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" 
                                        else "Mostrar contraseña",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (passwordVisible) VisualTransformation.None 
                              else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * Botón de inicio de sesión.
 * 
 * Molécula que combina un Button con estados de carga y habilitado/deshabilitado.
 */
@Composable
private fun LoginButton(
    uiState: LoginUiState,
    username: String,
    password: String,
    onClick: () -> Unit
) {
    val isEnabled = uiState !is LoginUiState.Loading && 
                   username.isNotBlank() && 
                   password.isNotBlank()
    val buttonAlpha by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "buttonAlpha"
    )
    
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(buttonAlpha),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (uiState is LoginUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Iniciando sesión...",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.login_button),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Card de mensaje de error.
 * 
 * Molécula que muestra un mensaje de error con iconos y botón de cierre.
 */
@Composable
private fun LoginErrorCard(
    uiState: LoginUiState,
    onDismiss: () -> Unit
) {
    val errorAlpha by animateFloatAsState(
        targetValue = if (uiState is LoginUiState.Error) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "errorAlpha"
    )
    
    if (uiState is LoginUiState.Error) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(errorAlpha),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = uiState.mensaje,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


