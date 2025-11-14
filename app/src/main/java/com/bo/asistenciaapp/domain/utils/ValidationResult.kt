package com.bo.asistenciaapp.domain.utils

/**
 * Resultado de una validación.
 * 
 * Puede ser Success (válido) o Error (inválido con mensaje).
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class SuccessWithData<T>(val data: T) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isValid: Boolean
        get() = this is Success || this is SuccessWithData<*>
    
    val errorMessage: String?
        get() = (this as? Error)?.message
}

/**
 * Valida múltiples condiciones y retorna el primer error encontrado.
 */
fun validate(vararg validations: ValidationResult): ValidationResult {
    return validations.firstOrNull { !it.isValid } ?: ValidationResult.Success
}

