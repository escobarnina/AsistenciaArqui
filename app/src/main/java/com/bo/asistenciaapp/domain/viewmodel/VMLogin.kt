package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.data.local.UserSession
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la UI para el login.
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val usuario: Usuario) : LoginUiState()
    data class Error(val mensaje: String) : LoginUiState()
}

/**
 * ViewModel para la pantalla de Login.
 * 
 * Maneja la lógica de autenticación y el estado de la UI.
 * 
 * Responsabilidades:
 * - Validar credenciales del usuario
 * - Gestionar estados de carga, éxito y error
 * - Guardar sesión del usuario autenticado
 * 
 * Uso:
 * ```kotlin
 * val viewModel: VMLogin = viewModel()
 * val uiState by viewModel.uiState.collectAsState()
 * ```
 */
class VMLogin(
    private val usuarioCU: UsuarioCU,
    private val userSession: UserSession
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas.
     * 
     * @param username Nombre de usuario
     * @param contrasena Contraseña del usuario
     */
    fun login(username: String, contrasena: String) {
        // Validar que los campos no estén vacíos
        if (username.isBlank() || contrasena.isBlank()) {
            _uiState.value = LoginUiState.Error("Por favor complete todos los campos")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            try {
                val usuario = usuarioCU.validarUsuario(username, contrasena)
                
                if (usuario != null) {
                    // Guardar sesión
                    userSession.saveUser(
                        id = usuario.id,
                        nombre = "${usuario.nombres} ${usuario.apellidos}",
                        rol = usuario.rol
                    )
                    _uiState.value = LoginUiState.Success(usuario)
                } else {
                    _uiState.value = LoginUiState.Error("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Error al iniciar sesión: ${e.message}")
            }
        }
    }
    
    /**
     * Limpia el estado de error, volviendo al estado Idle.
     */
    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }
    
    /**
     * Resetea el estado del ViewModel.
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

