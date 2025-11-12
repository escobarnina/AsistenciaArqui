package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.MateriaRepository
import com.bo.asistenciaapp.domain.model.Materia

/**
 * Caso de uso para gestionar materias.
 * 
 * Orquesta la lógica de negocio relacionada con materias,
 * utilizando el repositorio para acceder a los datos.
 */
class MateriaCU(private val materiaRepository: MateriaRepository) {
    
    /**
     * Obtiene todas las materias del sistema.
     */
    fun obtenerMaterias(): List<Materia> {
        return materiaRepository.obtenerTodas()
    }

    /**
     * Agrega una nueva materia al sistema.
     */
    fun agregarMateria(nombre: String, sigla: String, nivel: Int) {
        materiaRepository.agregar(nombre, sigla, nivel)
    }
    
    /**
     * Elimina una materia del sistema.
     */
    fun eliminarMateria(id: Int) {
        materiaRepository.eliminar(id)
    }
}