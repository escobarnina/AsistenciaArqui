package com.bo.asistenciaapp.domain.model

/**
 * Modelo de dominio que representa un horario de clase.
 * 
 * Este modelo representa la entidad Horario en la capa de dominio.
 * Corresponde a la tabla `horarios` en la base de datos SQLite.
 * 
 * Un horario define cuándo y dónde se imparte una clase de un grupo específico.
 * Un grupo puede tener múltiples horarios (ej: Lunes y Miércoles).
 * 
 * Propiedades:
 * - `id`: Identificador único del horario (clave primaria)
 * - `grupoId`: ID del grupo asociado (clave foránea)
 * - `dia`: Día de la semana. Valores válidos: "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
 * - `horaInicio`: Hora de inicio de la clase en formato HH:mm (ej: "08:00")
 * - `horaFin`: Hora de fin de la clase en formato HH:mm (ej: "10:00")
 * - `materia`: Nombre de la materia (calculado desde el grupo, para facilitar consultas)
 * - `grupo`: Nombre/paralelo del grupo (calculado desde el grupo, para facilitar consultas)
 * 
 * Ejemplo de uso:
 * ```kotlin
 * val horario = Horario(
 *     id = 1,
 *     grupoId = 1,
 *     dia = "Lunes",
 *     horaInicio = "08:00",
 *     horaFin = "10:00",
 *     materia = "Programación I",
 *     grupo = "A"
 * )
 * ```
 * 
 * Relaciones:
 * - Un Horario pertenece a un Grupo (grupoId)
 * - Los Horarios se usan para validar cruces de horarios en Inscripciones
 * - Los Horarios se usan para validar el momento correcto de marcar Asistencias
 * 
 * Validaciones de negocio:
 * - El día debe ser un día de la semana válido
 * - El formato de hora debe ser HH:mm (24 horas)
 * - La hora de inicio debe ser anterior a la hora de fin
 * - No puede haber horarios solapados para el mismo grupo
 */
data class Horario(
    val id: Int,
    val grupoId: Int,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val materia: String,
    val grupo: String
)
