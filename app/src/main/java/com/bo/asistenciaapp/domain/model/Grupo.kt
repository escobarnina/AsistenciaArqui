package com.bo.asistenciaapp.domain.model

data class Grupo(
    val id: Int,
    val grupo: String,
    val materiaId: Int,
    val materiaNombre: String,
    val docenteId: Int,
    val docenteNombre: String,
    val semestre: Int,
    val gestion: Int,
    val capacidad: Int,
    val nroInscritos: Int
)
