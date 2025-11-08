package com.bo.asistenciaapp.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.usecase.UsuarioCU
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VMUsuario(
    private val nombres: String,
    private val apellidos: String,
    private val registro: String,
    private val rol: String,
    private val username: String,
    private val contrasena: String,
    private val usuarioCU: UsuarioCU
) : ViewModel() {
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> get() = _usuarios

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch { _usuarios.value = usuarioCU.obtenerUsuarios() }
    }

    fun registrar() {
        viewModelScope.launch {
            usuarioCU.agregarUsuario(nombres, apellidos, registro, rol, username, contrasena)
        }
    }
}