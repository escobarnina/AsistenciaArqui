package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.domain.model.Grupo

/**
 * Caso de uso para gestionar grupos.
 * 
 * Orquesta la lógica de negocio relacionada con grupos,
 * utilizando el repositorio para acceder a los datos.
 */
class GrupoCU(private val grupoRepository: GrupoRepository) {
    
    /**
     * Obtiene todos los grupos del sistema.
     */
    fun obtenerGrupos(): List<Grupo> {
        return grupoRepository.obtenerTodos()
    }

    /**
     * Agrega un nuevo grupo al sistema.
     */
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
        grupoRepository.agregar(
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
     * Elimina un grupo del sistema.
     */
    fun eliminarGrupo(id: Int) {
        grupoRepository.eliminar(id)
    }
}