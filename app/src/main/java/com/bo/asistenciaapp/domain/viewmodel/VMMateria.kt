package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.usecase.MateriaCU
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VMMateria(
    private val nombre: String,
    private val sigla: String,
    private val nivel: Int,
    private val materiaCU: MateriaCU
) : ViewModel() {
    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> get() = _materias

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch { _materias.value = materiaCU.obtenerMaterias() }
    }

    fun registrar() {
        viewModelScope.launch {
            materiaCU.agregarMateria(
                nombre,
                sigla,
                nivel
            )
        }
    }
}