package com.bo.asistenciaapp.domain.model

data class Horario(
    val id: Int,
    val grupoId: Int,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val materia: String,
    val grupo: String,
)
