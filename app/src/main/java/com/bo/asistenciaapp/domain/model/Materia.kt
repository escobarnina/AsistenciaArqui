package com.bo.asistenciaapp.domain.model

/**
 * Modelo de dominio que representa una materia académica.
 * 
 * Este modelo representa la entidad Materia en la capa de dominio.
 * Corresponde a la tabla `materias` en la base de datos SQLite.
 * 
 * Propiedades:
 * - `id`: Identificador único de la materia (clave primaria)
 * - `nombre`: Nombre completo de la materia (ej: "Programación I")
 * - `sigla`: Sigla única de la materia (ej: "PROG1"). Debe ser única en el sistema
 * - `nivel`: Nivel académico de la materia. Valores típicos: 1-10
 * 
 * Ejemplo de uso:
 * ```kotlin
 * val materia = Materia(
 *     id = 1,
 *     nombre = "Programación I",
 *     sigla = "PROG1",
 *     nivel = 1
 * )
 * ```
 * 
 * Relaciones:
 * - Una Materia puede tener múltiples Grupos asociados
 * - Los Grupos referencian a una Materia mediante `materiaId`
 * 
 * Validaciones de negocio:
 * - La sigla debe ser única en el sistema
 * - El nivel debe estar entre 1 y 10
 * - El nombre debe tener al menos 3 caracteres
 */
data class Materia(
    val id: Int,
    val nombre: String,
    val sigla: String,
    val nivel: Int
)
