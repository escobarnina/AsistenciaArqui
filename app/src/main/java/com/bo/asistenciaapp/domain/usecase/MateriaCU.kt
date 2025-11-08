package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Materia

class MateriaCU(private val db: AppDatabase) {
    fun obtenerMaterias(): List<Materia> {
        return db.obtenerMaterias()
    }

    fun agregarMateria(nombre: String, sigla: String, nivel: Int ) {
        db.agregarMateria(
            nombre,
            sigla,
            nivel
        )
    }
}