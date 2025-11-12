package com.bo.asistenciaapp.data.repository

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Usuario

/**
 * Responsabilidad: Gestionar todas las operaciones relacionadas con usuarios.
 * 
 * Este repositorio abstrae el acceso a datos de usuarios, permitiendo:
 * - Cambiar la fuente de datos sin afectar los casos de uso
 * - Centralizar la lógica de acceso a datos de usuarios
 * - Facilitar pruebas unitarias
 * 
 * Usa UsuarioDao para las operaciones CRUD.
 */
class UsuarioRepository(private val database: AppDatabase) {
    
    /**
     * Valida las credenciales de un usuario.
     * 
     * @param username Nombre de usuario
     * @param contrasena Contraseña
     * @return Usuario si las credenciales son válidas, null en caso contrario
     */
    fun validarUsuario(username: String, contrasena: String): Usuario? {
        return database.usuarioDao.validarUsuario(username, contrasena)
    }
    
    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * @return Lista de usuarios ordenados por ID descendente
     */
    fun obtenerTodos(): List<Usuario> {
        return database.usuarioDao.obtenerTodos()
    }
    
    /**
     * Obtiene solo los usuarios con rol de docente.
     * 
     * @return Lista de docentes ordenados por ID descendente
     */
    fun obtenerDocentes(): List<Usuario> {
        return database.usuarioDao.obtenerDocentes()
    }
    
    /**
     * Agrega un nuevo usuario al sistema.
     * 
     * @param nombres Nombres del usuario
     * @param apellidos Apellidos del usuario
     * @param registro Número de registro
     * @param rol Rol del usuario (Admin, Docente, Alumno)
     * @param username Nombre de usuario único
     * @param contrasena Contraseña del usuario
     */
    fun agregar(
        nombres: String,
        apellidos: String,
        registro: String,
        rol: String,
        username: String,
        contrasena: String
    ) {
        database.usuarioDao.insertar(nombres, apellidos, registro, rol, username, contrasena)
    }
    
    /**
     * Elimina un usuario del sistema.
     * 
     * @param id ID del usuario a eliminar
     */
    fun eliminar(id: Int) {
        database.usuarioDao.eliminar(id)
    }
    
    /**
     * Actualiza los datos de un usuario existente.
     * 
     * @param id ID del usuario a actualizar
     * @param nombres Nuevos nombres
     * @param apellidos Nuevos apellidos
     * @param rol Nuevo rol
     */
    fun actualizar(id: Int, nombres: String, apellidos: String, rol: String) {
        database.usuarioDao.actualizar(id, nombres, apellidos, rol)
    }
}

