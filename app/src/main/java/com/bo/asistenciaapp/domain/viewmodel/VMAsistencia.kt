package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Horario
import com.bo.asistenciaapp.domain.usecase.AsistenciaCU
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import com.bo.asistenciaapp.domain.utils.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Estado de la UI para gestión de asistencias.
 */
sealed class AsistenciaUiState {
    object Idle : AsistenciaUiState()
    object Loading : AsistenciaUiState()
    data class Success(val mensaje: String? = null, val estadoAsistencia: String? = null) : AsistenciaUiState()
    data class Error(val mensaje: String) : AsistenciaUiState()
}

/**
 * Modelo que combina un Grupo con sus horarios para mostrar información completa.
 */
data class GrupoConHorarios(
    val grupo: Grupo,
    val horarios: List<Horario>
)

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
    
    private val _gruposDisponiblesAhora = MutableStateFlow<List<GrupoConHorarios>>(emptyList())
    val gruposDisponiblesAhora: StateFlow<List<GrupoConHorarios>> = _gruposDisponiblesAhora.asStateFlow()
    
    private val _gruposProximos = MutableStateFlow<List<GrupoConHorarios>>(emptyList())
    val gruposProximos: StateFlow<List<GrupoConHorarios>> = _gruposProximos.asStateFlow()
    
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Idle)
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    /**
     * Recarga las listas desde la base de datos.
     * Separa los grupos en disponibles ahora y próximos según horarios.
     */
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = AsistenciaUiState.Loading
            try {
                // Obtener grupos en los que el alumno está inscrito
                val boletas = inscripcionCU.obtenerInscripciones(alumnoId)
                val todosGrupos = grupoCU.obtenerGrupos()
                val gruposInscritos = todosGrupos.filter { grupo ->
                    boletas.any { it.grupoId == grupo.id }
                }
                
                // Obtener horarios para cada grupo y separar disponibles ahora vs próximos
                val gruposConHorarios = gruposInscritos.map { grupo ->
                    val horarios = asistenciaCU.obtenerHorariosGrupo(grupo.id)
                    GrupoConHorarios(grupo, horarios)
                }
                
                val (disponiblesAhora, proximos) = separarGruposPorDisponibilidad(gruposConHorarios)
                
                _gruposDisponiblesAhora.value = disponiblesAhora
                _gruposProximos.value = proximos
                _uiState.value = AsistenciaUiState.Success()
            } catch (e: Exception) {
                _uiState.value = AsistenciaUiState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }
    
    /**
     * Separa los grupos en disponibles ahora y próximos según la hora actual y día.
     */
    private fun separarGruposPorDisponibilidad(
        gruposConHorarios: List<GrupoConHorarios>
    ): Pair<List<GrupoConHorarios>, List<GrupoConHorarios>> {
        val calendario = Calendar.getInstance()
        val diaActual = obtenerNombreDia(calendario.get(Calendar.DAY_OF_WEEK))
        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendario.time)
        
        val disponiblesAhora = mutableListOf<GrupoConHorarios>()
        val proximos = mutableListOf<GrupoConHorarios>()
        
        gruposConHorarios.forEach { grupoConHorarios ->
            // Verificar si hay un horario que coincide con el día y hora actual
            val tieneHorarioDisponibleAhora = grupoConHorarios.horarios.any { horario ->
                horario.dia.equals(diaActual, ignoreCase = true) &&
                horario.horaInicio <= horaActual &&
                horario.horaFin >= horaActual
            }
            
            // Si tiene horario disponible ahora, verificar si puede marcar asistencia
            if (tieneHorarioDisponibleAhora && asistenciaCU.puedeMarcarAsistencia(alumnoId, grupoConHorarios.grupo.id)) {
                disponiblesAhora.add(grupoConHorarios)
            } else {
                proximos.add(grupoConHorarios)
            }
        }
        
        // Ordenar grupos próximos por día y hora
        proximos.sortWith(compareBy<GrupoConHorarios> { grupoConHorarios ->
            val proximoHorario = obtenerProximoHorario(grupoConHorarios.horarios, diaActual, horaActual)
            proximoHorario?.let { "${it.dia}-${it.horaInicio}" } ?: "ZZZ"
        })
        
        return Pair(disponiblesAhora, proximos)
    }
    
    /**
     * Obtiene el próximo horario disponible para un grupo.
     */
    private fun obtenerProximoHorario(
        horarios: List<Horario>,
        diaActual: String,
        horaActual: String
    ): Horario? {
        val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val indiceDiaActual = diasSemana.indexOfFirst { it.equals(diaActual, ignoreCase = true) }
        
        // Buscar en el día actual primero
        val horariosHoy = horarios.filter { it.dia.equals(diaActual, ignoreCase = true) }
        val proximoHoy = horariosHoy.find { it.horaInicio > horaActual }
        if (proximoHoy != null) return proximoHoy
        
        // Buscar en los días siguientes de la semana
        for (i in 1..7) {
            val indiceSiguiente = (indiceDiaActual + i) % 7
            val diaSiguiente = diasSemana[indiceSiguiente]
            val horariosDiaSiguiente = horarios.filter { it.dia.equals(diaSiguiente, ignoreCase = true) }
            if (horariosDiaSiguiente.isNotEmpty()) {
                return horariosDiaSiguiente.minByOrNull { it.horaInicio }
            }
        }
        
        return horarios.minByOrNull { it.horaInicio }
    }
    
    /**
     * Convierte el número del día de la semana a nombre en español.
     */
    private fun obtenerNombreDia(diaSemana: Int): String {
        val dias = arrayOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        return dias[diaSemana - 1]
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
                    is ValidationResult.SuccessWithData<*> -> {
                        recargar()
                        val estado = result.data as? String ?: "PRESENTE"
                        _uiState.value = AsistenciaUiState.Success("Asistencia registrada exitosamente", estado)
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
    
    /**
     * Limpia el estado de éxito después de mostrar el diálogo.
     */
    fun clearSuccess() {
        if (_uiState.value is AsistenciaUiState.Success) {
            _uiState.value = AsistenciaUiState.Idle
        }
    }
}
