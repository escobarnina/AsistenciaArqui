package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Horario

class HorarioCU(private val db: AppDatabase) {
    fun obtenerHorarios(): List<Horario> {
        return db.obtenerHorarios()
    }

    fun agregarHorario(
        grupoId: Int, dia: String, horaInicio: String, horaFin: String
    ) {
        db.agregarHorario(
            grupoId,
            dia,
            horaInicio,
            horaFin
        )
    }
}