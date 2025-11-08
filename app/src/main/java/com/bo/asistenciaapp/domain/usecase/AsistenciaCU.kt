package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.model.Usuario


class AsistenciaCU(private val db: AppDatabase) {
    fun obtenerAsistencias(alumnoId: Int): List<Asistencia> {
        return db.obtenerAsistenciasPorAlumno(alumnoId)
    }

    fun marcarAsistencia(alumnoId: Int, grupoId: Int, fecha: String) {
        db.registrarAsistencia(
            alumnoId, grupoId, fecha
        )
    }

    fun puedeMarcarAsistencia(alumnoId: Int, grupoId: Int): Boolean {
        return db.puedeMarcarAsistencia(alumnoId, grupoId)
    }
}