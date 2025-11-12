package com.bo.asistenciaapp.data.repository

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Grupo

/**
 * Responsabilidad: Gestionar todas las operaciones relacionadas con grupos.
 * 
 * Este repositorio abstrae el acceso a datos de grupos, permitiendo:
 * - Cambiar la fuente de datos sin afectar los casos de uso
 * - Centralizar la lógica de acceso a datos de grupos
 * - Facilitar pruebas unitarias
 */
class GrupoRepository(private val database: AppDatabase) {
    
    /**
     * Obtiene todos los grupos del sistema.
     * 
     * @return Lista de grupos
     */
    fun obtenerTodos(): List<Grupo> {
        return database.grupoDao.obtenerTodos()
    }
    
    /**
     * Agrega un nuevo grupo al sistema.
     * 
     * @param materiaId ID de la materia
     * @param materiaNombre Nombre de la materia
     * @param docenteId ID del docente
     * @param docenteNombre Nombre del docente
     * @param semestre Semestre (1 o 2)
     * @param gestion Año de gestión
     * @param capacidad Capacidad máxima del grupo
     * @param grupo Nombre/paralelo del grupo
     */
    fun agregar(
        materiaId: Int,
        materiaNombre: String,
        docenteId: Int,
        docenteNombre: String,
        semestre: Int,
        gestion: Int,
        capacidad: Int,
        grupo: String
    ) {
        database.grupoDao.insertar(
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
    
    /**
     * Obtiene todos los grupos asignados a un docente específico.
     * 
     * @param docenteId ID del docente
     * @return Lista de grupos asignados al docente
     */
    fun obtenerPorDocente(docenteId: Int): List<Grupo> {
        return database.grupoDao.obtenerPorDocente(docenteId)
    }
    
    /**
     * Elimina un grupo del sistema.
     * 
     * @param id ID del grupo a eliminar
     */
    fun eliminar(id: Int) {
        database.grupoDao.eliminar(id)
    }
}

