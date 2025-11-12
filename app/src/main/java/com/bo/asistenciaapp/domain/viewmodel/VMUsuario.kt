package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la UI para gestión de usuarios.
 */
sealed class UsuarioUiState {
    object Idle : UsuarioUiState()
    object Loading : UsuarioUiState()
    data class Success(val mensaje: String? = null) : UsuarioUiState()
    data class Error(val mensaje: String) : UsuarioUiState()
}

/**
 * ViewModel para la gestión de usuarios.
 * 
 * Maneja la lógica de negocio relacionada con usuarios y el estado de la UI.
 * 
 * Responsabilidades:
 * - Obtener lista de usuarios
 * - Agregar nuevos usuarios
 * - Actualizar usuarios existentes
 * - Eliminar usuarios
 * - Gestionar estados de carga, éxito y error
 * 
 * Uso:
 * ```kotlin
 * val viewModel: VMUsuario = viewModel()
 * val usuarios by viewModel.usuarios.collectAsState()
 * ```
 */
class VMUsuario(
    private val usuarioCU: UsuarioCU
) : ViewModel() {
    
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()
    
    private val _docentes = MutableStateFlow<List<Usuario>>(emptyList())
    val docentes: StateFlow<List<Usuario>> = _docentes.asStateFlow()
    
    private val _uiState = MutableStateFlow<UsuarioUiState>(UsuarioUiState.Idle)
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    /**
     * Recarga la lista de usuarios desde la base de datos.
     */
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = UsuarioUiState.Loading
            try {
                _usuarios.value = usuarioCU.obtenerUsuarios()
                _docentes.value = usuarioCU.obtenerDocentes()
                _uiState.value = UsuarioUiState.Success()
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Error al cargar usuarios: ${e.message}")
            }
        }
    }

    /**
     * Agrega un nuevo usuario al sistema.
     * 
     * @param nombres Nombres del usuario
     * @param apellidos Apellidos del usuario
     * @param registro Número de registro
     * @param rol Rol del usuario (Admin, Docente, Alumno)
     * @param username Nombre de usuario único
     * @param contrasena Contraseña del usuario
     */
    fun agregarUsuario(
        nombres: String,
        apellidos: String,
        registro: String,
        rol: String,
        username: String,
        contrasena: String
    ) {
        viewModelScope.launch {
            _uiState.value = UsuarioUiState.Loading
            try {
                val result = usuarioCU.agregarUsuario(
                    nombres, apellidos, registro, rol, username, contrasena
                )
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = UsuarioUiState.Success("Usuario agregado exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = UsuarioUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Error al agregar usuario: ${e.message}")
            }
        }
    }
    
    /**
     * Actualiza los datos de un usuario existente.
     * 
     * @param id ID del usuario a actualizar
     * @param nombres Nuevos nombres
     * @param apellidos Nuevos apellidos
     * @param rol Nuevo rol
     */
    fun actualizarUsuario(id: Int, nombres: String, apellidos: String, rol: String) {
        viewModelScope.launch {
            _uiState.value = UsuarioUiState.Loading
            try {
                val result = usuarioCU.actualizarUsuario(id, nombres, apellidos, rol)
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = UsuarioUiState.Success("Usuario actualizado exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = UsuarioUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Error al actualizar usuario: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina un usuario del sistema.
     * 
     * @param id ID del usuario a eliminar
     */
    fun eliminarUsuario(id: Int) {
        viewModelScope.launch {
            _uiState.value = UsuarioUiState.Loading
            try {
                val result = usuarioCU.eliminarUsuario(id)
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = UsuarioUiState.Success("Usuario eliminado exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = UsuarioUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error("Error al eliminar usuario: ${e.message}")
            }
        }
    }
    
    /**
     * Limpia el estado de error, volviendo al estado Idle.
     */
    fun clearError() {
        if (_uiState.value is UsuarioUiState.Error) {
            _uiState.value = UsuarioUiState.Idle
        }
    }
}