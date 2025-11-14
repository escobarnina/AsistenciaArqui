package com.bo.asistenciaapp.domain.model

/**
 * Modelo de dominio que representa un registro de asistencia.
 * 
 * Este modelo representa la entidad Asistencia en la capa de dominio.
 * Corresponde a la tabla `asistencias` en la base de datos SQLite.
 * 
 * Una asistencia representa el registro de presencia de un alumno
 * en una clase específica de un grupo en una fecha determinada.
 * 
 * Propiedades:
 * - `id`: Identificador único de la asistencia (clave primaria)
 * - `alumnoId`: ID del alumno que marca asistencia (clave foránea a usuarios)
 * - `grupoId`: ID del grupo al que pertenece la clase (clave foránea)
 * - `fecha`: Fecha de la asistencia en formato YYYY-MM-DD
 * - `grupo`: Nombre/paralelo del grupo (calculado desde el grupo, para facilitar consultas)
 * - `materiaNombre`: Nombre de la materia (calculado desde el grupo, para facilitar consultas)
 * 
 * Ejemplo de uso:
 * ```kotlin
 * val asistencia = Asistencia(
 *     id = 1,
 *     alumnoId = 1,
 *     grupoId = 1,
 *     fecha = "2025-01-20",
 *     grupo = "A",
 *     materiaNombre = "Programación I"
 * )
 * ```
 * 
 * Relaciones:
 * - Una Asistencia pertenece a un Alumno (alumnoId)
 * - Una Asistencia pertenece a un Grupo (grupoId)
 * - Un Alumno puede tener múltiples Asistencias (en diferentes grupos y fechas)
 * - Una Asistencia está relacionada con una Boleta/Inscripción (indirectamente)
 * 
 * Validaciones de negocio:
 * - El alumno debe estar inscrito en el grupo (tener una Boleta activa)
 * - La fecha debe corresponder al día de clase según el Horario del grupo
 * - La hora de marcado debe estar dentro del rango permitido según el Horario
 * - No se puede marcar asistencia dos veces en la misma fecha para el mismo grupo
 * - El formato de fecha debe ser YYYY-MM-DD
 */
data class Asistencia(
    val id: Int,
    val alumnoId: Int,
    val grupoId: Int,
    val fecha: String,
    val grupo: String,
    val materiaNombre: String,
    val horaMarcada: String? = null,
    val estado: String? = null  // PRESENTE, RETRASO, FALTA
)
