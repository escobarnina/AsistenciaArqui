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
     * Obtiene un grupo por su ID.
     * 
     * @param id ID del grupo
     * @return Grupo si existe, null en caso contrario
     */
    fun obtenerPorId(id: Int): Grupo? {
        return database.grupoDao.obtenerPorId(id)
    }
    
    /**
     * Actualiza la tolerancia de un grupo específico.
     * 
     * ⭐ PATRÓN STRATEGY - Configuración Dinámica:
     * Este método permite modificar el parámetro de tolerancia que utilizan
     * las estrategias de asistencia, haciendo el sistema completamente configurable
     * desde la interfaz de usuario.
     * 
     * @param id ID del grupo a actualizar
     * @param toleranciaMinutos Nueva tolerancia en minutos (0-60)
     */
    fun actualizarTolerancia(id: Int, toleranciaMinutos: Int) {
        database.grupoDao.actualizarTolerancia(id, toleranciaMinutos)
    }
    
    /**
     * Actualiza el tipo de estrategia de un grupo específico.
     * 
     * ⭐ PATRÓN STRATEGY - Configuración Dinámica:
     * Este método permite modificar qué estrategia utilizará el grupo
     * para calcular el estado de asistencia (PRESENTE, RETRASO, FALTA).
     * 
     * @param id ID del grupo a actualizar
     * @param tipoEstrategia Tipo de estrategia: "PRESENTE", "RETRASO" o "FALTA"
     */
    fun actualizarTipoEstrategia(id: Int, tipoEstrategia: String) {
        database.grupoDao.actualizarTipoEstrategia(id, tipoEstrategia)
    }
    
    /**
     * Verifica si existe un grupo con el ID especificado.
     * 
     * @param id ID del grupo a verificar
     * @return true si el grupo existe, false en caso contrario
     */
    fun existeGrupo(id: Int): Boolean {
        return database.grupoDao.existe(id)
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

