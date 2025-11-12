package com.bo.asistenciaapp.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShortText
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
import com.bo.asistenciaapp.data.repository.MateriaRepository
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.usecase.MateriaCU
import com.bo.asistenciaapp.domain.viewmodel.MateriaUiState
import com.bo.asistenciaapp.domain.viewmodel.VMMateria
import com.bo.asistenciaapp.presentation.common.UserLayout
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank

/**
 * Pantalla de gestión de materias para administrador.
 * 
 * Permite:
 * - Agregar nuevas materias
 * - Ver lista de materias existentes
 * - Eliminar materias
 * 
 * Arquitectura: Componentes organizados siguiendo principios de Atomic Design
 * - Atoms: Elementos básicos (Iconos, Textos, TextFields)
 * - Molecules: Componentes compuestos (Cards de materia, Formulario)
 * - Organisms: Secciones completas (Lista de materias, Formulario de creación)
 */
@Composable
fun GestionarMateriasScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val database = remember { AppDatabase.getInstance(context) }
    val materiaRepository = remember { MateriaRepository(database) }
    val materiaCU = remember { MateriaCU(materiaRepository) }
    
    // ViewModel
    val viewModel: VMMateria = viewModel {
        VMMateria(materiaCU)
    }
    
    val materias by viewModel.materias.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Manejar estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is MateriaUiState.Success -> {
                if (state.mensaje != null) {
                    snackbarHostState.showSnackbar(state.mensaje)
                }
            }
            is MateriaUiState.Error -> {
                snackbarHostState.showSnackbar(state.mensaje)
            }
            else -> {}
        }
    }

    var nombre by remember { mutableStateOf("") }
    var sigla by remember { mutableStateOf("") }
    var nivel by remember { mutableStateOf("") }

    UserLayout(
        title = "Gestión de Materias",
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
                MateriaFormSection(
                    nombre = nombre,
                    sigla = sigla,
                    nivel = nivel,
                    onNombreChange = { nombre = it },
                    onSiglaChange = { sigla = it },
                    onNivelChange = { nivel = it },
                    onSubmit = {
                        if (nombre.isNotBlank() && sigla.isNotBlank() && nivel.isNotBlank()) {
                            viewModel.agregarMateria(nombre, sigla, nivel.toInt())
                            nombre = ""
                            sigla = ""
                            nivel = ""
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Completa todos los campos") }
                        }
                    },
                    isLoading = uiState is MateriaUiState.Loading
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                MateriaListSection(
                    materias = materias,
                    onDelete = { viewModel.eliminarMateria(it) },
                    isLoading = uiState is MateriaUiState.Loading
                )
            }
        }
    }
}

// ============================================================================
// ORGANISMS - Componentes complejos que combinan múltiples moléculas
// ============================================================================

/**
 * Sección del formulario para agregar materias.
 * 
 * Organismo que combina campos de entrada y botón de acción.
 */
@Composable
private fun MateriaFormSection(
    nombre: String,
    sigla: String,
    nivel: String,
    onNombreChange: (String) -> Unit,
    onSiglaChange: (String) -> Unit,
    onNivelChange: (String) -> Unit,
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
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Nueva Materia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            MateriaNombreField(
                value = nombre,
                onValueChange = onNombreChange
            )

            MateriaSiglaField(
                value = sigla,
                onValueChange = onSiglaChange
            )

            MateriaNivelField(
                value = nivel,
                onValueChange = onNivelChange
            )

            MateriaSubmitButton(
                onClick = onSubmit,
                isLoading = isLoading
            )
        }
    }
}

/**
 * Sección de lista de materias.
 * 
 * Organismo que muestra todas las materias existentes.
 */
@Composable
private fun MateriaListSection(
    materias: List<Materia>,
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
                imageVector = Icons.Default.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Materias Registradas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (materias.isEmpty()) {
            MateriaEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(materias) { materia ->
                    MateriaCard(
                        materia = materia,
                        onDelete = { onDelete(materia.id) },
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
 * Campo de texto para el nombre de la materia.
 */
@Composable
private fun MateriaNombreField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Nombre") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Title,
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Campo de texto para la sigla de la materia.
 */
@Composable
private fun MateriaSiglaField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Sigla") },
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ShortText,
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Campo de texto para el nivel de la materia.
 */
@Composable
private fun MateriaNivelField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Nivel") },
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
 * Botón para enviar el formulario de materia.
 */
@Composable
private fun MateriaSubmitButton(
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
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar Materia")
        }
    }
}

/**
 * Card que representa una materia en la lista.
 */
@Composable
private fun MateriaCard(
    materia: Materia,
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
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = materia.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = materia.sigla,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Nivel ${materia.nivel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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

/**
 * Estado vacío cuando no hay materias.
 */
@Composable
private fun MateriaEmptyState() {
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
                imageVector = Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No hay materias registradas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Agrega una nueva materia usando el formulario superior",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}