package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.usecase.AsistenciaCU
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la UI para gestión de asistencias.
 */
sealed class AsistenciaUiState {
    object Idle : AsistenciaUiState()
    object Loading : AsistenciaUiState()
    data class Success(val mensaje: String? = null) : AsistenciaUiState()
    data class Error(val mensaje: String) : AsistenciaUiState()
}

/**
 * ViewModel para la gestión de asistencias.
 * 
 * Maneja la lógica de negocio relacionada con asistencias y el estado de la UI.
 */
class VMAsistencia(
    private val asistenciaCU: AsistenciaCU,
    private val inscripcionCU: InscripcionCU,
    private val grupoCU: GrupoCU,
    private val alumnoId: Int
) : ViewModel() {
    
    private val _asistencias = MutableStateFlow<List<Asistencia>>(emptyList())
    val asistencias: StateFlow<List<Asistencia>> = _asistencias.asStateFlow()
    
    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> = _grupos.asStateFlow()
    
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Idle)
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    /**
     * Recarga las listas desde la base de datos.
     */
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = AsistenciaUiState.Loading
            try {
                _asistencias.value = asistenciaCU.obtenerAsistencias(alumnoId)
                // Obtener grupos en los que el alumno está inscrito
                val boletas = inscripcionCU.obtenerInscripciones(alumnoId)
                val todosGrupos = grupoCU.obtenerGrupos()
                _grupos.value = todosGrupos.filter { grupo ->
                    boletas.any { it.grupoId == grupo.id }
                }
                _uiState.value = AsistenciaUiState.Success()
            } catch (e: Exception) {
                _uiState.value = AsistenciaUiState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    /**
     * Verifica si el alumno puede marcar asistencia en un grupo.
     */
    fun puedeMarcarAsistencia(grupoId: Int): Boolean {
        return asistenciaCU.puedeMarcarAsistencia(alumnoId, grupoId)
    }

    /**
     * Registra una nueva asistencia.
     */
    fun marcarAsistencia(grupoId: Int, fecha: String) {
        viewModelScope.launch {
            _uiState.value = AsistenciaUiState.Loading
            try {
                val result = asistenciaCU.marcarAsistencia(alumnoId, grupoId, fecha)
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = AsistenciaUiState.Success("Asistencia registrada exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = AsistenciaUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AsistenciaUiState.Error("Error al registrar asistencia: ${e.message}")
            }
        }
    }
    
    /**
     * Limpia el estado de error.
     */
    fun clearError() {
        if (_uiState.value is AsistenciaUiState.Error) {
            _uiState.value = AsistenciaUiState.Idle
        }
    }
}
