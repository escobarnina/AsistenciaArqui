package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.UsuarioRepository
import com.bo.asistenciaapp.domain.model.Usuario

/**
 * Caso de uso para gestionar usuarios.
 * 
 * Orquesta la lógica de negocio relacionada con usuarios,
 * utilizando el repositorio para acceder a los datos.
 */
class UsuarioCU(private val usuarioRepository: UsuarioRepository) {
    
    /**
     * Valida las credenciales de un usuario.
     */
    fun validarUsuario(username: String, contrasena: String): Usuario? {
        return usuarioRepository.validarUsuario(username, contrasena)
    }
    
    /**
     * Obtiene todos los usuarios del sistema.
     */
    fun obtenerUsuarios(): List<Usuario> {
        return usuarioRepository.obtenerTodos()
    }

    /**
     * Obtiene solo los usuarios con rol de docente.
     */
    fun obtenerDocentes(): List<Usuario> {
        return usuarioRepository.obtenerDocentes()
    }

    /**
     * Agrega un nuevo usuario al sistema.
     */
    fun agregarUsuario(
        nombres: String,
        apellidos: String,
        registro: String,
        rol: String,
        username: String,
        contrasena: String
    ) {
        usuarioRepository.agregar(nombres, apellidos, registro, rol, username, contrasena)
    }
    
    /**
     * Elimina un usuario del sistema.
     */
    fun eliminarUsuario(id: Int) {
        usuarioRepository.eliminar(id)
    }
    
    /**
     * Actualiza los datos de un usuario existente.
     */
    fun actualizarUsuario(id: Int, nombres: String, apellidos: String, rol: String) {
        usuarioRepository.actualizar(id, nombres, apellidos, rol)
    }
}