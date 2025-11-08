package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Boleta
import com.bo.asistenciaapp.domain.model.Horario

class InscripcionCU(
    private val alumnoId: Int,
    private val db: AppDatabase
) {
    fun obtenerInscripciones(alumnoId: Int): List<Boleta> {
        return db.obtenerBoletasPorAlumno(alumnoId)
    }

    fun agregarInscripcion(
        alumnoId: Int, grupoId: Int, fecha: String, semestre: Int, gestion: Int
    ) {
        db.registrarBoleta(
            alumnoId,
            grupoId,
            fecha,
            semestre,
            gestion
        )
    }
}