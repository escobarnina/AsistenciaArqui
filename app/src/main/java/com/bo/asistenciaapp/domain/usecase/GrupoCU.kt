package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.GrupoRepository
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.utils.Validators
import com.bo.asistenciaapp.domain.utils.ValidationResult
import com.bo.asistenciaapp.domain.utils.validate

/**
 * Caso de uso para gestionar grupos.
 * 
 * Orquesta la lógica de negocio relacionada con grupos,
 * utilizando el repositorio para acceder a los datos.
 * 
 * Incluye validaciones de negocio antes de realizar operaciones.
 */
class GrupoCU(private val grupoRepository: GrupoRepository) {
    
    /**
     * Obtiene todos los grupos del sistema.
     */
    fun obtenerGrupos(): List<Grupo> {
        return grupoRepository.obtenerTodos()
    }

    /**
     * Valida los datos de un grupo antes de agregarlo.
     */
    fun validarDatosGrupo(
        materiaId: Int,
        materiaNombre: String,
        docenteId: Int,
        docenteNombre: String,
        semestre: Int,
        gestion: Int,
        capacidad: Int,
        grupo: String
    ): ValidationResult {
        return validate(
            if (Validators.isPositive(materiaId)) ValidationResult.Success
            else ValidationResult.Error("ID de materia inválido"),
            
            if (Validators.isNotEmpty(materiaNombre)) ValidationResult.Success
            else ValidationResult.Error("El nombre de la materia es requerido"),
            
            if (Validators.isPositive(docenteId)) ValidationResult.Success
            else ValidationResult.Error("ID de docente inválido"),
            
            if (Validators.isNotEmpty(docenteNombre)) ValidationResult.Success
            else ValidationResult.Error("El nombre del docente es requerido"),
            
            if (Validators.isValidSemestre(semestre)) ValidationResult.Success
            else ValidationResult.Error("El semestre debe ser 1 o 2"),
            
            if (Validators.isValidGestion(gestion)) ValidationResult.Success
            else ValidationResult.Error("El año de gestión es inválido"),
            
            if (Validators.isPositive(capacidad)) ValidationResult.Success
            else ValidationResult.Error("La capacidad debe ser mayor a 0"),
            
            if (Validators.isInRange(capacidad, 1, 100)) ValidationResult.Success
            else ValidationResult.Error("La capacidad debe estar entre 1 y 100"),
            
            if (Validators.isNotEmpty(grupo)) ValidationResult.Success
            else ValidationResult.Error("El nombre del grupo es requerido")
        )
    }

    /**
     * Agrega un nuevo grupo al sistema.
     * 
     * @return ValidationResult con el resultado de la operación
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
    ): ValidationResult {
        val validation = validarDatosGrupo(
            materiaId, materiaNombre, docenteId, docenteNombre,
            semestre, gestion, capacidad, grupo
        )
        
        if (!validation.isValid) {
            return validation
        }
        
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
        return ValidationResult.Success
    }
    
    /**
     * Elimina un grupo del sistema.
     * 
     * @param id ID del grupo a eliminar
     * @return ValidationResult con el resultado de la operación
     */
    fun eliminarGrupo(id: Int): ValidationResult {
        if (!Validators.isPositive(id)) {
            return ValidationResult.Error("ID de grupo inválido")
        }
        
        // Verificar que el grupo existe
        val grupo = grupoRepository.obtenerTodos().find { it.id == id }
        if (grupo == null) {
            return ValidationResult.Error("El grupo no existe")
        }
        
        grupoRepository.eliminar(id)
        return ValidationResult.Success
    }
}