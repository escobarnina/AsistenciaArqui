package com.bo.asistenciaapp.presentation.admin

import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.repository.UsuarioRepository
import androidx.compose.runtime.*
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import com.bo.asistenciaapp.domain.viewmodel.UsuarioUiState
import com.bo.asistenciaapp.domain.viewmodel.VMUsuario
import kotlinx.coroutines.launch

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
                    scope.launch { snackbarHostState.showSnackbar(state.mensaje) }
                }
            }
            is UsuarioUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.mensaje) }
            }
            else -> {}
        }
    }

    // Roles
    var roles = listOf<String>("Alumno", "Docente", "Admin")
    var expanded by remember { mutableStateOf(false) }
    var expandedEdit by remember { mutableStateOf(false) }

    // Estados para el nuevo usuario
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }
    var registro by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Edicion
    var editUsuario by remember { mutableStateOf<Usuario?>(null) }
    var editNombres by remember { mutableStateOf("") }
    var editApellidos by remember { mutableStateOf("") }
    var editRol by remember { mutableStateOf("") }
//    var editUsername by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Gestión de usuarios", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            // Formulario para usuarios
            OutlinedTextField(
                value = nombres, onValueChange = { nombres = it },
                label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = apellidos, onValueChange = { apellidos = it },
                label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth()
            )
//            OutlinedTextField(
//                value = rol, onValueChange = { rol = it },
//                label = { Text("Rol (alumno/docente/admin)") }, modifier = Modifier.fillMaxWidth()
//            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = rol,
                    onValueChange = {},
                    label = { Text("Rol") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roles.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                rol = opcion
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = registro, onValueChange = { registro = it },
                label = { Text("Registro") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contrasena, onValueChange = { contrasena = it },
                label = { Text("Contrasena") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (nombres.isNotBlank() && apellidos.isNotBlank() && rol.isNotBlank() && registro.isNotBlank() && username.isNotBlank() && contrasena.isNotBlank()) {
                        viewModel.agregarUsuario(nombres, apellidos, registro, rol, username, contrasena)
                        nombres = ""; apellidos = ""; rol = ""; registro = ""; username = ""; contrasena = ""
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Completa todos los campos") }
                    }
                },
                enabled = uiState !is UsuarioUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState is UsuarioUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Agregar usuario")
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Lista de usuarios
            LazyColumn {
                items(usuarios) { u: Usuario ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                editUsuario = u
                                editNombres = u.nombres
                                editApellidos = u.apellidos
                                editRol = u.rol
                            },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${u.nombres} ${u.apellidos} (${u.registro})")
                        Spacer(Modifier.height(8.dp))
                        Text("${u.username} - ${u.rol}")
                        IconButton(
                            onClick = { viewModel.eliminarUsuario(u.id) },
                            enabled = uiState !is UsuarioUiState.Loading
                        ) {
                            Icon(Icons.Default.Delete, "Eliminar")
                        }
                    }
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // Editar usuario
    if (editUsuario != null) {
        BasicAlertDialog(onDismissRequest = { editUsuario = null }) {
            androidx.compose.material3.Card {
                Text(
                    text = "Editar usuario",
                    modifier = Modifier.padding(16.dp)
                )

                // Campos del formulario
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = editNombres, onValueChange = { editNombres = it },
                        label = { Text("Nombres") }
                    )
                    OutlinedTextField(
                        value = editApellidos, onValueChange = { editApellidos = it },
                        label = { Text("Apellidos") }
                    )
//                    OutlinedTextField(
//                        value = editRol, onValueChange = {editRol = it},
//                        label = {Text("Rol")}
//                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedEdit,
                        onExpandedChange = { expandedEdit = !expandedEdit }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = editRol,
                            onValueChange = {},
                            label = { Text("Rol") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEdit)
                            }
                        )
                    }
                }

                // Botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            viewModel.actualizarUsuario(editUsuario!!.id, editNombres, editApellidos, editRol)
                            editUsuario = null
                        },
                        enabled = uiState !is UsuarioUiState.Loading
                    ) {
                        Text("Confirmar")
                    }
                    TextButton(
                        onClick = {
                            editUsuario = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}