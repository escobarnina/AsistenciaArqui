package com.bo.asistenciaapp.domain.model

/**
 * Modelo de dominio que representa una boleta de inscripción.
 * 
 * Este modelo representa la entidad Boleta (Inscripción) en la capa de dominio.
 * Corresponde a la tabla `boletas` en la base de datos SQLite.
 * 
 * Una boleta representa la inscripción de un alumno en un grupo específico
 * para un semestre y año de gestión determinado.
 * 
 * Propiedades:
 * - `id`: Identificador único de la boleta (clave primaria)
 * - `alumnoId`: ID del alumno inscrito (clave foránea a usuarios)
 * - `grupoId`: ID del grupo en el que se inscribe (clave foránea)
 * - `fecha`: Fecha de inscripción en formato YYYY-MM-DD
 * - `semestre`: Semestre académico. Valores válidos: 1 o 2
 * - `gestion`: Año de gestión académica (ej: 2025)
 * - `grupo`: Nombre/paralelo del grupo (calculado desde el grupo, para facilitar consultas)
 * - `materiaNombre`: Nombre de la materia (calculado desde el grupo, para facilitar consultas)
 * - `dia`: Días de clase del grupo (calculado desde los horarios, para facilitar consultas)
 * - `horario`: Horarios de clase del grupo (calculado desde los horarios, para facilitar consultas)
 * 
 * Ejemplo de uso:
 * ```kotlin
 * val boleta = Boleta(
 *     id = 1,
 *     alumnoId = 1,
 *     grupoId = 1,
 *     fecha = "2025-01-15",
 *     semestre = 1,
 *     gestion = 2025,
 *     grupo = "A",
 *     materiaNombre = "Programación I",
 *     dia = "Lunes, Miércoles",
 *     horario = "08:00-10:00"
 * )
 * ```
 * 
 * Relaciones:
 * - Una Boleta pertenece a un Alumno (alumnoId)
 * - Una Boleta pertenece a un Grupo (grupoId)
 * - Un Alumno puede tener múltiples Boletas (inscrito en varios grupos)
 * - Una Boleta puede tener múltiples Asistencias asociadas
 * 
 * Validaciones de negocio:
 * - Un alumno no puede inscribirse en grupos con cruce de horarios
 * - El formato de fecha debe ser YYYY-MM-DD
 * - El semestre debe ser 1 o 2
 * - El año de gestión debe ser válido
 * - No se puede inscribir si el grupo está lleno (capacidad alcanzada)
 */
data class Boleta(
    val id: Int,
    val alumnoId: Int,
    val grupoId: Int,
    val fecha: String,
    val semestre: Int,
    val gestion: Int,
    val grupo: String,
    val materiaNombre: String,
    val dia: String,
    val horario: String
)
