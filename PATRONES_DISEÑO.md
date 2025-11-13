# Guía de Patrones de Diseño - AsistenciaApp

## 📚 Índice
1. [Introducción](#introducción)
2. [Patrón Adapter](#patrón-adapter)
3. [Patrón Strategy](#patrón-strategy)
4. [Casos de Uso Implementados](#casos-de-uso-implementados)
5. [Ejemplos de Código](#ejemplos-de-código)
6. [Diagramas UML](#diagramas-uml)
7. [Guía de Implementación](#guía-de-implementación)

---

## 📖 Introducción

Este documento describe la aplicación de los patrones de diseño **Adapter** y **Strategy** en el proyecto AsistenciaApp. Estos patrones mejoran la mantenibilidad, escalabilidad y flexibilidad del código.

### ¿Por qué estos patrones?

- **Adapter**: Permite integrar sistemas externos (API REST, Room, exportación de datos) sin modificar el código existente.
- **Strategy**: Facilita la implementación de diferentes algoritmos (validaciones, cálculos, ordenamiento) de forma intercambiable.

---

## 🔌 Patrón Adapter

### Definición
El patrón **Adapter** (también conocido como Wrapper) permite que interfaces incompatibles trabajen juntas. Actúa como un puente entre dos interfaces diferentes.

### Propósito
- Convertir la interfaz de una clase en otra interfaz que los clientes esperan
- Permitir que clases con interfaces incompatibles trabajen juntas
- Reutilizar código existente sin modificarlo

### Estructura General
```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Cliente   │────────>│   Adapter   │────────>│  Adaptee    │
│             │         │ (Interface) │         │  (Sistema   │
│             │         │             │         │   Externo)  │
└─────────────┘         └─────────────┘         └─────────────┘
```

### Casos de Uso en AsistenciaApp

#### 1. Adapter para Base de Datos (SQLite ↔ Room)

**Problema**: 
- Actualmente usamos `SQLiteOpenHelper`
- En el futuro queremos migrar a Room
- No queremos reescribir todo el código

**Solución**:
```kotlin
// 📁 data/local/adapter/DatabaseAdapter.kt
interface DatabaseAdapter {
    fun getUsuarioDao(): UsuarioDaoInterface
    fun getMateriaDao(): MateriaDaoInterface
    fun getGrupoDao(): GrupoDaoInterface
    fun getHorarioDao(): HorarioDaoInterface
    fun getInscripcionDao(): InscripcionDaoInterface
    fun getAsistenciaDao(): AsistenciaDaoInterface
}

// 📁 data/local/adapter/SQLiteDatabaseAdapter.kt
class SQLiteDatabaseAdapter(
    private val db: AppDatabase
) : DatabaseAdapter {
    
    override fun getUsuarioDao(): UsuarioDaoInterface {
        return UsuarioDaoSQLiteAdapter(db.usuarioDao)
    }
    
    override fun getMateriaDao(): MateriaDaoInterface {
        return MateriaDaoSQLiteAdapter(db.materiaDao)
    }
    
    // ... otros DAOs
}

// 📁 data/local/adapter/RoomDatabaseAdapter.kt
class RoomDatabaseAdapter(
    private val db: RoomAppDatabase
) : DatabaseAdapter {
    
    override fun getUsuarioDao(): UsuarioDaoInterface {
        return UsuarioDaoRoomAdapter(db.usuarioDao())
    }
    
    override fun getMateriaDao(): MateriaDaoInterface {
        return MateriaDaoRoomAdapter(db.materiaDao())
    }
    
    // ... otros DAOs
}
```

**Beneficios**:
- ✅ Migración gradual de SQLite a Room
- ✅ No se modifica la capa de repositorios
- ✅ Fácil de testear con mocks
- ✅ Cambio de implementación transparente

---

#### 2. Adapter para API REST

**Problema**:
- Los datos del servidor tienen estructura diferente a nuestros modelos
- Necesitamos convertir entre DTOs (Data Transfer Objects) y modelos de dominio

**Solución**:
```kotlin
// 📁 data/remote/dto/UsuarioDTO.kt
data class UsuarioDTO(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val studentId: String,
    val role: String,          // "ADMINISTRATOR", "TEACHER", "STUDENT"
    val userName: String
)

// 📁 data/remote/adapter/UsuarioApiAdapter.kt
class UsuarioApiAdapter {
    
    /**
     * Convierte un DTO de la API a un modelo de dominio
     */
    fun toUsuario(dto: UsuarioDTO): Usuario {
        return Usuario(
            id = dto.userId,
            nombres = dto.firstName,
            apellidos = dto.lastName,
            registro = dto.studentId,
            rol = mapRolFromApi(dto.role),
            username = dto.userName
        )
    }
    
    /**
     * Convierte un modelo de dominio a un DTO para la API
     */
    fun toUsuarioDTO(usuario: Usuario): UsuarioDTO {
        return UsuarioDTO(
            userId = usuario.id,
            firstName = usuario.nombres,
            lastName = usuario.apellidos,
            studentId = usuario.registro,
            role = mapRolToApi(usuario.rol),
            userName = usuario.username
        )
    }
    
    /**
     * Mapea roles de la API a roles internos
     */
    private fun mapRolFromApi(apiRole: String): String {
        return when(apiRole.uppercase()) {
            "ADMINISTRATOR" -> "Admin"
            "TEACHER" -> "Docente"
            "STUDENT" -> "Alumno"
            else -> "Alumno"
        }
    }
    
    /**
     * Mapea roles internos a roles de la API
     */
    private fun mapRolToApi(rol: String): String {
        return when(rol) {
            "Admin" -> "ADMINISTRATOR"
            "Docente" -> "TEACHER"
            "Alumno" -> "STUDENT"
            else -> "STUDENT"
        }
    }
}

// 📁 data/remote/adapter/MateriaApiAdapter.kt
class MateriaApiAdapter {
    
    fun toMateria(dto: MateriaDTO): Materia {
        return Materia(
            id = dto.subjectId,
            nombre = dto.name,
            sigla = dto.code,
            nivel = dto.level
        )
    }
    
    fun toMateriaDTO(materia: Materia): MateriaDTO {
        return MateriaDTO(
            subjectId = materia.id,
            name = materia.nombre,
            code = materia.sigla,
            level = materia.nivel
        )
    }
}
```

**Uso en el Repository**:
```kotlin
// 📁 data/repository/UsuarioRepository.kt
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val apiService: ApiService? = null,
    private val apiAdapter: UsuarioApiAdapter = UsuarioApiAdapter()
) {
    
    /**
     * Obtiene usuarios desde la API y los guarda localmente
     */
    suspend fun sincronizarUsuarios() {
        apiService?.let { api ->
            try {
                // Obtener datos de la API
                val dtos = api.getUsuarios()
                
                // Convertir DTOs a modelos de dominio usando el Adapter
                val usuarios = dtos.map { dto -> apiAdapter.toUsuario(dto) }
                
                // Guardar en la base de datos local
                usuarios.forEach { usuario ->
                    usuarioDao.agregar(
                        usuario.nombres,
                        usuario.apellidos,
                        usuario.registro,
                        usuario.rol,
                        usuario.username,
                        "1234" // contraseña por defecto
                    )
                }
                
                Log.d("UsuarioRepository", "Sincronización exitosa: ${usuarios.size} usuarios")
            } catch (e: Exception) {
                Log.e("UsuarioRepository", "Error en sincronización: ${e.message}")
            }
        }
    }
}
```

**Beneficios**:
- ✅ Separación clara entre API y dominio
- ✅ Fácil cambio de estructura de API
- ✅ Reutilizable en diferentes contextos
- ✅ Testeable independientemente

---

#### 3. Adapter para Exportación de Datos

**Problema**:
- Necesitamos exportar asistencias en diferentes formatos (Excel, PDF, CSV)
- Cada formato tiene su propia lógica de generación

**Solución**:
```kotlin
// 📁 data/export/adapter/DataExportAdapter.kt
interface DataExportAdapter<T> {
    fun export(data: List<T>): ByteArray
    fun getFileExtension(): String
    fun getMimeType(): String
}

// 📁 data/export/adapter/AsistenciaCSVAdapter.kt
class AsistenciaCSVAdapter : DataExportAdapter<Asistencia> {
    
    override fun export(data: List<Asistencia>): ByteArray {
        val csv = StringBuilder()
        
        // Encabezados
        csv.append("ID,ID_Alumno,ID_Grupo,Fecha\n")
        
        // Datos
        data.forEach { asistencia ->
            csv.append("${asistencia.id},")
            csv.append("${asistencia.idAlumno},")
            csv.append("${asistencia.idGrupo},")
            csv.append("${asistencia.fecha}\n")
        }
        
        return csv.toString().toByteArray(Charsets.UTF_8)
    }
    
    override fun getFileExtension(): String = "csv"
    
    override fun getMimeType(): String = "text/csv"
}

// 📁 data/export/adapter/AsistenciaExcelAdapter.kt
class AsistenciaExcelAdapter : DataExportAdapter<Asistencia> {
    
    override fun export(data: List<Asistencia>): ByteArray {
        // Usar librería Apache POI para generar Excel
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Asistencias")
        
        // Crear encabezados
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("ID Alumno")
        headerRow.createCell(2).setCellValue("ID Grupo")
        headerRow.createCell(3).setCellValue("Fecha")
        
        // Agregar datos
        data.forEachIndexed { index, asistencia ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(asistencia.id.toDouble())
            row.createCell(1).setCellValue(asistencia.idAlumno.toDouble())
            row.createCell(2).setCellValue(asistencia.idGrupo.toDouble())
            row.createCell(3).setCellValue(asistencia.fecha)
        }
        
        // Convertir a ByteArray
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()
        
        return outputStream.toByteArray()
    }
    
    override fun getFileExtension(): String = "xlsx"
    
    override fun getMimeType(): String = 
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
}

// 📁 data/export/adapter/AsistenciaPDFAdapter.kt
class AsistenciaPDFAdapter : DataExportAdapter<Asistencia> {
    
    override fun export(data: List<Asistencia>): ByteArray {
        // Usar librería iText o similar para generar PDF
        val outputStream = ByteArrayOutputStream()
        val document = Document()
        val writer = PdfWriter.getInstance(document, outputStream)
        
        document.open()
        document.add(Paragraph("Reporte de Asistencias"))
        
        // Crear tabla
        val table = PdfPTable(4)
        table.addCell("ID")
        table.addCell("ID Alumno")
        table.addCell("ID Grupo")
        table.addCell("Fecha")
        
        data.forEach { asistencia ->
            table.addCell(asistencia.id.toString())
            table.addCell(asistencia.idAlumno.toString())
            table.addCell(asistencia.idGrupo.toString())
            table.addCell(asistencia.fecha)
        }
        
        document.add(table)
        document.close()
        
        return outputStream.toByteArray()
    }
    
    override fun getFileExtension(): String = "pdf"
    
    override fun getMimeType(): String = "application/pdf"
}
```

**Uso en un UseCase**:
```kotlin
// 📁 domain/usecase/ExportarAsistenciaCU.kt
class ExportarAsistenciaCU(
    private val asistenciaRepository: AsistenciaRepository
) {
    
    /**
     * Exporta asistencias usando el adapter especificado
     */
    fun exportar(
        idGrupo: Int,
        adapter: DataExportAdapter<Asistencia>
    ): ExportResult {
        return try {
            // Obtener datos
            val asistencias = asistenciaRepository.obtenerPorGrupo(idGrupo)
            
            if (asistencias.isEmpty()) {
                return ExportResult.Error("No hay asistencias para exportar")
            }
            
            // Exportar usando el adapter
            val bytes = adapter.export(asistencias)
            val extension = adapter.getFileExtension()
            val mimeType = adapter.getMimeType()
            
            ExportResult.Success(bytes, extension, mimeType)
            
        } catch (e: Exception) {
            ExportResult.Error("Error al exportar: ${e.message}")
        }
    }
}

sealed class ExportResult {
    data class Success(
        val data: ByteArray,
        val extension: String,
        val mimeType: String
    ) : ExportResult()
    
    data class Error(val message: String) : ExportResult()
}
```

**Uso en la UI**:
```kotlin
// En DocenteHomeScreen o AdminHome
@Composable
fun ExportarAsistenciasButton(idGrupo: Int, exportarCU: ExportarAsistenciaCU) {
    var showDialog by remember { mutableStateOf(false) }
    
    Button(onClick = { showDialog = true }) {
        Text("Exportar Asistencias")
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Seleccionar formato") },
            text = {
                Column {
                    Button(onClick = {
                        exportarAsistencias(idGrupo, AsistenciaCSVAdapter(), exportarCU)
                        showDialog = false
                    }) {
                        Text("Exportar a CSV")
                    }
                    
                    Button(onClick = {
                        exportarAsistencias(idGrupo, AsistenciaExcelAdapter(), exportarCU)
                        showDialog = false
                    }) {
                        Text("Exportar a Excel")
                    }
                    
                    Button(onClick = {
                        exportarAsistencias(idGrupo, AsistenciaPDFAdapter(), exportarCU)
                        showDialog = false
                    }) {
                        Text("Exportar a PDF")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

fun exportarAsistencias(
    idGrupo: Int,
    adapter: DataExportAdapter<Asistencia>,
    exportarCU: ExportarAsistenciaCU
) {
    when (val result = exportarCU.exportar(idGrupo, adapter)) {
        is ExportResult.Success -> {
            // Guardar archivo
            guardarArchivo(result.data, "asistencias_grupo_$idGrupo", result.extension)
        }
        is ExportResult.Error -> {
            // Mostrar error
            Log.e("Export", result.message)
        }
    }
}
```

**Beneficios**:
- ✅ Fácil agregar nuevos formatos de exportación
- ✅ Separación de responsabilidades
- ✅ Reutilizable para diferentes tipos de datos
- ✅ Testeable independientemente

---

## 🎯 Patrón Strategy

### Definición
El patrón **Strategy** define una familia de algoritmos, encapsula cada uno y los hace intercambiables. Permite que el algoritmo varíe independientemente de los clientes que lo usan.

### Propósito
- Definir una familia de algoritmos relacionados
- Encapsular cada algoritmo en una clase separada
- Hacer que los algoritmos sean intercambiables en tiempo de ejecución

### Estructura General
```
┌─────────────┐         ┌─────────────────┐
│   Context   │────────>│    Strategy     │
│             │         │   (Interface)   │
└─────────────┘         └─────────────────┘
                                 △
                                 │
                    ┌────────────┼────────────┐
                    │            │            │
            ┌───────▼──────┐ ┌──▼─────────┐ ┌▼────────────┐
            │ StrategyA    │ │ StrategyB  │ │ StrategyC   │
            └──────────────┘ └────────────┘ └─────────────┘
```

### Casos de Uso en AsistenciaApp

#### 1. Strategy para Validación de Usuarios por Rol

**Problema**:
- Diferentes tipos de usuarios requieren validaciones diferentes
- Admin: contraseña más fuerte (8+ caracteres)
- Docente: registro con formato específico (5-6 dígitos)
- Alumno: registro con formato específico (6 dígitos)

**Solución**:
```kotlin
// 📁 domain/strategy/validation/ValidationStrategy.kt
interface ValidationStrategy {
    fun validate(
        nombres: String,
        apellidos: String,
        registro: String,
        username: String,
        contrasena: String
    ): ValidationResult
    
    fun getRoleName(): String
}

// 📁 domain/strategy/validation/AdminValidationStrategy.kt
class AdminValidationStrategy : ValidationStrategy {
    
    override fun validate(
        nombres: String,
        apellidos: String,
        registro: String,
        username: String,
        contrasena: String
    ): ValidationResult {
        // Validaciones comunes
        val commonValidation = validateCommon(nombres, apellidos, username)
        if (!commonValidation.isValid) {
            return commonValidation
        }
        
        // Validaciones específicas de Admin
        if (!Validators.hasMinLength(contrasena, 8)) {
            return ValidationResult.Error(
                "Los administradores requieren contraseña de al menos 8 caracteres"
            )
        }
        
        if (!contrasena.any { it.isUpperCase() }) {
            return ValidationResult.Error(
                "La contraseña debe contener al menos una mayúscula"
            )
        }
        
        if (!contrasena.any { it.isDigit() }) {
            return ValidationResult.Error(
                "La contraseña debe contener al menos un número"
            )
        }
        
        // Formato de registro para Admin (5 dígitos)
        if (!registro.matches(Regex("^[0-9]{5}$"))) {
            return ValidationResult.Error(
                "El registro de administrador debe tener exactamente 5 dígitos"
            )
        }
        
        return ValidationResult.Success
    }
    
    override fun getRoleName(): String = "Admin"
    
    private fun validateCommon(
        nombres: String,
        apellidos: String,
        username: String
    ): ValidationResult {
        if (!Validators.isNotEmpty(nombres)) {
            return ValidationResult.Error("Los nombres son requeridos")
        }
        
        if (!Validators.isNotEmpty(apellidos)) {
            return ValidationResult.Error("Los apellidos son requeridos")
        }
        
        if (!Validators.isValidUsername(username)) {
            return ValidationResult.Error(
                "El username solo puede contener letras, números y guiones bajos"
            )
        }
        
        if (!Validators.hasMinLength(username, 3)) {
            return ValidationResult.Error(
                "El username debe tener al menos 3 caracteres"
            )
        }
        
        if (!Validators.hasMaxLength(username, 20)) {
            return ValidationResult.Error(
                "El username no puede tener más de 20 caracteres"
            )
        }
        
        return ValidationResult.Success
    }
}

// 📁 domain/strategy/validation/DocenteValidationStrategy.kt
class DocenteValidationStrategy : ValidationStrategy {
    
    override fun validate(
        nombres: String,
        apellidos: String,
        registro: String,
        username: String,
        contrasena: String
    ): ValidationResult {
        // Validaciones comunes (se puede extraer a una clase base)
        if (!Validators.isNotEmpty(nombres)) {
            return ValidationResult.Error("Los nombres son requeridos")
        }
        
        if (!Validators.isNotEmpty(apellidos)) {
            return ValidationResult.Error("Los apellidos son requeridos")
        }
        
        if (!Validators.isValidUsername(username)) {
            return ValidationResult.Error(
                "El username solo puede contener letras, números y guiones bajos"
            )
        }
        
        if (!Validators.hasLengthBetween(username, 3, 20)) {
            return ValidationResult.Error(
                "El username debe tener entre 3 y 20 caracteres"
            )
        }
        
        // Validaciones específicas de Docente
        if (!Validators.hasMinLength(contrasena, 6)) {
            return ValidationResult.Error(
                "Los docentes requieren contraseña de al menos 6 caracteres"
            )
        }
        
        // Formato de registro para Docente (5-6 dígitos)
        if (!registro.matches(Regex("^[0-9]{5,6}$"))) {
            return ValidationResult.Error(
                "El registro de docente debe tener entre 5 y 6 dígitos"
            )
        }
        
        return ValidationResult.Success
    }
    
    override fun getRoleName(): String = "Docente"
}

// 📁 domain/strategy/validation/AlumnoValidationStrategy.kt
class AlumnoValidationStrategy : ValidationStrategy {
    
    override fun validate(
        nombres: String,
        apellidos: String,
        registro: String,
        username: String,
        contrasena: String
    ): ValidationResult {
        // Validaciones comunes
        if (!Validators.isNotEmpty(nombres)) {
            return ValidationResult.Error("Los nombres son requeridos")
        }
        
        if (!Validators.isNotEmpty(apellidos)) {
            return ValidationResult.Error("Los apellidos son requeridos")
        }
        
        if (!Validators.isValidUsername(username)) {
            return ValidationResult.Error(
                "El username solo puede contener letras, números y guiones bajos"
            )
        }
        
        if (!Validators.hasLengthBetween(username, 3, 20)) {
            return ValidationResult.Error(
                "El username debe tener entre 3 y 20 caracteres"
            )
        }
        
        // Validaciones específicas de Alumno
        if (!Validators.hasMinLength(contrasena, 4)) {
            return ValidationResult.Error(
                "La contraseña debe tener al menos 4 caracteres"
            )
        }
        
        // Formato de registro para Alumno (exactamente 6 dígitos)
        if (!registro.matches(Regex("^[0-9]{6}$"))) {
            return ValidationResult.Error(
                "El registro de alumno debe tener exactamente 6 dígitos"
            )
        }
        
        return ValidationResult.Success
    }
    
    override fun getRoleName(): String = "Alumno"
}
```

**Uso en UsuarioCU**:
```kotlin
// 📁 domain/usecase/UsuarioCU.kt
class UsuarioCU(private val usuarioRepository: UsuarioRepository) {
    
    // Mapa de strategies por rol
    private val validationStrategies = mapOf(
        "Admin" to AdminValidationStrategy(),
        "Docente" to DocenteValidationStrategy(),
        "Alumno" to AlumnoValidationStrategy()
    )
    
    /**
     * Agrega un nuevo usuario al sistema.
     * Usa la strategy de validación correspondiente al rol.
     */
    fun agregarUsuario(
        nombres: String,
        apellidos: String,
        registro: String,
        rol: String,
        username: String,
        contrasena: String
    ): ValidationResult {
        // Seleccionar la strategy apropiada según el rol
        val strategy = validationStrategies[rol]
        
        if (strategy == null) {
            return ValidationResult.Error(
                "Rol inválido. Debe ser: Admin, Docente o Alumno"
            )
        }
        
        // Ejecutar validación usando la strategy
        val validation = strategy.validate(
            nombres, apellidos, registro, username, contrasena
        )
        
        if (!validation.isValid) {
            return validation
        }
        
        // Verificar si el username ya existe
        val usuarioExistente = usuarioRepository
            .obtenerTodos()
            .find { it.username == username }
            
        if (usuarioExistente != null) {
            return ValidationResult.Error(
                "El username '$username' ya está en uso"
            )
        }
        
        // Agregar usuario
        usuarioRepository.agregar(
            nombres, apellidos, registro, rol, username, contrasena
        )
        
        return ValidationResult.Success
    }
    
    /**
     * Obtiene la strategy de validación para un rol específico.
     * Útil para mostrar requisitos en la UI.
     */
    fun getValidationRequirements(rol: String): String {
        return when(rol) {
            "Admin" -> """
                • Contraseña: mínimo 8 caracteres, con mayúscula y número
                • Registro: exactamente 5 dígitos
                • Username: 3-20 caracteres alfanuméricos
            """.trimIndent()
            
            "Docente" -> """
                • Contraseña: mínimo 6 caracteres
                • Registro: 5-6 dígitos
                • Username: 3-20 caracteres alfanuméricos
            """.trimIndent()
            
            "Alumno" -> """
                • Contraseña: mínimo 4 caracteres
                • Registro: exactamente 6 dígitos
                • Username: 3-20 caracteres alfanuméricos
            """.trimIndent()
            
            else -> "Rol no válido"
        }
    }
}
```

**Beneficios**:
- ✅ Validaciones específicas por rol sin if/else anidados
- ✅ Fácil agregar nuevos roles con sus propias validaciones
- ✅ Código más limpio y mantenible
- ✅ Cada strategy es testeable independientemente

---

#### 2. Strategy para Cálculo de Asistencias

**Problema**:
- Diferentes políticas de asistencia según el semestre o materia
- Algunas materias requieren 80% de asistencia
- Otras pueden ser más flexibles (70%)

**Solución**:
```kotlin
// 📁 domain/strategy/attendance/AttendanceCalculationStrategy.kt
interface AttendanceCalculationStrategy {
    /**
     * Calcula el porcentaje de asistencia
     */
    fun calculatePercentage(asistencias: Int, totalClases: Int): Double
    
    /**
     * Determina el estado académico según el porcentaje
     */
    fun getStatus(percentage: Double): AttendanceStatus
    
    /**
     * Obtiene el porcentaje mínimo requerido
     */
    fun getMinimumRequired(): Double
    
    /**
     * Obtiene el nombre de la estrategia
     */
    fun getStrategyName(): String
}

// 📁 domain/model/AttendanceStatus.kt
enum class AttendanceStatus(val displayName: String, val color: String) {
    APROBADO("Aprobado", "#4CAF50"),      // Verde
    EN_RIESGO("En Riesgo", "#FF9800"),    // Naranja
    REPROBADO("Reprobado", "#F44336")     // Rojo
}

// 📁 domain/strategy/attendance/StrictAttendanceStrategy.kt
class StrictAttendanceStrategy : AttendanceCalculationStrategy {
    
    override fun calculatePercentage(asistencias: Int, totalClases: Int): Double {
        if (totalClases == 0) return 0.0
        return (asistencias.toDouble() / totalClases) * 100
    }
    
    override fun getStatus(percentage: Double): AttendanceStatus {
        return when {
            percentage >= 80.0 -> AttendanceStatus.APROBADO
            percentage >= 60.0 -> AttendanceStatus.EN_RIESGO
            else -> AttendanceStatus.REPROBADO
        }
    }
    
    override fun getMinimumRequired(): Double = 80.0
    
    override fun getStrategyName(): String = "Política Estricta (80%)"
}

// 📁 domain/strategy/attendance/LenientAttendanceStrategy.kt
class LenientAttendanceStrategy : AttendanceCalculationStrategy {
    
    override fun calculatePercentage(asistencias: Int, totalClases: Int): Double {
        if (totalClases == 0) return 0.0
        return (asistencias.toDouble() / totalClases) * 100
    }
    
    override fun getStatus(percentage: Double): AttendanceStatus {
        return when {
            percentage >= 70.0 -> AttendanceStatus.APROBADO
            percentage >= 50.0 -> AttendanceStatus.EN_RIESGO
            else -> AttendanceStatus.REPROBADO
        }
    }
    
    override fun getMinimumRequired(): Double = 70.0
    
    override fun getStrategyName(): String = "Política Flexible (70%)"
}

// 📁 domain/strategy/attendance/ModerateAttendanceStrategy.kt
class ModerateAttendanceStrategy : AttendanceCalculationStrategy {
    
    override fun calculatePercentage(asistencias: Int, totalClases: Int): Double {
        if (totalClases == 0) return 0.0
        return (asistencias.toDouble() / totalClases) * 100
    }
    
    override fun getStatus(percentage: Double): AttendanceStatus {
        return when {
            percentage >= 75.0 -> AttendanceStatus.APROBADO
            percentage >= 55.0 -> AttendanceStatus.EN_RIESGO
            else -> AttendanceStatus.REPROBADO
        }
    }
    
    override fun getMinimumRequired(): Double = 75.0
    
    override fun getStrategyName(): String = "Política Moderada (75%)"
}
```

**Modelo de datos para estadísticas**:
```kotlin
// 📁 domain/model/AsistenciaStats.kt
data class AsistenciaStats(
    val totalAsistencias: Int,
    val totalClases: Int,
    val porcentaje: Double,
    val estado: AttendanceStatus,
    val estrategiaUsada: String,
    val minimoRequerido: Double,
    val asistenciasRestantes: Int  // Cuántas faltan para aprobar
)
```

**Uso en AsistenciaCU**:
```kotlin
// 📁 domain/usecase/AsistenciaCU.kt
class AsistenciaCU(
    private val asistenciaRepository: AsistenciaRepository,
    private val grupoRepository: GrupoRepository,
    private var calculationStrategy: AttendanceCalculationStrategy = StrictAttendanceStrategy()
) {
    
    /**
     * Cambia la estrategia de cálculo de asistencias
     */
    fun setCalculationStrategy(strategy: AttendanceCalculationStrategy) {
        calculationStrategy = strategy
    }
    
    /**
     * Obtiene estadísticas de asistencia para un alumno en un grupo
     */
    fun obtenerEstadisticasAsistencia(
        idAlumno: Int,
        idGrupo: Int,
        totalClasesEsperadas: Int = 40  // Por defecto 40 clases
    ): ValidationResult {
        // Validar IDs
        if (!Validators.isPositive(idAlumno) || !Validators.isPositive(idGrupo)) {
            return ValidationResult.Error("IDs inválidos")
        }
        
        // Obtener asistencias
        val asistencias = asistenciaRepository.obtenerPorAlumnoYGrupo(idAlumno, idGrupo)
        
        // Calcular porcentaje usando la estrategia actual
        val porcentaje = calculationStrategy.calculatePercentage(
            asistencias.size,
            totalClasesEsperadas
        )
        
        // Obtener estado
        val estado = calculationStrategy.getStatus(porcentaje)
        
        // Calcular cuántas asistencias faltan para aprobar
        val minimoRequerido = calculationStrategy.getMinimumRequired()
        val asistenciasNecesarias = ((minimoRequerido / 100) * totalClasesEsperadas).toInt()
        val asistenciasRestantes = maxOf(0, asistenciasNecesarias - asistencias.size)
        
        // Crear objeto de estadísticas
        val stats = AsistenciaStats(
            totalAsistencias = asistencias.size,
            totalClases = totalClasesEsperadas,
            porcentaje = porcentaje,
            estado = estado,
            estrategiaUsada = calculationStrategy.getStrategyName(),
            minimoRequerido = minimoRequerido,
            asistenciasRestantes = asistenciasRestantes
        )
        
        return ValidationResult.SuccessWithData(stats)
    }
    
    /**
     * Obtiene estadísticas para todos los alumnos de un grupo
     */
    fun obtenerEstadisticasPorGrupo(
        idGrupo: Int,
        totalClasesEsperadas: Int = 40
    ): Map<Int, AsistenciaStats> {
        val asistenciasPorAlumno = asistenciaRepository
            .obtenerPorGrupo(idGrupo)
            .groupBy { it.idAlumno }
        
        return asistenciasPorAlumno.mapValues { (idAlumno, asistencias) ->
            val porcentaje = calculationStrategy.calculatePercentage(
                asistencias.size,
                totalClasesEsperadas
            )
            
            val estado = calculationStrategy.getStatus(porcentaje)
            val minimoRequerido = calculationStrategy.getMinimumRequired()
            val asistenciasNecesarias = ((minimoRequerido / 100) * totalClasesEsperadas).toInt()
            val asistenciasRestantes = maxOf(0, asistenciasNecesarias - asistencias.size)
            
            AsistenciaStats(
                totalAsistencias = asistencias.size,
                totalClases = totalClasesEsperadas,
                porcentaje = porcentaje,
                estado = estado,
                estrategiaUsada = calculationStrategy.getStrategyName(),
                minimoRequerido = minimoRequerido,
                asistenciasRestantes = asistenciasRestantes
            )
        }
    }
    
    /**
     * Registra una nueva asistencia
     */
    fun registrarAsistencia(
        idAlumno: Int,
        idGrupo: Int,
        fecha: String
    ): ValidationResult {
        // Validaciones
        if (!Validators.isPositive(idAlumno)) {
            return ValidationResult.Error("ID de alumno inválido")
        }
        
        if (!Validators.isPositive(idGrupo)) {
            return ValidationResult.Error("ID de grupo inválido")
        }
        
        if (!Validators.isValidDateFormat(fecha)) {
            return ValidationResult.Error("Formato de fecha inválido (use YYYY-MM-DD)")
        }
        
        // Verificar si ya existe una asistencia para esta fecha
        val asistenciaExistente = asistenciaRepository
            .obtenerPorAlumnoYGrupo(idAlumno, idGrupo)
            .find { it.fecha == fecha }
        
        if (asistenciaExistente != null) {
            return ValidationResult.Error(
                "Ya existe una asistencia registrada para esta fecha"
            )
        }
        
        // Registrar asistencia
        asistenciaRepository.agregar(idAlumno, idGrupo, fecha)
        
        return ValidationResult.Success
    }
}
```

**Extensión de ValidationResult para incluir datos**:
```kotlin
// 📁 domain/utils/ValidationResult.kt (actualización)
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    data class SuccessWithData<T>(val data: T) : ValidationResult()
    
    val isValid: Boolean
        get() = this is Success || this is SuccessWithData<*>
}
```

**Uso en la UI (DocenteHomeScreen)**:
```kotlin
// En VerEstudiantesGrupoScreen.kt
@Composable
fun EstadisticasAsistenciaSection(
    idGrupo: Int,
    asistenciaCU: AsistenciaCU
) {
    var selectedStrategy by remember { mutableStateOf<AttendanceCalculationStrategy>(
        StrictAttendanceStrategy()
    )}
    
    val strategies = remember {
        listOf(
            StrictAttendanceStrategy(),
            ModerateAttendanceStrategy(),
            LenientAttendanceStrategy()
        )
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        // Selector de estrategia
        Text(
            text = "Política de Asistencia",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        strategies.forEach { strategy ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedStrategy::class == strategy::class,
                    onClick = {
                        selectedStrategy = strategy
                        asistenciaCU.setCalculationStrategy(strategy)
                    }
                )
                Text(
                    text = strategy.getStrategyName(),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mostrar estadísticas
        val estadisticas = remember(selectedStrategy) {
            asistenciaCU.obtenerEstadisticasPorGrupo(idGrupo)
        }
        
        Text(
            text = "Resumen del Grupo",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        estadisticas.forEach { (idAlumno, stats) ->
            EstadisticaCard(stats, idAlumno)
        }
    }
}

@Composable
fun EstadisticaCard(stats: AsistenciaStats, idAlumno: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when(stats.estado) {
                AttendanceStatus.APROBADO -> Color(0xFFE8F5E9)
                AttendanceStatus.EN_RIESGO -> Color(0xFFFFF3E0)
                AttendanceStatus.REPROBADO -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Alumno ID: $idAlumno",
                style = MaterialTheme.typography.titleSmall
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Asistencias: ${stats.totalAsistencias}/${stats.totalClases}")
                Text("${String.format("%.1f", stats.porcentaje)}%")
            }
            
            Text(
                text = "Estado: ${stats.estado.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(android.graphics.Color.parseColor(stats.estado.color))
            )
            
            if (stats.asistenciasRestantes > 0) {
                Text(
                    text = "Faltan ${stats.asistenciasRestantes} asistencias para aprobar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
```

**Beneficios**:
- ✅ Cambio fácil de políticas de asistencia
- ✅ Diferentes estrategias para diferentes materias
- ✅ Código más limpio sin if/else anidados
- ✅ Fácil agregar nuevas políticas

---

#### 3. Strategy para Ordenamiento de Listas

**Problema**:
- Los usuarios quieren ordenar listas de diferentes maneras
- Ordenar por nombre, registro, rol, fecha, etc.

**Solución**:
```kotlin
// 📁 domain/strategy/sorting/SortingStrategy.kt
interface SortingStrategy<T> {
    fun sort(items: List<T>): List<T>
    fun getSortingName(): String
}

// 📁 domain/strategy/sorting/UsuarioSortingStrategies.kt

class SortUsuariosByNombreStrategy : SortingStrategy<Usuario> {
    override fun sort(items: List<Usuario>): List<Usuario> {
        return items.sortedBy { it.nombres.lowercase() }
    }
    
    override fun getSortingName(): String = "Nombre (A-Z)"
}

class SortUsuariosByNombreDescStrategy : SortingStrategy<Usuario> {
    override fun sort(items: List<Usuario>): List<Usuario> {
        return items.sortedByDescending { it.nombres.lowercase() }
    }
    
    override fun getSortingName(): String = "Nombre (Z-A)"
}

class SortUsuariosByApellidoStrategy : SortingStrategy<Usuario> {
    override fun sort(items: List<Usuario>): List<Usuario> {
        return items.sortedBy { it.apellidos.lowercase() }
    }
    
    override fun getSortingName(): String = "Apellido (A-Z)"
}

class SortUsuariosByRegistroStrategy : SortingStrategy<Usuario> {
    override fun sort(items: List<Usuario>): List<Usuario> {
        return items.sortedBy { it.registro }
    }
    
    override fun getSortingName(): String = "Registro"
}

class SortUsuariosByRolStrategy : SortingStrategy<Usuario> {
    override fun sort(items: List<Usuario>): List<Usuario> {
        val rolOrder = mapOf("Admin" to 1, "Docente" to 2, "Alumno" to 3)
        return items.sortedBy { rolOrder[it.rol] ?: 4 }
    }
    
    override fun getSortingName(): String = "Rol"
}

// 📁 domain/strategy/sorting/MateriaSortingStrategies.kt

class SortMateriasByNombreStrategy : SortingStrategy<Materia> {
    override fun sort(items: List<Materia>): List<Materia> {
        return items.sortedBy { it.nombre.lowercase() }
    }
    
    override fun getSortingName(): String = "Nombre (A-Z)"
}

class SortMateriasBySiglaStrategy : SortingStrategy<Materia> {
    override fun sort(items: List<Materia>): List<Materia> {
        return items.sortedBy { it.sigla }
    }
    
    override fun getSortingName(): String = "Sigla"
}

class SortMateriasByNivelStrategy : SortingStrategy<Materia> {
    override fun sort(items: List<Materia>): List<Materia> {
        return items.sortedBy { it.nivel }
    }
    
    override fun getSortingName(): String = "Nivel"
}

// 📁 domain/strategy/sorting/AsistenciaSortingStrategies.kt

class SortAsistenciasByFechaAscStrategy : SortingStrategy<Asistencia> {
    override fun sort(items: List<Asistencia>): List<Asistencia> {
        return items.sortedBy { it.fecha }
    }
    
    override fun getSortingName(): String = "Fecha (Recientes últimas)"
}

class SortAsistenciasByFechaDescStrategy : SortingStrategy<Asistencia> {
    override fun sort(items: List<Asistencia>): List<Asistencia> {
        return items.sortedByDescending { it.fecha }
    }
    
    override fun getSortingName(): String = "Fecha (Recientes primero)"
}

class SortAsistenciasByAlumnoStrategy : SortingStrategy<Asistencia> {
    override fun sort(items: List<Asistencia>): List<Asistencia> {
        return items.sortedBy { it.idAlumno }
    }
    
    override fun getSortingName(): String = "Alumno"
}
```

**Uso en ViewModel**:
```kotlin
// 📁 domain/viewmodel/VMUsuario.kt
class VMUsuario(private val usuarioCU: UsuarioCU) : ViewModel() {
    
    // Estado de UI
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()
    
    // Estrategia de ordenamiento actual
    private var currentSortingStrategy: SortingStrategy<Usuario> = 
        SortUsuariosByNombreStrategy()
    
    // Estrategias disponibles
    val availableSortingStrategies = listOf(
        SortUsuariosByNombreStrategy(),
        SortUsuariosByNombreDescStrategy(),
        SortUsuariosByApellidoStrategy(),
        SortUsuariosByRegistroStrategy(),
        SortUsuariosByRolStrategy()
    )
    
    init {
        cargarUsuarios()
    }
    
    /**
     * Cambia la estrategia de ordenamiento y recarga los usuarios
     */
    fun changeSortingStrategy(strategy: SortingStrategy<Usuario>) {
        currentSortingStrategy = strategy
        cargarUsuarios()
    }
    
    /**
     * Carga y ordena los usuarios según la estrategia actual
     */
    fun cargarUsuarios() {
        viewModelScope.launch {
            try {
                val usuariosList = usuarioCU.obtenerUsuarios()
                val sortedUsuarios = currentSortingStrategy.sort(usuariosList)
                _usuarios.value = sortedUsuarios
            } catch (e: Exception) {
                Log.e("VMUsuario", "Error cargando usuarios: ${e.message}")
                _usuarios.value = emptyList()
            }
        }
    }
}
```

**Uso en la UI**:
```kotlin
// En PUsuario.kt
@Composable
fun ListaUsuariosConOrdenamiento(viewModel: VMUsuario) {
    val usuarios by viewModel.usuarios.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    
    Column {
        // Botón para mostrar opciones de ordenamiento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Ordenar"
                )
            }
        }
        
        // Menú desplegable con opciones de ordenamiento
        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            viewModel.availableSortingStrategies.forEach { strategy ->
                DropdownMenuItem(
                    text = { Text(strategy.getSortingName()) },
                    onClick = {
                        viewModel.changeSortingStrategy(strategy)
                        showSortMenu = false
                    }
                )
            }
        }
        
        // Lista de usuarios ordenados
        LazyColumn {
            items(usuarios) { usuario ->
                UsuarioCard(usuario)
            }
        }
    }
}
```

**Beneficios**:
- ✅ Cambio dinámico de ordenamiento sin recargar datos
- ✅ Fácil agregar nuevos criterios de ordenamiento
- ✅ Código reutilizable para otras entidades
- ✅ UI más flexible y amigable

---

#### 4. Strategy para Notificaciones

**Problema**:
- Diferentes formas de notificar al usuario (Toast, Snackbar, Dialog, Log)
- Dependiendo del contexto, algunas notificaciones son más apropiadas que otras

**Solución**:
```kotlin
// 📁 domain/strategy/notification/NotificationStrategy.kt
interface NotificationStrategy {
    suspend fun show(message: String, title: String? = null)
    fun getStrategyName(): String
}

// 📁 domain/strategy/notification/ToastNotificationStrategy.kt
class ToastNotificationStrategy(
    private val context: Context
) : NotificationStrategy {
    
    override suspend fun show(message: String, title: String?) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun getStrategyName(): String = "Toast"
}

// 📁 domain/strategy/notification/SnackbarNotificationStrategy.kt
class SnackbarNotificationStrategy(
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) : NotificationStrategy {
    
    override suspend fun show(message: String, title: String?) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    override fun getStrategyName(): String = "Snackbar"
}

// 📁 domain/strategy/notification/DialogNotificationStrategy.kt
class DialogNotificationStrategy(
    private val context: Context
) : NotificationStrategy {
    
    override suspend fun show(message: String, title: String?) {
        withContext(Dispatchers.Main) {
            AlertDialog.Builder(context)
                .setTitle(title ?: "Notificación")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> 
                    dialog.dismiss() 
                }
                .show()
        }
    }
    
    override fun getStrategyName(): String = "Dialog"
}

// 📁 domain/strategy/notification/LogNotificationStrategy.kt
class LogNotificationStrategy(
    private val tag: String = "AsistenciaApp"
) : NotificationStrategy {
    
    override suspend fun show(message: String, title: String?) {
        val fullMessage = if (title != null) {
            "$title: $message"
        } else {
            message
        }
        Log.d(tag, fullMessage)
    }
    
    override fun getStrategyName(): String = "Log"
}

// 📁 domain/strategy/notification/CompositeNotificationStrategy.kt
class CompositeNotificationStrategy(
    private val strategies: List<NotificationStrategy>
) : NotificationStrategy {
    
    override suspend fun show(message: String, title: String?) {
        strategies.forEach { strategy ->
            strategy.show(message, title)
        }
    }
    
    override fun getStrategyName(): String = 
        "Composite (${strategies.joinToString { it.getStrategyName() }})"
}
```

**Gestor de notificaciones**:
```kotlin
// 📁 domain/notification/NotificationManager.kt
class NotificationManager(
    private var strategy: NotificationStrategy
) {
    
    /**
     * Cambia la estrategia de notificación
     */
    fun setStrategy(newStrategy: NotificationStrategy) {
        strategy = newStrategy
    }
    
    /**
     * Muestra una notificación de éxito
     */
    suspend fun showSuccess(message: String) {
        strategy.show("✓ $message", "Éxito")
    }
    
    /**
     * Muestra una notificación de error
     */
    suspend fun showError(message: String) {
        strategy.show("✗ $message", "Error")
    }
    
    /**
     * Muestra una notificación de información
     */
    suspend fun showInfo(message: String) {
        strategy.show("ℹ $message", "Información")
    }
    
    /**
     * Muestra una notificación de advertencia
     */
    suspend fun showWarning(message: String) {
        strategy.show("⚠ $message", "Advertencia")
    }
}
```

**Uso en ViewModel**:
```kotlin
// 📁 domain/viewmodel/VMUsuario.kt
class VMUsuario(
    private val usuarioCU: UsuarioCU,
    private val notificationManager: NotificationManager
) : ViewModel() {
    
    fun agregarUsuario(/* parámetros */) {
        viewModelScope.launch {
            when (val result = usuarioCU.agregarUsuario(/* parámetros */)) {
                is ValidationResult.Success -> {
                    notificationManager.showSuccess("Usuario agregado exitosamente")
                    cargarUsuarios()
                }
                is ValidationResult.Error -> {
                    notificationManager.showError(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun eliminarUsuario(id: Int) {
        viewModelScope.launch {
            when (val result = usuarioCU.eliminarUsuario(id)) {
                is ValidationResult.Success -> {
                    notificationManager.showSuccess("Usuario eliminado")
                    cargarUsuarios()
                }
                is ValidationResult.Error -> {
                    notificationManager.showError(result.message)
                }
                else -> {}
            }
        }
    }
}
```

**Uso en la UI**:
```kotlin
// En PUsuario.kt
@Composable
fun PUsuario() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Configurar estrategia de notificación
    val notificationStrategy = remember {
        SnackbarNotificationStrategy(snackbarHostState, coroutineScope)
    }
    
    val notificationManager = remember {
        NotificationManager(notificationStrategy)
    }
    
    // Crear ViewModel con el NotificationManager
    val db = AppDatabase.getInstance(context)
    val usuarioRepository = UsuarioRepository(db.usuarioDao)
    val usuarioCU = UsuarioCU(usuarioRepository)
    val viewModel = remember {
        VMUsuario(usuarioCU, notificationManager)
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // UI content
        Column(modifier = Modifier.padding(padding)) {
            // ... contenido
        }
    }
}
```

**Beneficios**:
- ✅ Notificaciones consistentes en toda la app
- ✅ Fácil cambiar el tipo de notificación
- ✅ Posibilidad de múltiples notificaciones simultáneas
- ✅ Testeable con estrategias mock

---

## 📊 Diagramas UML

### Diagrama Adapter - Base de Datos

```
┌─────────────────────────┐
│  UsuarioRepository      │
│                         │
│  - databaseAdapter      │
└────────────┬────────────┘
             │ usa
             ▼
┌─────────────────────────┐
│  <<interface>>          │
│  DatabaseAdapter        │
│                         │
│  + getUsuarioDao()      │
│  + getMateriaDao()      │
└────────────┬────────────┘
             △
             │ implementa
    ┌────────┴────────┐
    │                 │
┌───▼─────────────┐  ┌▼──────────────────┐
│ SQLiteDatabase  │  │ RoomDatabase      │
│ Adapter         │  │ Adapter           │
│                 │  │                   │
│ - appDatabase   │  │ - roomDatabase    │
└─────────────────┘  └───────────────────┘
```

### Diagrama Strategy - Validación

```
┌─────────────────────────┐
│  UsuarioCU              │
│                         │
│  - strategies: Map      │
│  + agregarUsuario()     │
└────────────┬────────────┘
             │ usa
             ▼
┌─────────────────────────┐
│  <<interface>>          │
│  ValidationStrategy     │
│                         │
│  + validate()           │
│  + getRoleName()        │
└────────────┬────────────┘
             △
             │ implementa
    ┌────────┼────────┐
    │        │        │
┌───▼───┐ ┌─▼────┐ ┌─▼─────┐
│ Admin │ │Docente│ │Alumno │
│Valid. │ │Valid. │ │Valid. │
└───────┘ └───────┘ └───────┘
```

### Diagrama Strategy - Cálculo de Asistencias

```
┌─────────────────────────┐
│  AsistenciaCU           │
│                         │
│  - calculationStrategy  │
│  + obtenerEstadisticas()│
└────────────┬────────────┘
             │ usa
             ▼
┌──────────────────────────────┐
│  <<interface>>               │
│  AttendanceCalculationStrategy│
│                              │
│  + calculatePercentage()     │
│  + getStatus()               │
│  + getMinimumRequired()      │
└────────────┬─────────────────┘
             △
             │ implementa
    ┌────────┼────────┐
    │        │        │
┌───▼───┐ ┌─▼────┐ ┌─▼─────┐
│Strict │ │Moderate│Lenient│
│ (80%) │ │ (75%) │ (70%) │
└───────┘ └───────┘ └───────┘
```

---

## 🛠️ Guía de Implementación

### Paso 1: Crear estructura de carpetas

```bash
app/src/main/java/com/bo/asistenciaapp/
├── data/
│   ├── local/
│   │   └── adapter/
│   ├── remote/
│   │   ├── dto/
│   │   └── adapter/
│   └── export/
│       └── adapter/
├── domain/
│   ├── strategy/
│   │   ├── validation/
│   │   ├── sorting/
│   │   ├── attendance/
│   │   └── notification/
│   └── notification/
```

### Paso 2: Implementar patrones por prioridad

#### ✅ Alta prioridad (Implementar primero):

1. **Strategy para Validación de Usuarios**
   - Crear `ValidationStrategy` interface
   - Implementar strategies por rol (Admin, Docente, Alumno)
   - Modificar `UsuarioCU` para usar strategies
   - Testear con datos de prueba

2. **Strategy para Cálculo de Asistencias**
   - Crear `AttendanceCalculationStrategy` interface
   - Implementar strategies (Strict, Moderate, Lenient)
   - Modificar `AsistenciaCU` para usar strategies
   - Actualizar UI para mostrar estadísticas

3. **Adapter para API REST**
   - Crear DTOs para todas las entidades
   - Implementar adapters (UsuarioApiAdapter, MateriaApiAdapter, etc.)
   - Modificar repositories para usar adapters
   - Preparar para integración con backend

#### ⭐ Media prioridad:

4. **Strategy para Ordenamiento**
   - Crear `SortingStrategy` interface
   - Implementar strategies para Usuario, Materia, Grupo
   - Modificar ViewModels para usar strategies
   - Actualizar UI con opciones de ordenamiento

5. **Adapter para Exportación**
   - Crear `DataExportAdapter` interface
   - Implementar adapters (CSV, Excel, PDF)
   - Crear UseCase de exportación
   - Agregar botones de exportación en UI

#### 💡 Baja prioridad:

6. **Adapter para Room**
   - Crear interfaces comunes para DAOs
   - Implementar SQLiteAdapter (wrapper actual)
   - Implementar RoomAdapter (para futuro)
   - Migración gradual

7. **Strategy para Notificaciones**
   - Crear `NotificationStrategy` interface
   - Implementar strategies (Toast, Snackbar, Dialog, Log)
   - Crear NotificationManager
   - Integrar en ViewModels

### Paso 3: Testing

Para cada patrón implementado, crear tests unitarios:

```kotlin
// Ejemplo: Test para ValidationStrategy
class AdminValidationStrategyTest {
    
    private lateinit var strategy: AdminValidationStrategy
    
    @Before
    fun setup() {
        strategy = AdminValidationStrategy()
    }
    
    @Test
    fun `test contraseña muy corta debe fallar`() {
        val result = strategy.validate(
            nombres = "Juan",
            apellidos = "Pérez",
            registro = "12345",
            username = "admin1",
            contrasena = "123"  // Muy corta
        )
        
        assertFalse(result.isValid)
        assertTrue(result is ValidationResult.Error)
    }
    
    @Test
    fun `test contraseña sin mayúscula debe fallar`() {
        val result = strategy.validate(
            nombres = "Juan",
            apellidos = "Pérez",
            registro = "12345",
            username = "admin1",
            contrasena = "password123"  // Sin mayúscula
        )
        
        assertFalse(result.isValid)
    }
    
    @Test
    fun `test validación exitosa con datos correctos`() {
        val result = strategy.validate(
            nombres = "Juan",
            apellidos = "Pérez",
            registro = "12345",
            username = "admin1",
            contrasena = "Password123"
        )
        
        assertTrue(result.isValid)
        assertTrue(result is ValidationResult.Success)
    }
}
```

### Paso 4: Documentación

Para cada patrón implementado:

1. Documentar la interfaz con KDoc
2. Documentar cada implementación
3. Agregar ejemplos de uso
4. Actualizar este archivo con los cambios

---

## 📝 Resumen de Beneficios

### Patrón Adapter:
- ✅ Integración fácil con sistemas externos
- ✅ Desacoplamiento entre capas
- ✅ Migración gradual sin romper código existente
- ✅ Reutilización de código
- ✅ Fácil de testear

### Patrón Strategy:
- ✅ Eliminación de if/else anidados
- ✅ Código más limpio y mantenible
- ✅ Fácil agregar nuevos algoritmos
- ✅ Cambio dinámico de comportamiento
- ✅ Cada strategy es independiente y testeable

---

## 🎓 Ejemplos de Casos Reales

### Caso 1: Agregar nuevo rol "Coordinador"

**Sin Strategy:**
```kotlin
fun validarDatosUsuario(rol: String, ...): ValidationResult {
    if (rol == "Admin") {
        // validaciones de admin
    } else if (rol == "Docente") {
        // validaciones de docente
    } else if (rol == "Alumno") {
        // validaciones de alumno
    } else if (rol == "Coordinador") {  // ❌ Modificar código existente
        // validaciones de coordinador
    }
}
```

**Con Strategy:**
```kotlin
// ✅ Solo agregar nueva strategy
class CoordinadorValidationStrategy : ValidationStrategy {
    override fun validate(...): ValidationResult {
        // validaciones específicas
    }
}

// Y registrarla
val validationStrategies = mapOf(
    "Admin" to AdminValidationStrategy(),
    "Docente" to DocenteValidationStrategy(),
    "Alumno" to AlumnoValidationStrategy(),
    "Coordinador" to CoordinadorValidationStrategy()  // ✅ Sin modificar código existente
)
```

### Caso 2: Cambiar de SQLite a Room

**Sin Adapter:**
```kotlin
// ❌ Reescribir todos los repositories
class UsuarioRepository(private val dao: UsuarioDao) {
    // Cambiar toda la implementación
}
```

**Con Adapter:**
```kotlin
// ✅ Solo cambiar el adapter
val databaseAdapter = RoomDatabaseAdapter(roomDb)  // Era: SQLiteDatabaseAdapter(sqliteDb)
val usuarioRepository = UsuarioRepository(databaseAdapter.getUsuarioDao())
// ✅ Todo sigue funcionando sin cambios
```

---

## 📚 Referencias

- **Adapter Pattern**: [Refactoring Guru - Adapter](https://refactoring.guru/design-patterns/adapter)
- **Strategy Pattern**: [Refactoring Guru - Strategy](https://refactoring.guru/design-patterns/strategy)
- **Design Patterns**: Gang of Four (GoF) - Gamma, Helm, Johnson, Vlissides

---

## ✅ Checklist de Implementación

### Patrón Adapter:
- [ ] Crear interfaces adapter para base de datos
- [ ] Implementar SQLiteDatabaseAdapter
- [ ] Implementar adapters para API REST (DTOs y conversión)
- [ ] Implementar adapters para exportación (CSV, Excel, PDF)
- [ ] Crear tests unitarios para cada adapter
- [ ] Documentar cada adapter con ejemplos

### Patrón Strategy:
- [ ] Implementar ValidationStrategy por rol
- [ ] Implementar AttendanceCalculationStrategy
- [ ] Implementar SortingStrategy para entidades principales
- [ ] Implementar NotificationStrategy
- [ ] Integrar strategies en UseCases
- [ ] Actualizar ViewModels para usar strategies
- [ ] Crear tests unitarios para cada strategy
- [ ] Actualizar UI con opciones de cambio de strategy

---

**Última actualización**: [Fecha de creación del documento]
**Versión**: 1.0
**Autor**: AsistenciaApp Development Team

