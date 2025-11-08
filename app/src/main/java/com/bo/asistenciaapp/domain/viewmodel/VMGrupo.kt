package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.usecase.GrupoCU
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VMGrupo(
    private val materiaId: Int,
    private val materiaNombre: String,
    private val docenteId: Int,
    private val docenteNombre: String,
    private val semestre: Int,
    private val gestion: Int,
    private val capacidad: Int,
    private val grupo: String,
    private val grupoCU: GrupoCU
) : ViewModel() {
    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> get() = _grupos

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch { _grupos.value = grupoCU.obtenerGrupos() }
    }

    fun registrar() {
        viewModelScope.launch {
            grupoCU.agregarGrupo(
                materiaId,
                materiaNombre,
                docenteId,
                docenteNombre,
                semestre,
                gestion,
                capacidad,
                grupo
            )
        }
    }
}