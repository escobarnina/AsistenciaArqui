package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.usecase.AsistenciaCU
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VMAsistencia(
    private val alumnoId: Int,
    private val grupoId: Int,
    private val fecha: String,
    private val asistenciaCU: AsistenciaCU
) : ViewModel() {
    private val _asistencias = MutableStateFlow<List<Asistencia>>(emptyList())
    val asistencias: StateFlow<List<Asistencia>> get() = _asistencias

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch { _asistencias.value = asistenciaCU.obtenerAsistencias(alumnoId) }
    }

    fun puedeMarcarAsistencia() {
        viewModelScope.launch {
            asistenciaCU.puedeMarcarAsistencia(
                alumnoId, grupoId
            )
        }
    }

    fun registrar() {
        viewModelScope.launch {
            asistenciaCU.marcarAsistencia(
                alumnoId, grupoId, fecha
            )
        }
    }
}