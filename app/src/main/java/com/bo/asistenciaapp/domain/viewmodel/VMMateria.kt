package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.usecase.MateriaCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la UI para gestión de materias.
 */
sealed class MateriaUiState {
    object Idle : MateriaUiState()
    object Loading : MateriaUiState()
    data class Success(val mensaje: String? = null) : MateriaUiState()
    data class Error(val mensaje: String) : MateriaUiState()
}

/**
 * ViewModel para la gestión de materias.
 * 
 * Maneja la lógica de negocio relacionada con materias y el estado de la UI.
 */
class VMMateria(
    private val materiaCU: MateriaCU
) : ViewModel() {
    
    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias.asStateFlow()
    
    private val _uiState = MutableStateFlow<MateriaUiState>(MateriaUiState.Idle)
    val uiState: StateFlow<MateriaUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    /**
     * Recarga la lista de materias desde la base de datos.
     */
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = MateriaUiState.Loading
            try {
                _materias.value = materiaCU.obtenerMaterias()
                _uiState.value = MateriaUiState.Success()
            } catch (e: Exception) {
                _uiState.value = MateriaUiState.Error("Error al cargar materias: ${e.message}")
            }
        }
    }

    /**
     * Agrega una nueva materia al sistema.
     */
    fun agregarMateria(nombre: String, sigla: String, nivel: Int) {
        viewModelScope.launch {
            _uiState.value = MateriaUiState.Loading
            try {
                val result = materiaCU.agregarMateria(nombre, sigla, nivel)
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = MateriaUiState.Success("Materia agregada exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = MateriaUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MateriaUiState.Error("Error al agregar materia: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina una materia del sistema.
     */
    fun eliminarMateria(id: Int) {
        viewModelScope.launch {
            _uiState.value = MateriaUiState.Loading
            try {
                val result = materiaCU.eliminarMateria(id)
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = MateriaUiState.Success("Materia eliminada exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = MateriaUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MateriaUiState.Error("Error al eliminar materia: ${e.message}")
            }
        }
    }
    
    /**
     * Limpia el estado de error.
     */
    fun clearError() {
        if (_uiState.value is MateriaUiState.Error) {
            _uiState.value = MateriaUiState.Idle
        }
    }
}
