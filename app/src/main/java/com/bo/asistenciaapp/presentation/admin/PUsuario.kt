package com.bo.asistenciaapp.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.repository.UsuarioRepository
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import com.bo.asistenciaapp.domain.viewmodel.UsuarioUiState
import com.bo.asistenciaapp.domain.viewmodel.VMUsuario
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank

/**
 * Pantalla de gestión de usuarios para administrador.
 * 
 * Permite:
 * - Agregar nuevos usuarios
 * - Ver lista de usuarios existentes
 * - Editar usuarios existentes
 * - Eliminar usuarios
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos, TextFields)
 * - Molecules: Componentes compuestos (Cards de usuario, Formulario, Diálogo de edición)
 * - Organisms: Secciones completas (Lista de usuarios, Formulario de creación)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarUsuariosScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val usuarioRepository = remember { UsuarioRepository(database) }
    val usuarioCU = remember { UsuarioCU(usuarioRepository) }
    
    // ViewModel
    val viewModel: VMUsuario = viewModel {
        VMUsuario(usuarioCU)
    }
    
    val usuarios by viewModel.usuarios.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UsuarioUiState.Success -> {
                if (state.mensaje != null) {
                    snackbarHostState.showSnackbar(state.mensaje)
                }
            }
            is UsuarioUiState.Error -> {
                snackbarHostState.showSnackbar(state.mensaje)
            }
            else -> {}
        }
    }

    // Estados para el nuevo usuario
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }
    var registro by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var expandedRol by remember { mutableStateOf(false) }

    // Edición
    var editUsuario by remember { mutableStateOf<Usuario?>(null) }
    var editNombres by remember { mutableStateOf("") }
    var editApellidos by remember { mutableStateOf("") }
    var editRol by remember { mutableStateOf("") }
    var expandedEditRol by remember { mutableStateOf(false) }

    val roles = listOf("Alumno", "Docente", "Admin")

    UserLayout(
        title = "Gestión de Usuarios",
        showBackButton = true,
        onBack = onBack
    ) { paddingValues ->
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UsuarioFormSection(
                    nombres = nombres,
                    apellidos = apellidos,
                    rol = rol,
                    registro = registro,
                    username = username,
                    contrasena = contrasena,
                    expandedRol = expandedRol,
                    roles = roles,
                    onNombresChange = { nombres = it },
                    onApellidosChange = { apellidos = it },
                    onRolChange = { rol = it },
                    onRegistroChange = { registro = it },
                    onUsernameChange = { username = it },
                    onContrasenaChange = { contrasena = it },
                    onExpandedRolChange = { expandedRol = it },
                    onSubmit = {
                        if (nombres.isNotBlank() && apellidos.isNotBlank() && rol.isNotBlank() &&
                            registro.isNotBlank() && username.isNotBlank() && contrasena.isNotBlank()) {
                            viewModel.agregarUsuario(nombres, apellidos, registro, rol, username, contrasena)
                            nombres = ""
                            apellidos = ""
                            rol = ""
                            registro = ""
                            username = ""
                            contrasena = ""
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Completa todos los campos") }
                        }
                    },
                    isLoading = uiState is UsuarioUiState.Loading
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                UsuarioListSection(
                    usuarios = usuarios,
                    onEdit = { usuario ->
                        editUsuario = usuario
                        editNombres = usuario.nombres
                        editApellidos = usuario.apellidos
                        editRol = usuario.rol
                    },
                    onDelete = { viewModel.eliminarUsuario(it) },
                    isLoading = uiState is UsuarioUiState.Loading
                )
            }
        }
    }

    // Diálogo de edición
    if (editUsuario != null) {
        UsuarioEditDialog(
            nombres = editNombres,
            apellidos = editApellidos,
            rol = editRol,
            expandedRol = expandedEditRol,
            roles = roles,
            onNombresChange = { editNombres = it },
            onApellidosChange = { editApellidos = it },
            onRolChange = { editRol = it },
            onExpandedRolChange = { expandedEditRol = it },
            onConfirm = {
                viewModel.actualizarUsuario(editUsuario!!.id, editNombres, editApellidos, editRol)
                editUsuario = null
            },
            onDismiss = { editUsuario = null },
            isLoading = uiState is UsuarioUiState.Loading
        )
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Sección del formulario para agregar usuarios.
 * 
 * Organismo que combina campos de entrada y botón de acción.
 */
@Composable
private fun UsuarioFormSection(
    nombres: String,
    apellidos: String,
    rol: String,
    registro: String,
    username: String,
    contrasena: String,
    expandedRol: Boolean,
    roles: List<String>,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onRolChange: (String) -> Unit,
    onRegistroChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onExpandedRolChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Nuevo Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            UsuarioNombresField(
                value = nombres,
                onValueChange = onNombresChange
            )

            UsuarioApellidosField(
                value = apellidos,
                onValueChange = onApellidosChange
            )

            UsuarioRolDropdown(
                value = rol,
                expanded = expandedRol,
                roles = roles,
                onValueChange = onRolChange,
                onExpandedChange = onExpandedRolChange
            )

            UsuarioRegistroField(
                value = registro,
                onValueChange = onRegistroChange
            )

            UsuarioUsernameField(
                value = username,
                onValueChange = onUsernameChange
            )

            UsuarioContrasenaField(
                value = contrasena,
                onValueChange = onContrasenaChange
            )

            UsuarioSubmitButton(
                onClick = onSubmit,
                isLoading = isLoading
            )
        }
    }
}

/**
 * Sección de lista de usuarios.
 * 
 * Organismo que muestra todos los usuarios existentes.
 */
@Composable
private fun UsuarioListSection(
    usuarios: List<Usuario>,
    onEdit: (Usuario) -> Unit,
    onDelete: (Int) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Usuarios Registrados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (usuarios.isEmpty()) {
            UsuarioEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(usuarios) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        onEdit = { onEdit(usuario) },
                        onDelete = { onDelete(usuario.id) },
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}

// ============================================================================
// MOLECULES - Componentes compuestos que combinan átomos
// ============================================================================

/**
 * Campo de texto para nombres del usuario.
 */
@Composable
private fun UsuarioNombresField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Nombres") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Campo de texto para apellidos del usuario.
 */
@Composable
private fun UsuarioApellidosField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Apellidos") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Dropdown para seleccionar el rol del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsuarioRolDropdown(
    value: String,
    expanded: Boolean,
    roles: List<String>,
    onValueChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Rol") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            roles.forEach { rol ->
                DropdownMenuItem(
                    text = { Text(rol) },
                    onClick = {
                        onValueChange(rol)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

/**
 * Campo de texto para registro del usuario.
 */
@Composable
private fun UsuarioRegistroField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Registro") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Numbers,
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Campo de texto para username del usuario.
 */
@Composable
private fun UsuarioUsernameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Username") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Campo de texto para contraseña del usuario.
 */
@Composable
private fun UsuarioContrasenaField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Contraseña") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        visualTransformation = if (passwordVisible) {
            androidx.compose.ui.text.input.VisualTransformation.None
        } else {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Botón para enviar el formulario de usuario.
 */
@Composable
private fun UsuarioSubmitButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar Usuario")
        }
    }
}

/**
 * Card que representa un usuario en la lista.
 */
@Composable
private fun UsuarioCard(
    usuario: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = when (usuario.rol.lowercase()) {
                        "admin" -> MaterialTheme.colorScheme.errorContainer
                        "docente" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = when (usuario.rol.lowercase()) {
                                "admin" -> Icons.Default.AdminPanelSettings
                                "docente" -> Icons.Default.School
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = when (usuario.rol.lowercase()) {
                                "admin" -> MaterialTheme.colorScheme.onErrorContainer
                                "docente" -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${usuario.nombres} ${usuario.apellidos}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = usuario.registro,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = usuario.username,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = usuario.rol,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para editar un usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsuarioEditDialog(
    nombres: String,
    apellidos: String,
    rol: String,
    expandedRol: Boolean,
    roles: List<String>,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onRolChange: (String) -> Unit,
    onExpandedRolChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Editar Usuario",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UsuarioNombresField(
                    value = nombres,
                    onValueChange = onNombresChange
                )
                UsuarioApellidosField(
                    value = apellidos,
                    onValueChange = onApellidosChange
                )
                UsuarioRolDropdown(
                    value = rol,
                    expanded = expandedRol,
                    roles = roles,
                    onValueChange = onRolChange,
                    onExpandedChange = onExpandedRolChange
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading && nombres.isNotBlank() && apellidos.isNotBlank() && rol.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Estado vacío cuando no hay usuarios.
 */
@Composable
private fun UsuarioEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No hay usuarios registrados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Agrega un nuevo usuario usando el formulario superior",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
