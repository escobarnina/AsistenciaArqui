package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Materia

class GrupoCU(private val db: AppDatabase) {
    fun obtenerGrupos(): List<Grupo> {
        return db.obtenerGrupos()
    }

    fun agregarGrupo(
        materiaId: Int,
        materiaNombre: String,
        docenteId: Int,
        docenteNombre: String,
        semestre: Int,
        gestion: Int,
        capacidad: Int,
        grupo: String
    ) {
        db.agregarGrupo(
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