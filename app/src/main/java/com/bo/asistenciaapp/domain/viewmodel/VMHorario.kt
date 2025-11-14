package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Horario
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.HorarioCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la UI para gestión de horarios.
 */
sealed class HorarioUiState {
    object Idle : HorarioUiState()
    object Loading : HorarioUiState()
    data class Success(val mensaje: String? = null) : HorarioUiState()
    data class Error(val mensaje: String) : HorarioUiState()
}

/**
 * ViewModel para la gestión de horarios.
 * 
 * Maneja la lógica de negocio relacionada con horarios y el estado de la UI.
 */
class VMHorario(
    private val horarioCU: HorarioCU,
    private val grupoCU: GrupoCU
) : ViewModel() {
    
    private val _horarios = MutableStateFlow<List<Horario>>(emptyList())
    val horarios: StateFlow<List<Horario>> = _horarios.asStateFlow()
    
    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> = _grupos.asStateFlow()
    
    private val _uiState = MutableStateFlow<HorarioUiState>(HorarioUiState.Idle)
    val uiState: StateFlow<HorarioUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    /**
     * Recarga las listas desde la base de datos.
     */
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = HorarioUiState.Loading
            try {
                _horarios.value = horarioCU.obtenerHorarios()
                _grupos.value = grupoCU.obtenerGrupos()
                _uiState.value = HorarioUiState.Success()
            } catch (e: Exception) {
                _uiState.value = HorarioUiState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    /**
     * Agrega un nuevo horario al sistema.
     */
    fun agregarHorario(grupoId: Int, dia: String, horaInicio: String, horaFin: String) {
        viewModelScope.launch {
            _uiState.value = HorarioUiState.Loading
            try {
                val result = horarioCU.agregarHorario(grupoId, dia, horaInicio, horaFin)
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = HorarioUiState.Success("Horario agregado exitosamente")
                    }
                    is ValidationResult.SuccessWithData<*> -> {
                        recargar()
                        _uiState.value = HorarioUiState.Success("Horario agregado exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = HorarioUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HorarioUiState.Error("Error al agregar horario: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina un horario del sistema.
     */
    fun eliminarHorario(id: Int) {
        viewModelScope.launch {
            _uiState.value = HorarioUiState.Loading
            try {
                val result = horarioCU.eliminarHorario(id)
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = HorarioUiState.Success("Horario eliminado exitosamente")
                    }
                    is ValidationResult.SuccessWithData<*> -> {
                        recargar()
                        _uiState.value = HorarioUiState.Success("Horario eliminado exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = HorarioUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HorarioUiState.Error("Error al eliminar horario: ${e.message}")
            }
        }
    }
    
    /**
     * Limpia el estado de error.
     */
    fun clearError() {
        if (_uiState.value is HorarioUiState.Error) {
            _uiState.value = HorarioUiState.Idle
        }
    }
}
