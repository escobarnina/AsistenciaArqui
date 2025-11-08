package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Boleta
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import com.bo.asistenciaapp.domain.usecase.InscripcionCU
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VMInscripcion(
    private val alumnoId: Int,
    private val grupoId: Int,
    private val fecha: String,
    private val semestre: Int,
    private val gestion: Int,
    private val boletaCU: InscripcionCU
) : ViewModel() {
    private val _boletas = MutableStateFlow<List<Boleta>>(emptyList())
    val grupos: StateFlow<List<Boleta>> get() = _boletas

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch { _boletas.value = boletaCU.obtenerInscripciones(alumnoId) }
    }

    fun registrar() {
        viewModelScope.launch {
            boletaCU.agregarInscripcion(
                alumnoId,
                grupoId,
                fecha,
                semestre,
                gestion,
            )
        }
    }
}