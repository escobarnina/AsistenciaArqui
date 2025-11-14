package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.data.repository.HorarioRepository
import com.bo.asistenciaapp.domain.model.Boleta
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Horario
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la UI para gestión de inscripciones.
 */
sealed class InscripcionUiState {
    object Idle : InscripcionUiState()
    object Loading : InscripcionUiState()
    data class Success(val mensaje: String? = null) : InscripcionUiState()
    data class Error(val mensaje: String) : InscripcionUiState()
}

/**
 * Data class que combina un grupo con sus horarios.
 * 
 * Utilizado para mostrar información completa de grupos en las pantallas de inscripción.
 */
data class GrupoConHorariosInscripcion(
    val grupo: Grupo,
    val horarios: List<Horario>
)

/**
 * ViewModel para la gestión de inscripciones.
 * 
 * Maneja la lógica de negocio relacionada con inscripciones y el estado de la UI.
 */
class VMInscripcion(
    private val inscripcionCU: InscripcionCU,
    private val grupoCU: GrupoCU,
    private val alumnoId: Int,
    private val horarioRepository: HorarioRepository
) : ViewModel() {
    
    private val _boletas = MutableStateFlow<List<Boleta>>(emptyList())
    val boletas: StateFlow<List<Boleta>> = _boletas.asStateFlow()
    
    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> = _grupos.asStateFlow()
    
    private val _gruposDisponibles = MutableStateFlow<List<GrupoConHorariosInscripcion>>(emptyList())
    val gruposDisponibles: StateFlow<List<GrupoConHorariosInscripcion>> = _gruposDisponibles.asStateFlow()
    
    private val _gruposInscritos = MutableStateFlow<List<GrupoConHorariosInscripcion>>(emptyList())
    val gruposInscritos: StateFlow<List<GrupoConHorariosInscripcion>> = _gruposInscritos.asStateFlow()
    
    private val _uiState = MutableStateFlow<InscripcionUiState>(InscripcionUiState.Idle)
    val uiState: StateFlow<InscripcionUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    /**
     * Recarga las listas desde la base de datos.
     */
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = InscripcionUiState.Loading
            try {
                val boletasList = inscripcionCU.obtenerInscripciones(alumnoId)
                val gruposList = grupoCU.obtenerGrupos()
                
                _boletas.value = boletasList
                _grupos.value = gruposList
                
                // Obtener IDs de grupos inscritos
                val gruposInscritosIds = boletasList.map { it.grupoId }.toSet()
                
                // Separar grupos disponibles e inscritos
                val gruposDisponiblesList = gruposList.filter { it.id !in gruposInscritosIds }
                val gruposInscritosList = gruposList.filter { it.id in gruposInscritosIds }
                
                // Combinar grupos con sus horarios
                _gruposDisponibles.value = gruposDisponiblesList.map { grupo ->
                    GrupoConHorariosInscripcion(
                        grupo = grupo,
                        horarios = horarioRepository.obtenerPorGrupo(grupo.id)
                    )
                }
                
                _gruposInscritos.value = gruposInscritosList.map { grupo ->
                    GrupoConHorariosInscripcion(
                        grupo = grupo,
                        horarios = horarioRepository.obtenerPorGrupo(grupo.id)
                    )
                }
                
                _uiState.value = InscripcionUiState.Success()
            } catch (e: Exception) {
                _uiState.value = InscripcionUiState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    /**
     * Verifica si hay cruce de horarios.
     */
    fun tieneCruceDeHorario(grupoId: Int): Boolean {
        return inscripcionCU.tieneCruceDeHorario(alumnoId, grupoId)
    }

    /**
     * Registra una nueva inscripción.
     */
    fun registrarInscripcion(
        grupoId: Int,
        fecha: String,
        semestre: Int,
        gestion: Int
    ) {
        viewModelScope.launch {
            _uiState.value = InscripcionUiState.Loading
            try {
                val result = inscripcionCU.agregarInscripcion(
                    alumnoId, grupoId, fecha, semestre, gestion
                )
                
                when (result) {
                    is ValidationResult.Success -> {
                        recargar()
                        _uiState.value = InscripcionUiState.Success("Inscripción realizada exitosamente")
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = InscripcionUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = InscripcionUiState.Error("Error al registrar inscripción: ${e.message}")
            }
        }
    }
    
    /**
     * Limpia el estado de error.
     */
    fun clearError() {
        if (_uiState.value is InscripcionUiState.Error) {
            _uiState.value = InscripcionUiState.Idle
        }
    }
}
