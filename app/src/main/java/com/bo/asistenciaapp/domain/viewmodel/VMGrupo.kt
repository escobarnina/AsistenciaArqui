package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.MateriaCU
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la UI para gestión de grupos.
 */
sealed class GrupoUiState {
    object Idle : GrupoUiState()
    object Loading : GrupoUiState()
    data class Success(val mensaje: String? = null) : GrupoUiState()
    data class Error(val mensaje: String) : GrupoUiState()
}

/**
 * ViewModel para la gestión de grupos.
 * 
 * Maneja la lógica de negocio relacionada con grupos y el estado de la UI.
 */
class VMGrupo(
    private val grupoCU: GrupoCU,
    private val materiaCU: MateriaCU,
    private val usuarioCU: UsuarioCU
) : ViewModel() {
    
    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> = _grupos.asStateFlow()
    
    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias.asStateFlow()
    
    private val _docentes = MutableStateFlow<List<Usuario>>(emptyList())
    val docentes: StateFlow<List<Usuario>> = _docentes.asStateFlow()
    
    private val _uiState = MutableStateFlow<GrupoUiState>(GrupoUiState.Idle)
    val uiState: StateFlow<GrupoUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    /**
     * Recarga las listas desde la base de datos.
     */
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = GrupoUiState.Loading
            try {
                _grupos.value = grupoCU.obtenerGrupos()
                _materias.value = materiaCU.obtenerMaterias()
                _docentes.value = usuarioCU.obtenerDocentes()
                _uiState.value = GrupoUiState.Success()
            } catch (e: Exception) {
                _uiState.value = GrupoUiState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    /**
     * Agrega un nuevo grupo al sistema.
     */
    fun agregarGrupo(
        materiaId: Int,
        materiaNombre: String,
        docenteId: Int,
        docenteNombre: String,
        semestre: Int,
        gestion: Int,
        capacidad: Int,
        grupo: String
    ) {
        viewModelScope.launch {
            _uiState.value = GrupoUiState.Loading
            try {
                val result = grupoCU.agregarGrupo(
                    materiaId, materiaNombre, docenteId, docenteNombre,
                    semestre, gestion, capacidad, grupo
                )
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = GrupoUiState.Success("Grupo agregado exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = GrupoUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = GrupoUiState.Error("Error al agregar grupo: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina un grupo del sistema.
     */
    fun eliminarGrupo(id: Int) {
        viewModelScope.launch {
            _uiState.value = GrupoUiState.Loading
            try {
                val result = grupoCU.eliminarGrupo(id)
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = GrupoUiState.Success("Grupo eliminado exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = GrupoUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = GrupoUiState.Error("Error al eliminar grupo: ${e.message}")
            }
        }
    }
    
    /**
     * Limpia el estado de error.
     */
    fun clearError() {
        if (_uiState.value is GrupoUiState.Error) {
            _uiState.value = GrupoUiState.Idle
        }
    }
}
