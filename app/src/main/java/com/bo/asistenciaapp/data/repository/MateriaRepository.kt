package com.bo.asistenciaapp.data.repository

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Materia

/**
 * Responsabilidad: Gestionar todas las operaciones relacionadas con materias.
 * 
 * Este repositorio abstrae el acceso a datos de materias, permitiendo:
 * - Cambiar la fuente de datos sin afectar los casos de uso
 * - Centralizar la lógica de acceso a datos de materias
 * - Facilitar pruebas unitarias
 */
class MateriaRepository(private val database: AppDatabase) {
    
    /**
     * Obtiene todas las materias del sistema.
     * 
     * @return Lista de materias ordenadas por ID descendente
     */
    fun obtenerTodas(): List<Materia> {
        return database.materiaDao.obtenerTodas()
    }
    
    /**
     * Agrega una nueva materia al sistema.
     * 
     * @param nombre Nombre de la materia
     * @param sigla Sigla única de la materia
     * @param nivel Nivel de la materia
     */
    fun agregar(nombre: String, sigla: String, nivel: Int) {
        database.materiaDao.insertar(nombre, sigla, nivel)
    }
    
    /**
     * Elimina una materia del sistema.
     * 
     * @param id ID de la materia a eliminar
     */
    fun eliminar(id: Int) {
        database.materiaDao.eliminar(id)
    }
}

