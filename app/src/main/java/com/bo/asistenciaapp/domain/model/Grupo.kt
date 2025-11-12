package com.bo.asistenciaapp.domain.model

/**
 * Modelo de dominio que representa un grupo académico.
 * 
 * Este modelo representa la entidad Grupo en la capa de dominio.
 * Corresponde a la tabla `grupos` en la base de datos SQLite.
 * 
 * Un grupo representa una instancia de una materia asignada a un docente
 * en un semestre y año de gestión específicos, con un paralelo (A, B, etc.).
 * 
 * Propiedades:
 * - `id`: Identificador único del grupo (clave primaria)
 * - `grupo`: Nombre/paralelo del grupo (ej: "A", "B", "1")
 * - `materiaId`: ID de la materia asociada (clave foránea)
 * - `materiaNombre`: Nombre de la materia (denormalizado para facilitar consultas)
 * - `docenteId`: ID del docente asignado (clave foránea a usuarios)
 * - `docenteNombre`: Nombre del docente (denormalizado para facilitar consultas)
 * - `semestre`: Semestre académico. Valores válidos: 1 o 2
 * - `gestion`: Año de gestión académica (ej: 2025)
 * - `capacidad`: Capacidad máxima de estudiantes en el grupo
 * - `nroInscritos`: Número actual de estudiantes inscritos en el grupo
 * 
 * Ejemplo de uso:
 * ```kotlin
 * val grupo = Grupo(
 *     id = 1,
 *     grupo = "A",
 *     materiaId = 1,
 *     materiaNombre = "Programación I",
 *     docenteId = 4,
 *     docenteNombre = "Marcos Rodríguez",
 *     semestre = 1,
 *     gestion = 2025,
 *     capacidad = 30,
 *     nroInscritos = 15
 * )
 * ```
 * 
 * Relaciones:
 * - Un Grupo pertenece a una Materia (materiaId)
 * - Un Grupo está asignado a un Docente (docenteId)
 * - Un Grupo puede tener múltiples Horarios
 * - Un Grupo puede tener múltiples Boletas/Inscripciones
 * - Un Grupo puede tener múltiples Asistencias
 * 
 * Validaciones de negocio:
 * - El semestre debe ser 1 o 2
 * - La capacidad debe ser mayor a 0 y típicamente entre 1 y 100
 * - El número de inscritos no puede exceder la capacidad
 * - El año de gestión debe ser válido (año actual ± 5 años)
 */
data class Grupo(
    val id: Int,
    val grupo: String,
    val materiaId: Int,
    val materiaNombre: String,
    val docenteId: Int,
    val docenteNombre: String,
    val semestre: Int,
    val gestion: Int,
    val capacidad: Int,
    val nroInscritos: Int
)
