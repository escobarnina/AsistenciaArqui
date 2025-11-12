package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.MateriaRepository
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.utils.Validators
import com.bo.asistenciaapp.domain.utils.ValidationResult
import com.bo.asistenciaapp.domain.utils.validate

/**
 * Caso de uso para gestionar materias.
 * 
 * Orquesta la lógica de negocio relacionada con materias,
 * utilizando el repositorio para acceder a los datos.
 * 
 * Incluye validaciones de negocio antes de realizar operaciones.
 */
class MateriaCU(private val materiaRepository: MateriaRepository) {
    
    /**
     * Obtiene todas las materias del sistema.
     */
    fun obtenerMaterias(): List<Materia> {
        return materiaRepository.obtenerTodas()
    }

    /**
     * Valida los datos de una materia antes de agregarla.
     */
    fun validarDatosMateria(nombre: String, sigla: String, nivel: Int): ValidationResult {
        return validate(
            if (Validators.isNotEmpty(nombre)) ValidationResult.Success
            else ValidationResult.Error("El nombre de la materia es requerido"),
            
            if (Validators.hasMinLength(nombre, 3)) ValidationResult.Success
            else ValidationResult.Error("El nombre debe tener al menos 3 caracteres"),
            
            if (Validators.isNotEmpty(sigla)) ValidationResult.Success
            else ValidationResult.Error("La sigla es requerida"),
            
            if (Validators.hasLengthBetween(sigla, 2, 10)) ValidationResult.Success
            else ValidationResult.Error("La sigla debe tener entre 2 y 10 caracteres"),
            
            if (Validators.isValidNivel(nivel)) ValidationResult.Success
            else ValidationResult.Error("El nivel debe estar entre 1 y 10")
        )
    }

    /**
     * Agrega una nueva materia al sistema.
     * 
     * @param nombre Nombre de la materia
     * @param sigla Sigla única de la materia
     * @param nivel Nivel académico (1-10)
     * @return ValidationResult con el resultado de la operación
     */
    fun agregarMateria(nombre: String, sigla: String, nivel: Int): ValidationResult {
        val validation = validarDatosMateria(nombre, sigla, nivel)
        
        if (!validation.isValid) {
            return validation
        }
        
        // Verificar si la sigla ya existe (validación de negocio adicional)
        val materiaExistente = materiaRepository.obtenerTodas().find { 
            it.sigla.equals(sigla, ignoreCase = true) 
        }
        if (materiaExistente != null) {
            return ValidationResult.Error("La sigla '$sigla' ya está en uso")
        }
        
        materiaRepository.agregar(nombre, sigla, nivel)
        return ValidationResult.Success
    }
    
    /**
     * Elimina una materia del sistema.
     * 
     * @param id ID de la materia a eliminar
     * @return ValidationResult con el resultado de la operación
     */
    fun eliminarMateria(id: Int): ValidationResult {
        if (!Validators.isPositive(id)) {
            return ValidationResult.Error("ID de materia inválido")
        }
        
        // Verificar que la materia existe
        val materia = materiaRepository.obtenerTodas().find { it.id == id }
        if (materia == null) {
            return ValidationResult.Error("La materia no existe")
        }
        
        materiaRepository.eliminar(id)
        return ValidationResult.Success
    }
}