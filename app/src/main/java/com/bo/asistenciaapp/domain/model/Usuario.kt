package com.bo.asistenciaapp.domain.model

/**
 * Modelo de dominio que representa un usuario del sistema.
 * 
 * Este modelo representa la entidad Usuario en la capa de dominio.
 * Corresponde a la tabla `usuarios` en la base de datos SQLite.
 * 
 * Propiedades:
 * - `id`: Identificador único del usuario (clave primaria)
 * - `nombres`: Nombres del usuario
 * - `apellidos`: Apellidos del usuario
 * - `registro`: Número de registro único del usuario
 * - `rol`: Rol del usuario en el sistema. Valores válidos: "Admin", "Docente", "Alumno"
 * - `username`: Nombre de usuario único para autenticación
 * 
 * Nota: La contraseña no se incluye en este modelo por razones de seguridad.
 * Solo se almacena en la base de datos y se usa durante la autenticación.
 * 
 * Ejemplo de uso:
 * ```kotlin
 * val usuario = Usuario(
 *     id = 1,
 *     nombres = "Juan",
 *     apellidos = "Pérez",
 *     registro = "212732",
 *     rol = "Alumno",
 *     username = "alumno2"
 * )
 * ```
 * 
 * Relaciones:
 * - Un Usuario puede tener múltiples Grupos asignados (si es Docente)
 * - Un Usuario puede tener múltiples Boletas/Inscripciones (si es Alumno)
 * - Un Usuario puede tener múltiples Asistencias (si es Alumno)
 */
data class Usuario(
    val id: Int,
    val nombres: String,
    val apellidos: String,
    val registro: String,
    val rol: String,
    val username: String
    // Nota: La contraseña no se incluye en el modelo por seguridad
)