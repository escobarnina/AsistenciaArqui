package com.bo.asistenciaapp.domain.model

data class Boleta(
    val id: Int,
    val alumnoId: Int,
    val grupoId: Int,
    val fecha: String,
    val semestre: Int,
    val gestion: Int,
    val grupo: String,
    val materiaNombre: String,
    val dia: String,
    val horario: String,
)
