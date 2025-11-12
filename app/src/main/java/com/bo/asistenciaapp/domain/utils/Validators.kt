package com.bo.asistenciaapp.domain.utils

/**
 * Utilidades de validación para la capa de dominio.
 * 
 * Contiene métodos estáticos para validar datos de entrada
 * antes de procesarlos en los casos de uso.
 */
object Validators {
    
    /**
     * Valida que un string no esté vacío o en blanco.
     */
    fun isNotEmpty(value: String?): Boolean {
        return !value.isNullOrBlank()
    }
    
    /**
     * Valida que un string tenga una longitud mínima.
     */
    fun hasMinLength(value: String?, minLength: Int): Boolean {
        return !value.isNullOrBlank() && value.length >= minLength
    }
    
    /**
     * Valida que un string tenga una longitud máxima.
     */
    fun hasMaxLength(value: String?, maxLength: Int): Boolean {
        return value != null && value.length <= maxLength
    }
    
    /**
     * Valida que un string tenga una longitud entre min y max.
     */
    fun hasLengthBetween(value: String?, minLength: Int, maxLength: Int): Boolean {
        return !value.isNullOrBlank() && value.length in minLength..maxLength
    }
    
    /**
     * Valida que un número sea positivo.
     */
    fun isPositive(value: Int?): Boolean {
        return value != null && value > 0
    }
    
    /**
     * Valida que un número esté en un rango específico.
     */
    fun isInRange(value: Int?, min: Int, max: Int): Boolean {
        return value != null && value in min..max
    }
    
    /**
     * Valida formato de email (básico).
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return emailRegex.toRegex().matches(email)
    }
    
    /**
     * Valida que un username solo contenga caracteres alfanuméricos y guiones bajos.
     */
    fun isValidUsername(username: String?): Boolean {
        if (username.isNullOrBlank()) return false
        val usernameRegex = "^[a-zA-Z0-9_]+$"
        return usernameRegex.toRegex().matches(username)
    }
    
    /**
     * Valida formato de fecha (YYYY-MM-DD).
     */
    fun isValidDateFormat(date: String?): Boolean {
        if (date.isNullOrBlank()) return false
        val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$"
        return dateRegex.toRegex().matches(date)
    }
    
    /**
     * Valida formato de hora (HH:mm).
     */
    fun isValidTimeFormat(time: String?): Boolean {
        if (time.isNullOrBlank()) return false
        val timeRegex = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$"
        return timeRegex.toRegex().matches(time)
    }
    
    /**
     * Valida que una hora de inicio sea anterior a la hora de fin.
     */
    fun isValidTimeRange(startTime: String?, endTime: String?): Boolean {
        if (startTime.isNullOrBlank() || endTime.isNullOrBlank()) return false
        if (!isValidTimeFormat(startTime) || !isValidTimeFormat(endTime)) return false
        
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        
        val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
        val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
        
        return startMinutes < endMinutes
    }
    
    /**
     * Valida que un rol sea válido.
     */
    fun isValidRol(rol: String?): Boolean {
        return rol != null && rol in listOf("Admin", "Docente", "Alumno")
    }
    
    /**
     * Valida que un semestre sea válido (1 o 2).
     */
    fun isValidSemestre(semestre: Int?): Boolean {
        return semestre != null && semestre in 1..2
    }
    
    /**
     * Valida que un nivel académico sea válido (1-10).
     */
    fun isValidNivel(nivel: Int?): Boolean {
        return nivel != null && nivel in 1..10
    }
    
    /**
     * Valida que un año de gestión sea válido (año actual o futuro).
     */
    fun isValidGestion(gestion: Int?): Boolean {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return gestion != null && gestion >= currentYear - 5 && gestion <= currentYear + 5
    }
    
    /**
     * Valida que un día de la semana sea válido.
     */
    fun isValidDiaSemana(dia: String?): Boolean {
        val diasValidos = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        return dia != null && dia in diasValidos
    }
}

