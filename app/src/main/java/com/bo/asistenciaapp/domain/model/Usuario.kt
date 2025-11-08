package com.bo.asistenciaapp.domain.model

data class Usuario (
    val id: Int,
    val nombres: String,
    val apellidos: String,
    val registro: String,
    val rol: String,
    val username: String,
//    val contrasena: String
)