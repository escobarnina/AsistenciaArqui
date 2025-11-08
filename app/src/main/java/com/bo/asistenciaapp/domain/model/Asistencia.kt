package com.bo.asistenciaapp.domain.model

data class Asistencia(
    val id: Int,
    val alumnoId: Int,
    val grupoId: Int,
    val fecha: String,
    val grupo: String,
    val materiaNombre: String

)
