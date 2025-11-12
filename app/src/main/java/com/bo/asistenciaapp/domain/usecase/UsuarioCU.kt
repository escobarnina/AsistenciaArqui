package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.repository.UsuarioRepository
import com.bo.asistenciaapp.domain.model.Usuario
import com.bo.asistenciaapp.domain.utils.Validators
import com.bo.asistenciaapp.domain.utils.ValidationResult
import com.bo.asistenciaapp.domain.utils.validate

/**
 * Caso de uso para gestionar usuarios.
 * 
 * Orquesta la lógica de negocio relacionada con usuarios,
 * utilizando el repositorio para acceder a los datos.
 * 
 * Incluye validaciones de negocio antes de realizar operaciones.
 */
class UsuarioCU(private val usuarioRepository: UsuarioRepository) {
    
    /**
     * Valida las credenciales de un usuario.
     * 
     * @param username Nombre de usuario
     * @param contrasena Contraseña
     * @return Usuario si las credenciales son válidas, null en caso contrario
     */
    fun validarUsuario(username: String, contrasena: String): Usuario? {
        // Validar que los campos no estén vacíos
        if (!Validators.isNotEmpty(username) || !Validators.isNotEmpty(contrasena)) {
            return null
        }
        
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
     * Valida los datos de un usuario antes de agregarlo.
     * 
     * @return ValidationResult con el resultado de la validación
     */
    fun validarDatosUsuario(
        nombres: String,
        apellidos: String,
        registro: String,
        rol: String,
        username: String,
        contrasena: String
    ): ValidationResult {
        return validate(
            if (Validators.isNotEmpty(nombres)) ValidationResult.Success
            else ValidationResult.Error("Los nombres son requeridos"),
            
            if (Validators.isNotEmpty(apellidos)) ValidationResult.Success
            else ValidationResult.Error("Los apellidos son requeridos"),
            
            if (Validators.isNotEmpty(registro)) ValidationResult.Success
            else ValidationResult.Error("El registro es requerido"),
            
            if (Validators.isValidRol(rol)) ValidationResult.Success
            else ValidationResult.Error("El rol debe ser Admin, Docente o Alumno"),
            
            if (Validators.isValidUsername(username)) ValidationResult.Success
            else ValidationResult.Error("El username solo puede contener letras, números y guiones bajos"),
            
            if (Validators.hasMinLength(username, 3)) ValidationResult.Success
            else ValidationResult.Error("El username debe tener al menos 3 caracteres"),
            
            if (Validators.hasMaxLength(username, 20)) ValidationResult.Success
            else ValidationResult.Error("El username no puede tener más de 20 caracteres"),
            
            if (Validators.hasMinLength(contrasena, 4)) ValidationResult.Success
            else ValidationResult.Error("La contraseña debe tener al menos 4 caracteres")
        )
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
     * @return ValidationResult con el resultado de la operación
     */
    fun agregarUsuario(
        nombres: String,
        apellidos: String,
        registro: String,
        rol: String,
        username: String,
        contrasena: String
    ): ValidationResult {
        val validation = validarDatosUsuario(nombres, apellidos, registro, rol, username, contrasena)
        
        if (!validation.isValid) {
            return validation
        }
        
        // Verificar si el username ya existe (validación de negocio adicional)
        val usuarioExistente = usuarioRepository.obtenerTodos().find { it.username == username }
        if (usuarioExistente != null) {
            return ValidationResult.Error("El username '$username' ya está en uso")
        }
        
        usuarioRepository.agregar(nombres, apellidos, registro, rol, username, contrasena)
        return ValidationResult.Success
    }
    
    /**
     * Elimina un usuario del sistema.
     * 
     * @param id ID del usuario a eliminar
     * @return ValidationResult con el resultado de la operación
     */
    fun eliminarUsuario(id: Int): ValidationResult {
        if (!Validators.isPositive(id)) {
            return ValidationResult.Error("ID de usuario inválido")
        }
        
        // Verificar que el usuario existe
        val usuario = usuarioRepository.obtenerTodos().find { it.id == id }
        if (usuario == null) {
            return ValidationResult.Error("El usuario no existe")
        }
        
        usuarioRepository.eliminar(id)
        return ValidationResult.Success
    }
    
    /**
     * Valida los datos de actualización de un usuario.
     */
    fun validarDatosActualizacion(
        nombres: String,
        apellidos: String,
        rol: String
    ): ValidationResult {
        return validate(
            if (Validators.isNotEmpty(nombres)) ValidationResult.Success
            else ValidationResult.Error("Los nombres son requeridos"),
            
            if (Validators.isNotEmpty(apellidos)) ValidationResult.Success
            else ValidationResult.Error("Los apellidos son requeridos"),
            
            if (Validators.isValidRol(rol)) ValidationResult.Success
            else ValidationResult.Error("El rol debe ser Admin, Docente o Alumno")
        )
    }
    
    /**
     * Actualiza los datos de un usuario existente.
     * 
     * @param id ID del usuario a actualizar
     * @param nombres Nuevos nombres
     * @param apellidos Nuevos apellidos
     * @param rol Nuevo rol
     * @return ValidationResult con el resultado de la operación
     */
    fun actualizarUsuario(id: Int, nombres: String, apellidos: String, rol: String): ValidationResult {
        val validation = validarDatosActualizacion(nombres, apellidos, rol)
        
        if (!validation.isValid) {
            return validation
        }
        
        if (!Validators.isPositive(id)) {
            return ValidationResult.Error("ID de usuario inválido")
        }
        
        // Verificar que el usuario existe
        val usuario = usuarioRepository.obtenerTodos().find { it.id == id }
        if (usuario == null) {
            return ValidationResult.Error("El usuario no existe")
        }
        
        usuarioRepository.actualizar(id, nombres, apellidos, rol)
        return ValidationResult.Success
    }
}