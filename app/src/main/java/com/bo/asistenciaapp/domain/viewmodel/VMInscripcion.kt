package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Boleta
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Horario
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import com.bo.asistenciaapp.data.repository.HorarioRepository
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
 * Modelo que combina un Grupo con sus horarios para mostrar información completa.
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
    
    private val _gruposInscritos = MutableStateFlow<List<GrupoConHorariosInscripcion>>(emptyList())
    val gruposInscritos: StateFlow<List<GrupoConHorariosInscripcion>> = _gruposInscritos.asStateFlow()
    
    private val _gruposDisponibles = MutableStateFlow<List<GrupoConHorariosInscripcion>>(emptyList())
    val gruposDisponibles: StateFlow<List<GrupoConHorariosInscripcion>> = _gruposDisponibles.asStateFlow()
    
    private val _uiState = MutableStateFlow<InscripcionUiState>(InscripcionUiState.Idle)
    val uiState: StateFlow<InscripcionUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    /**
     * Recarga las listas desde la base de datos.
     * Separa grupos inscritos vs disponibles.
     */
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = InscripcionUiState.Loading
            try {
                val boletas = inscripcionCU.obtenerInscripciones(alumnoId)
                _boletas.value = boletas
                
                val todosGrupos = grupoCU.obtenerGrupos()
                val gruposIdsInscritos = boletas.map { it.grupoId }.toSet()
                
                // Separar grupos inscritos vs disponibles
                val gruposInscritosList = mutableListOf<GrupoConHorariosInscripcion>()
                val gruposDisponiblesList = mutableListOf<GrupoConHorariosInscripcion>()
                
                todosGrupos.forEach { grupo ->
                    val horarios = horarioRepository.obtenerPorGrupo(grupo.id)
                    val grupoConHorarios = GrupoConHorariosInscripcion(grupo, horarios)
                    
                    if (gruposIdsInscritos.contains(grupo.id)) {
                        gruposInscritosList.add(grupoConHorarios)
                    } else {
                        gruposDisponiblesList.add(grupoConHorarios)
                    }
                }
                
                _gruposInscritos.value = gruposInscritosList
                _gruposDisponibles.value = gruposDisponiblesList
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
                    is ValidationResult.SuccessWithData<*> -> {
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
