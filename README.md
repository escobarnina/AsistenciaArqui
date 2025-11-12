# AsistenciaApp

Aplicación Android para la gestión de usuarios, materias, horarios, inscripciones y asistencias académicas. Está construida con Jetpack Compose y emplea una base de datos SQLite local para persistencia.

## Tecnologías utilizadas
- **Lenguaje:** Kotlin (JVM 17).
- **UI:** Jetpack Compose, Material 3, Navigation Compose.
- **Arquitectura de componentes:** ViewModel, StateFlow, corutinas.
- **Persistencia local:** SQLite mediante `SQLiteOpenHelper`.
- **Networking preparado:** Retrofit 2, OkHttp Logging Interceptor (configurados, aún sin endpoints implementados).
- **Gestión de dependencias:** Gradle con catálogo de versiones (`libs.versions.toml`).

## Arquitectura actual
El proyecto sigue una arquitectura en capas limpia y bien definida, respetando el principio de separación de responsabilidades:

```
┌───────────────────────────────┐
│ Capa de Presentación          │  Screens Compose, NavHost, temas
├───────────────▲───────────────┤
│ Capa de Dominio               │  Modelos, Casos de uso, ViewModels
├───────────────▲───────────────┤
│ Capa de Datos  ▼              │  SQLite (AppDatabase), UserSession
└───────────────────────────────┘
```

### Flujo de datos completo

```
┌─────────────────────────────────────────────────────────────┐
│ PRESENTACIÓN (UI Layer)                                      │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │  Screens (Compose)                                       │ │
│ │  ├─ LoginScreen                                         │ │
│ │  ├─ AdminHome, PUsuario, PMateria, PGrupo, PHorario    │ │
│ │  └─ AlumnoHome, GestionarInscripciones, GestionarAsist. │ │
│ │                                                          │ │
│ │  Observa: viewModel.uiState.collectAsState()            │ │
│ │  Dispara: viewModel.accion()                            │ │
│ └────────────────────┬────────────────────────────────────┘ │
└──────────────────────┼──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ DOMINIO - ViewModels (UI State Management)                  │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │  ViewModels                                             │ │
│ │  ├─ VMLogin                                             │ │
│ │  ├─ VMUsuario, VMMateria, VMGrupo, VMHorario          │ │
│ │  └─ VMInscripcion, VMAsistencia                         │ │
│ │                                                          │ │
│ │  Responsabilidades:                                      │ │
│ │  • Gestionar estado de UI (Loading, Success, Error)     │ │
│ │  • Exponer datos reactivos (StateFlow)                  │ │
│ │  • Orquestar casos de uso                               │ │
│ │  • Manejar errores y estados de carga                  │ │
│ └────────────────────┬────────────────────────────────────┘ │
└──────────────────────┼──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ DOMINIO - Casos de Uso (Business Logic)                     │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │  UseCases                                               │ │
│ │  ├─ UsuarioCU                                           │ │
│ │  ├─ MateriaCU, GrupoCU, HorarioCU                      │ │
│ │  └─ InscripcionCU, AsistenciaCU                        │ │
│ │                                                          │ │
│ │  Responsabilidades:                                      │ │
│ │  • Validar datos de entrada                             │ │
│ │  • Aplicar reglas de negocio                            │ │
│ │  • Orquestar operaciones complejas                      │ │
│ │  • Retornar ValidationResult (Success/Error)           │ │
│ └────────────────────┬────────────────────────────────────┘ │
└──────────────────────┼──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ DATOS - Repositories (Data Abstraction)                      │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │  Repositories                                            │ │
│ │  ├─ UsuarioRepository                                   │ │
│ │  ├─ MateriaRepository, GrupoRepository, HorarioRepo   │ │
│ │  └─ InscripcionRepository, AsistenciaRepository        │ │
│ │                                                          │ │
│ │  Responsabilidades:                                      │ │
│ │  • Abstraer acceso a datos                              │ │
│ │  • Delegar a DAOs correspondientes                      │ │
│ │  • Permitir cambio de fuente sin afectar UseCases      │ │
│ └────────────────────┬────────────────────────────────────┘ │
└──────────────────────┼──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ DATOS - DAOs (Data Access Objects)                           │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │  DAOs                                                    │ │
│ │  ├─ UsuarioDao                                          │ │
│ │  ├─ MateriaDao, GrupoDao, HorarioDao                   │ │
│ │  └─ InscripcionDao, AsistenciaDao                      │ │
│ │                                                          │ │
│ │  Responsabilidades:                                      │ │
│ │  • Operaciones CRUD específicas por entidad             │ │
│ │  • Acceso directo a SQLite (raw queries)                │ │
│ │  • Transformar resultados a modelos de dominio          │ │
│ └────────────────────┬────────────────────────────────────┘ │
└──────────────────────┼──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│ DATOS - AppDatabase (Singleton)                              │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │  AppDatabase (Singleton)                                │ │
│ │  • getInstance(context) - Instancia única               │ │
│ │  • Proporciona acceso lazy a DAOs                       │ │
│ │  • Gestiona ciclo de vida de SQLite                     │ │
│ │                                                          │ │
│ │  UserSession (SharedPreferences)                        │ │
│ │  • Manejo de sesión de usuario                         │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Relaciones entre componentes

**Capa de Presentación:**
- **Screens Compose** → Observan y llaman a **ViewModels**
- **ViewModels** → Usan **UseCases** para lógica de negocio
- **Navegación** → `AppNavHost` gestiona rutas según rol

**Capa de Dominio:**
- **ViewModels** → Dependen de **UseCases** (inyección manual actual)
- **UseCases** → Dependen de **Repositories** (inyección manual actual)
- **Modelos** → Entidades de dominio independientes de frameworks

**Capa de Datos:**
- **Repositories** → Dependen de **DAOs** (inyección manual actual)
- **DAOs** → Dependen de **AppDatabase** (acceso a SQLiteDatabase)
- **AppDatabase** → Singleton que proporciona acceso a DAOs
- **UserSession** → Independiente, usa SharedPreferences

### Principios arquitectónicos aplicados

1. **Separación de Responsabilidades (SRP):** Cada capa tiene una responsabilidad única y bien definida
2. **Dependency Inversion:** Las capas superiores dependen de abstracciones (Repositories), no de implementaciones concretas
3. **Single Source of Truth:** ViewModels son la única fuente de verdad para el estado de UI
4. **Unidirectional Data Flow:** Datos fluyen en una sola dirección (UI → ViewModel → UseCase → Repository → DAO → Database)
5. **Testabilidad:** Cada capa puede ser testeada independientemente mediante mocking

### Relaciones entre clases detalladas

#### Relación: Screen → ViewModel → UseCase → Repository → DAO

```
┌─────────────────────────────────────────────────────────────┐
│ Ejemplo: PUsuario.kt (Screen)                               │
│                                                              │
│ val viewModel: VMUsuario = viewModel {                      │
│     VMUsuario(usuarioCU)  ←─── Depende de UseCase          │
│ }                                                            │
│                                                              │
│ viewModel.agregarUsuario(...)  ←─── Llama método            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ VMUsuario.kt (ViewModel)                                    │
│                                                              │
│ class VMUsuario(                                             │
│     private val usuarioCU: UsuarioCU  ←─── Depende de UseCase│
│ ) {                                                          │
│     fun agregarUsuario(...) {                                │
│         val result = usuarioCU.agregarUsuario(...)           │
│     }                                                        │
│ }                                                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ UsuarioCU.kt (UseCase)                                      │
│                                                              │
│ class UsuarioCU(                                            │
│     private val usuarioRepository: UsuarioRepository  ←─── Depende de Repository│
│ ) {                                                          │
│     fun agregarUsuario(...): ValidationResult {              │
│         usuarioRepository.agregar(...)                       │
│     }                                                        │
│ }                                                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ UsuarioRepository.kt (Repository)                           │
│                                                              │
│ class UsuarioRepository(                                    │
│     private val database: AppDatabase  ←─── Depende de AppDatabase│
│ ) {                                                          │
│     fun agregar(...) {                                      │
│         database.usuarioDao.insertar(...)  ←─── Usa DAO     │
│     }                                                        │
│ }                                                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ UsuarioDao.kt (DAO)                                         │
│                                                              │
│ class UsuarioDao(                                           │
│     private val database: SQLiteDatabase  ←─── Depende de SQLite│
│ ) {                                                          │
│     fun insertar(...) {                                     │
│         database.execSQL("INSERT INTO usuarios...")          │
│     }                                                        │
│ }                                                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ AppDatabase.kt (Singleton)                                  │
│                                                              │
│ class AppDatabase private constructor(...) {                │
│     val usuarioDao: UsuarioDao by lazy {                    │
│         UsuarioDao(writableDatabase)  ←─── Crea DAO         │
│     }                                                        │
│ }                                                            │
└─────────────────────────────────────────────────────────────┘
```

#### Dependencias en el código

**En las pantallas (Presentation):**
```kotlin
// PUsuario.kt
val database = remember { AppDatabase.getInstance(context) }
val usuarioRepository = remember { UsuarioRepository(database) }
val usuarioCU = remember { UsuarioCU(usuarioRepository) }
val viewModel: VMUsuario = viewModel { VMUsuario(usuarioCU) }
```

**En los ViewModels (Domain):**
```kotlin
// VMUsuario.kt
class VMUsuario(private val usuarioCU: UsuarioCU) {
    fun agregarUsuario(...) {
        val result = usuarioCU.agregarUsuario(...)
    }
}
```

**En los UseCases (Domain):**
```kotlin
// UsuarioCU.kt
class UsuarioCU(private val usuarioRepository: UsuarioRepository) {
    fun agregarUsuario(...): ValidationResult {
        usuarioRepository.agregar(...)
    }
}
```

**En los Repositories (Data):**
```kotlin
// UsuarioRepository.kt
class UsuarioRepository(private val database: AppDatabase) {
    fun agregar(...) {
        database.usuarioDao.insertar(...)
    }
}
```

**En los DAOs (Data):**
```kotlin
// UsuarioDao.kt
class UsuarioDao(private val database: SQLiteDatabase) {
    fun insertar(...) {
        database.execSQL("INSERT INTO usuarios...")
    }
}
```

#### Reglas de dependencia

✅ **Permitido:**
- Pantallas → ViewModels
- ViewModels → UseCases
- UseCases → Repositories
- Repositories → DAOs
- DAOs → AppDatabase
- ViewModels → UserSession (para sesión)

❌ **Prohibido:**
- Pantallas → UseCases directamente (debe pasar por ViewModel)
- Pantallas → Repositories directamente
- Pantallas → DAOs directamente
- Pantallas → AppDatabase directamente (excepto para crear dependencias)
- UseCases → DAOs directamente (debe pasar por Repository)
- ViewModels → DAOs directamente

## Composición del proyecto

### Estructura de directorios

```
app/src/main/java/com/bo/asistenciaapp/
├── MainActivity.kt                    # Punto de entrada de la aplicación
├── data/                              # CAPA DE DATOS
│   ├── local/
│   │   ├── AppDatabase.kt            # Singleton - Gestión de conexión SQLite
│   │   ├── DatabaseMigrations.kt     # Esquema y migraciones de BD
│   │   ├── DatabaseSeeder.kt         # Datos iniciales de prueba
│   │   ├── dao/                      # Data Access Objects (CRUD por entidad)
│   │   │   ├── UsuarioDao.kt
│   │   │   ├── MateriaDao.kt
│   │   │   ├── GrupoDao.kt
│   │   │   ├── HorarioDao.kt
│   │   │   ├── InscripcionDao.kt
│   │   │   └── AsistenciaDao.kt
│   │   ├── UserSession.kt            # SharedPreferences para sesión
│   │   └── StringRange.kt            # Utilidades
│   └── repository/                   # Repositorios (abstracción de acceso a datos)
│       ├── UsuarioRepository.kt
│       ├── MateriaRepository.kt
│       ├── GrupoRepository.kt
│       ├── HorarioRepository.kt
│       ├── InscripcionRepository.kt
│       └── AsistenciaRepository.kt
├── domain/                            # CAPA DE DOMINIO
│   ├── model/                        # Modelos de datos
│   │   ├── Usuario.kt
│   │   ├── Materia.kt
│   │   ├── Grupo.kt
│   │   ├── Horario.kt
│   │   ├── Asistencia.kt
│   │   └── Boleta.kt
│   ├── usecase/                      # Casos de uso (lógica de negocio)
│   │   ├── UsuarioCU.kt
│   │   ├── MateriaCU.kt
│   │   ├── GrupoCU.kt
│   │   ├── HorarioCU.kt
│   │   ├── InscripcionCU.kt
│   │   └── AsistenciaCU.kt
│   ├── viewmodel/                    # ViewModels (estado UI)
│   │   ├── VMLogin.kt                # ViewModel para login
│   │   ├── VMUsuario.kt             # Gestión de usuarios
│   │   ├── VMMateria.kt             # Gestión de materias
│   │   ├── VMGrupo.kt               # Gestión de grupos
│   │   ├── VMHorario.kt             # Gestión de horarios
│   │   ├── VMInscripcion.kt         # Gestión de inscripciones
│   │   └── VMAsistencia.kt          # Gestión de asistencias
│   └── utils/                        # Utilidades de dominio
│       ├── Validators.kt            # Validadores reutilizables
│       └── ValidationResult.kt      # Resultado de validaciones
└── presentation/                      # CAPA DE PRESENTACIÓN
    ├── login/
    │   └── LoginScreen.kt
    ├── admin/
    │   ├── AdminHome.kt
    │   ├── PUsuario.kt              # Gestión Usuarios
    │   ├── PMateria.kt              # Gestión Materias
    │   ├── PGrupo.kt                # Gestión Grupos
    │   ├── PHorario.kt              # Gestión Horarios
    │   └── navigation/
    │       └── AppNavHost.kt        # Navegación principal
    └── alumno/
        ├── AlumnoHomeScreen.kt
        ├── GestionarInscripciones.kt
        └── GestionarAsistenca.kt
```

### Capa de Datos (`data/`)

**Responsabilidad:** Persistencia y acceso a datos con arquitectura completa.

#### Arquitectura de la capa de datos

```
AppDatabase (Singleton)
    ↓
DAOs (Data Access Objects)
    ↓
Repositories (Abstracción)
    ↓
UseCases
```

#### `AppDatabase.kt` (Singleton - SQLiteOpenHelper)
- **Patrón Singleton:** Instancia única gestionada mediante `getInstance(context)`
- Base de datos SQLite: `asistenciadb.db` (versión 16)
- **Responsabilidad:** Gestión de conexión y acceso a DAOs
- Proporciona acceso lazy a DAOs:
  - `usuarioDao`: Operaciones CRUD de usuarios
  - `materiaDao`: Operaciones CRUD de materias
  - `grupoDao`: Operaciones CRUD de grupos
  - `horarioDao`: Operaciones CRUD de horarios
  - `inscripcionDao`: Operaciones CRUD de inscripciones (boletas)
  - `asistenciaDao`: Operaciones CRUD de asistencias
- **Uso:** `AppDatabase.getInstance(context)` - NO instanciar directamente

#### `DatabaseMigrations.kt`
- **Responsabilidad:** Gestión del esquema y migraciones de la base de datos
- Contiene todas las definiciones `CREATE TABLE`
- Métodos principales:
  - `createTables()`: Crea todas las tablas
  - `migrate()`: Gestiona migraciones entre versiones
- Tablas principales:
  - `usuarios`: id, nombres, apellidos, username, contraseña, registro, rol
  - `materias`: id, nombre, sigla (única), nivel
  - `grupos`: id, materia_id, materia_nombre, docente_id, docente_nombre, grupo, semestre, gestión, capacidad, nro_inscritos
  - `horarios`: id, grupo_id, dia, hora_inicio, hora_fin
  - `boletas`: id, alumno_id, grupo_id, fecha, semestre, gestión
  - `asistencias`: id, alumno_id, grupo_id, fecha

#### `DatabaseSeeder.kt`
- **Responsabilidad:** Datos iniciales de prueba (seeders)
- Se ejecuta automáticamente al crear la base de datos
- **Datos de prueba incluidos:**
  - **10 usuarios:** 3 alumnos, 5 docentes, 2 admins
  - **20 materias:** Variadas de diferentes niveles (Programación, Base de Datos, Matemáticas, etc.)
  - **20 grupos:** Relacionando materias con docentes, diferentes paralelos
  - **38 horarios:** Múltiples horarios por grupo (Lunes a Viernes, 08:00-18:00)
  - **21 boletas (inscripciones):** Alumnos inscritos en múltiples grupos
  - **34 asistencias:** Registros de asistencia distribuidos en diferentes fechas
- Método `clearSeedData()` disponible para resetear datos en desarrollo

#### DAOs (`data/local/dao/`)
- **Responsabilidad:** Operaciones CRUD específicas por entidad
- Cada DAO maneja una tabla específica:
  - **`UsuarioDao`**: validarUsuario(), obtenerTodos(), obtenerDocentes(), insertar(), eliminar(), actualizar()
  - **`MateriaDao`**: obtenerTodas(), insertar(), eliminar()
  - **`GrupoDao`**: obtenerTodos(), insertar(), eliminar()
  - **`HorarioDao`**: obtenerTodos(), insertar(), eliminar()
  - **`InscripcionDao`**: obtenerPorAlumno(), insertar(), tieneCruceDeHorario()
  - **`AsistenciaDao`**: obtenerPorAlumno(), insertar(), puedeMarcarAsistencia()
- Acceso directo a SQLite (raw queries)
- Solo usado por Repositories

#### Repositories (`data/repository/`)
- **Responsabilidad:** Abstraer el acceso a datos
- Cada Repository usa su DAO correspondiente:
  - **`UsuarioRepository`**: Usa `UsuarioDao`
  - **`MateriaRepository`**: Usa `MateriaDao`
  - **`GrupoRepository`**: Usa `GrupoDao`
  - **`HorarioRepository`**: Usa `HorarioDao`
  - **`InscripcionRepository`**: Usa `InscripcionDao`
  - **`AsistenciaRepository`**: Usa `AsistenciaDao`
- Permiten cambiar la fuente de datos sin afectar casos de uso
- Facilita pruebas unitarias mediante mocking

#### `UserSession.kt` (SharedPreferences)
- Maneja la sesión del usuario logueado
- Guarda: `userId`, `userName`, `userRol`
- Métodos principales:
  - `saveUser(id, nombre, rol)`: Guarda datos de sesión
  - `getUserId()`: Obtiene ID del usuario
  - `getUserRol()`: Obtiene rol del usuario
  - `getUserName()`: Obtiene nombre del usuario
  - `clear()`: Limpia la sesión

### Capa de Dominio (`domain/`)

**Responsabilidad:** Lógica de negocio y modelos de datos.

#### Modelos (`domain/model/`)
- **`Usuario`**: id, nombres, apellidos, registro, rol (Admin/Docente/Alumno), username
- **`Materia`**: id, nombre, sigla (única), nivel
- **`Grupo`**: id, materia_id, materia_nombre, docente_id, docente_nombre, paralelo
- **`Horario`**: id, grupo_id, dia_semana, hora_inicio, hora_fin
- **`Asistencia`**: id, inscripcion_id, fecha, estado
- **`Boleta`**: id, inscripcion_id, nota_final

#### Casos de Uso (`domain/usecase/`)
Encapsulan la lógica de negocio y orquestan operaciones usando Repositories. Cada caso de uso sigue el patrón de validación y retorno de resultados:

**Casos de uso implementados:**
- **`UsuarioCU`**: 
  - `validarUsuario()`: Autenticación
  - `obtenerUsuarios()`, `obtenerDocentes()`: Consultas
  - `agregarUsuario()`, `eliminarUsuario()`, `actualizarUsuario()`: CRUD con validaciones
- **`MateriaCU`**: 
  - `obtenerMaterias()`: Consulta
  - `agregarMateria()`, `eliminarMateria()`: CRUD con validación de siglas únicas
- **`GrupoCU`**: 
  - `obtenerGrupos()`: Consulta
  - `agregarGrupo()`, `eliminarGrupo()`: CRUD con validaciones de capacidad y semestre
- **`HorarioCU`**: 
  - `obtenerHorarios()`: Consulta
  - `agregarHorario()`, `eliminarHorario()`: CRUD con validación de rangos de tiempo
- **`InscripcionCU`**: 
  - `obtenerInscripciones()`: Consulta por alumno
  - `agregarInscripcion()`: Validación de cruces de horario
  - `tieneCruceDeHorario()`: Verificación de conflictos
- **`AsistenciaCU`**: 
  - `obtenerAsistencias()`: Consulta por alumno
  - `marcarAsistencia()`: Validación de horarios y permisos
  - `puedeMarcarAsistencia()`: Verificación de condiciones

**Características:**
- **Validaciones:** Usan `Validators` y retornan `ValidationResult` (Success/Error)
- **Reglas de negocio:** Implementan validaciones específicas (username único, sigla única, cruces de horario, etc.)
- **Dependencias:** Reciben Repositories en el constructor (inyección manual actual)
- **Independencia:** No dependen de frameworks Android, solo de Kotlin estándar

**Ejemplo de validación:**
```kotlin
fun agregarUsuario(...): ValidationResult {
    val validation = validarDatosUsuario(...)
    if (!validation.isValid) return validation
    
    // Validación de negocio adicional
    val usuarioExistente = usuarioRepository.obtenerTodos().find { it.username == username }
    if (usuarioExistente != null) {
        return ValidationResult.Error("El username ya está en uso")
    }
    
    usuarioRepository.agregar(...)
    return ValidationResult.Success
}
```

**Arquitectura:** Los casos de uso usan Repositories, que a su vez usan DAOs, siguiendo el patrón Repository. Esto permite cambiar la fuente de datos sin afectar la lógica de negocio.

#### ViewModels (`domain/viewmodel/`)
Gestionan el estado de la UI y exponen datos reactivos usando el patrón de UI State Management:

**ViewModels implementados:**
- **`VMLogin`**: Maneja autenticación y estado de login (Idle, Loading, Success, Error)
- **`VMUsuario`**: Gestión completa de usuarios con UI state management
- **`VMMateria`**: Gestión de materias con validaciones y estados
- **`VMGrupo`**: Gestión de grupos, incluye materias y docentes
- **`VMHorario`**: Gestión de horarios con validación de conflictos
- **`VMInscripcion`**: Gestión de inscripciones con validación de cruces de horario
- **`VMAsistencia`**: Gestión de asistencias con validación de horarios

**Características:**
- **UI State Management:** Cada ViewModel expone un `sealed class` para estados (Idle, Loading, Success, Error)
- **StateFlow reactivo:** Exponen datos mediante `StateFlow` que la UI observa con `collectAsState()`
- **Manejo de errores:** Capturan excepciones y las exponen como estados de error
- **Validaciones:** Los UseCases retornan `ValidationResult` que los ViewModels manejan
- **Corutinas:** Ejecutan casos de uso en `viewModelScope.launch`
- **Dependencias:** Actualmente inyección manual (oportunidad de mejora con Hilt)

**Ejemplo de uso:**
```kotlin
// En la pantalla
val viewModel: VMUsuario = viewModel { VMUsuario(usuarioCU) }
val usuarios by viewModel.usuarios.collectAsState()
val uiState by viewModel.uiState.collectAsState()

// Observar estados
LaunchedEffect(uiState) {
    when (val state = uiState) {
        is UsuarioUiState.Success -> { /* Mostrar mensaje */ }
        is UsuarioUiState.Error -> { /* Mostrar error */ }
        else -> {}
    }
}

// Disparar acciones
viewModel.agregarUsuario(nombres, apellidos, ...)
```

### Capa de Presentación (`presentation/`)

**Responsabilidad:** Interfaz de usuario con Jetpack Compose.

#### Navegación (`presentation/admin/navigation/AppNavHost.kt`)
- Usa Navigation Compose para gestionar el flujo de pantallas
- Rutas principales:
  - `login`: Pantalla de inicio de sesión
  - `adminHome`: Dashboard del administrador
  - `alumnoHome`: Dashboard del alumno
  - `docenteHome`: Dashboard del docente (preparado, sin implementar)
- Redirección automática según rol guardado en `UserSession`
- Pantallas de administración:
  - `gestionUsuarios`: CRUD de usuarios
  - `gestionMaterias`: CRUD de materias
  - `gestionGrupos`: CRUD de grupos
  - `gestionHorarios`: CRUD de horarios
  - `gestionarInscripciones`: Gestión de inscripciones
- Pantallas de alumno:
  - `gestionarInscripciones`: Ver y gestionar sus inscripciones
  - `gestionarAsistencias`: Ver y gestionar sus asistencias

#### Pantallas principales

Todas las pantallas siguen el mismo patrón arquitectónico: **UI → ViewModel → UseCase → Repository → DAO → Database**

1. **`LoginScreen.kt`**
   - **ViewModel:** `VMLogin` (maneja estados: Idle, Loading, Success, Error)
   - **UseCase:** `UsuarioCU.validarUsuario()`
   - **Flujo:** Valida credenciales → Guarda sesión en `UserSession` → Redirige según rol
   - **UI State:** Maneja estados de carga y errores con Material Design 3

2. **`AdminHomeScreen.kt`**
   - Dashboard principal del administrador
   - Navegación a todas las pantallas de gestión
   - Opción de logout

3. **`GestionarUsuariosScreen.kt`** (PUsuario.kt)
   - **ViewModel:** `VMUsuario`
   - **UseCase:** `UsuarioCU` (agregar, eliminar, actualizar)
   - **Características:** CRUD completo con validaciones, estados de carga, manejo de errores
   - **UI:** Lista reactiva, formularios con validación, diálogos de edición

4. **`GestionarMateriasScreen.kt`** (PMateria.kt)
   - **ViewModel:** `VMMateria`
   - **UseCase:** `MateriaCU` (agregar, eliminar)
   - **Validaciones:** Siglas únicas, niveles válidos
   - **UI:** Lista con acciones de eliminación, formulario con validación

5. **`GestionarGruposScreen.kt`** (PGrupo.kt)
   - **ViewModel:** `VMGrupo` (incluye materias y docentes)
   - **UseCase:** `GrupoCU` (agregar, eliminar)
   - **Características:** Dropdowns para selección de materia y docente
   - **Validaciones:** Capacidad, semestre, gestión

6. **`GestionarHorarios.kt`** (PHorario.kt)
   - **ViewModel:** `VMHorario` (incluye grupos)
   - **UseCase:** `HorarioCU` (agregar, eliminar)
   - **Validaciones:** Formato de hora (HH:mm), rango válido
   - **UI:** Selector de grupo y día, campos de hora

7. **`AlumnoHomeScreen.kt`**
   - Dashboard principal del alumno
   - Acceso a sus inscripciones y asistencias

8. **`GestionarInscripciones.kt`**
   - **ViewModel:** `VMInscripcion` (incluye grupos disponibles y boletas del alumno)
   - **UseCase:** `InscripcionCU` (agregar con validación de cruces)
   - **Validaciones:** Cruce de horarios, capacidad del grupo
   - **UI:** Lista de grupos disponibles, botón de inscripción, lista de inscripciones actuales

9. **`GestionarAsistenca.kt`**
   - **ViewModel:** `VMAsistencia` (incluye grupos inscritos y asistencias)
   - **UseCase:** `AsistenciaCU` (marcar con validación de horarios)
   - **Validaciones:** Horario correcto, alumno inscrito
   - **UI:** Lista de grupos para marcar asistencia, historial de asistencias

### Flujo de datos detallado

**Ejemplo completo: Agregar un usuario desde la UI**

```
1. Usuario hace clic en "Agregar Usuario"
   └─> PUsuario.kt: Button.onClick { viewModel.agregarUsuario(...) }

2. ViewModel procesa la acción
   └─> VMUsuario.agregarUsuario()
       ├─> Cambia estado a Loading
       └─> Ejecuta en viewModelScope.launch

3. ViewModel llama al Caso de Uso
   └─> UsuarioCU.agregarUsuario(...)
       ├─> Valida datos (Validators)
       ├─> Verifica reglas de negocio (username único)
       └─> Retorna ValidationResult

4. Caso de Uso usa el Repository
   └─> UsuarioRepository.agregar(...)
       └─> Delega al DAO

5. Repository delega al DAO
   └─> UsuarioDao.insertar(...)
       └─> Ejecuta SQL en SQLiteDatabase

6. DAO accede a la base de datos
   └─> AppDatabase.writableDatabase.execSQL(...)
       └─> SQLite guarda el registro

7. ViewModel actualiza el estado
   └─> Si Success: recargar() → actualiza StateFlow
   └─> Si Error: actualiza uiState con mensaje de error

8. UI reacciona al cambio
   └─> collectAsState() detecta cambio
       ├─> Actualiza lista de usuarios
       └─> Muestra mensaje de éxito/error
```

**Flujo de datos reactivo:**

```
┌─────────────────────────────────────────────────────────┐
│  PRESENTACIÓN (Compose Screens)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │ LoginScreen  │  │ AdminHome   │  │ AlumnoHome  │    │
│  │              │  │ PUsuario    │  │ Gestionar   │    │
│  │              │  │ PMateria    │  │ Inscripc.   │    │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │
│         │                 │                  │            │
│         │ Observa StateFlow│                  │            │
│         │ Dispara acciones │                  │            │
│         └─────────────────┼──────────────────┘            │
│                           │                              │
│                    AppNavHost                            │
└───────────────────────────┼──────────────────────────────┘
                             │
                             ▼ (viewModel.accion())
┌─────────────────────────────────────────────────────────┐
│  DOMINIO - ViewModels (UI State Management)              │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ VMLogin      │  │ VMUsuario    │                      │
│  │ VMGrupo      │  │ VMMateria    │                      │
│  │ VMHorario    │  │ VMInscripcion│                      │
│  │ VMAsistencia │  │              │                      │
│  │              │  │              │                      │
│  │ Estados:     │  │ Estados:     │                      │
│  │ • Idle       │  │ • Idle       │                      │
│  │ • Loading    │  │ • Loading    │                      │
│  │ • Success    │  │ • Success    │                      │
│  │ • Error      │  │ • Error      │                      │
│  └──────┬───────┘  └──────┬───────┘                      │
│         │                 │                              │
│         │ StateFlow        │                              │
│         │ (reactivo)       │                              │
└─────────┼─────────────────┼────────────────────────────┘
          │                 │
          ▼ (usecase.accion())
┌─────────────────────────────────────────────────────────┐
│  DOMINIO - Casos de Uso (Business Logic)                 │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ UsuarioCU    │  │ MateriaCU    │                      │
│  │ GrupoCU      │  │ HorarioCU    │                      │
│  │ InscripcionCU│  │ AsistenciaCU │                      │
│  │              │  │              │                      │
│  │ Validaciones │  │ Validaciones │                      │
│  │ Reglas negocio│ │ Reglas negocio│                     │
│  │ Retorna:     │  │ Retorna:     │                      │
│  │ ValidationResult││ ValidationResult│                 │
│  └──────┬───────┘  └──────┬───────┘                      │
└─────────┼─────────────────┼────────────────────────────┘
          │                 │
          ▼ (repository.accion())
┌─────────────────────────────────────────────────────────┐
│  DATOS - Repositories (Data Abstraction)                  │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ UsuarioRepo  │  │ MateriaRepo  │                      │
│  │ GrupoRepo    │  │ HorarioRepo  │                      │
│  │ InscripcionRepo││ AsistenciaRepo│                    │
│  │              │  │              │                      │
│  │ Abstrae acceso│ │ Abstrae acceso│                     │
│  └──────┬───────┘  └──────┬───────┘                      │
└─────────┼─────────────────┼────────────────────────────┘
          │                 │
          ▼ (dao.accion())
┌─────────────────────────────────────────────────────────┐
│  DATOS - DAOs (Data Access Objects)                      │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ UsuarioDao   │  │ MateriaDao   │                      │
│  │ GrupoDao     │  │ HorarioDao   │                      │
│  │ InscripcionDao│ │ AsistenciaDao│                      │
│  │              │  │              │                      │
│  │ CRUD SQLite  │  │ CRUD SQLite  │                      │
│  └──────┬───────┘  └──────┬───────┘                      │
└─────────┼─────────────────┼────────────────────────────┘
          │                 │
          ▼ (AppDatabase.getInstance())
┌─────────────────────────────────────────────────────────┐
│  DATOS - AppDatabase (Singleton)                        │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ AppDatabase   │  │ UserSession │                      │
│  │ (SQLite)     │  │ (SharedPref)│                      │
│  │              │  │              │                      │
│  │ • Singleton  │  │ • Sesión     │                      │
│  │ • DAOs lazy  │  │ • Usuario    │                      │
│  └──────────────┘  └──────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

### Entidades principales del sistema

1. **Usuario**
   - Roles: Admin, Docente, Alumno
   - Campos: nombres, apellidos, registro, username, contraseña

2. **Materia**
   - Campos: nombre, sigla (única), nivel

3. **Grupo**
   - Relación: Materia + Docente + Paralelo

4. **Horario**
   - Relación: Grupo + Día + Hora inicio/fin

5. **Inscripción**
   - Relación: Alumno + Grupo + Semestre + Año

6. **Asistencia**
   - Relación: Inscripción + Fecha + Estado

7. **Boleta**
   - Relación: Inscripción + Nota final

### Puntos importantes de la arquitectura actual

1. **✅ Patrón Singleton implementado:** `AppDatabase` es una instancia única gestionada mediante `getInstance(context)`
2. **✅ Patrón Repository implementado:** Los casos de uso usan Repositories que abstraen el acceso a datos
3. **✅ DAOs separados por entidad:** Cada entidad tiene su propio DAO con operaciones CRUD específicas
4. **✅ Separación de responsabilidades:** DatabaseMigrations (esquema), DatabaseSeeder (datos), AppDatabase (conexión), DAOs (CRUD), Repositories (abstracción)
5. **Sin inyección de dependencias:** Dependencias manuales en ViewModels (oportunidad de mejora con Hilt)
6. **SQLite directo:** No usa Room (usa `SQLiteOpenHelper` con arquitectura limpia)
7. **Retrofit configurado:** Preparado pero sin endpoints implementados
8. **Navegación por roles:** `AppNavHost` redirige según rol guardado
9. **Estado reactivo:** ViewModels usan `StateFlow` para actualizar UI
10. **Datos de prueba completos:** Seeder incluye 10 usuarios, 20 materias, 20 grupos, 38 horarios, 21 inscripciones y 34 asistencias

## Flujo del sistema

### Flujo de inicio y autenticación

1. **Inicio de la aplicación:**
   - `MainActivity` carga `AppNavHost`
   - `AppNavHost` verifica si hay sesión activa en `UserSession`
   - Si hay sesión → Redirige según rol (Admin/Alumno/Docente)
   - Si no hay sesión → Muestra `LoginScreen`

2. **Proceso de autenticación:**
   ```
   LoginScreen
     ↓ (Usuario ingresa credenciales)
   VMLogin.login(username, password)
     ↓ (Cambia estado a Loading)
   UsuarioCU.validarUsuario(username, password)
     ↓ (Valida campos no vacíos)
   UsuarioRepository.validarUsuario(...)
     ↓ (Delega al DAO)
   UsuarioDao.validarUsuario(...)
     ↓ (Consulta SQLite)
   AppDatabase.writableDatabase.rawQuery(...)
     ↓ (Retorna Usuario o null)
   VMLogin recibe resultado
     ├─> Si válido: UserSession.saveUser() → Estado Success → onLoginSuccess()
     └─> Si inválido: Estado Error → Muestra mensaje
   ```

3. **Ruteo por rol:**
   - `AppNavHost` lee `UserSession.getUserRol()`
   - **Admin** → `AdminHome` → Acceso a todas las pantallas de gestión
   - **Alumno** → `AlumnoHome` → Acceso a inscripciones y asistencias
   - **Docente** → `DocenteHome` → (Pendiente de implementar)

### Flujo de gestión de datos (CRUD)

**Ejemplo: Agregar una materia**

```
PMateria.kt (UI)
  ↓ Usuario completa formulario y hace clic en "Agregar"
VMMateria.agregarMateria(nombre, sigla, nivel)
  ↓ Cambia uiState a Loading
MateriaCU.agregarMateria(...)
  ↓ Valida datos (Validators.isNotEmpty, Validators.hasMinLength, etc.)
  ↓ Verifica sigla única (regla de negocio)
MateriaRepository.agregar(...)
  ↓ Delega al DAO
MateriaDao.insertar(...)
  ↓ Ejecuta SQL INSERT
AppDatabase.writableDatabase.execSQL(...)
  ↓ SQLite guarda el registro
VMMateria recibe ValidationResult.Success
  ↓ Llama a recargar() → Actualiza StateFlow de materias
  ↓ Cambia uiState a Success con mensaje
PMateria.kt detecta cambio en StateFlow
  ↓ Actualiza lista de materias automáticamente
  ↓ Muestra Snackbar con mensaje de éxito
```

**Características del flujo:**
- **Unidireccional:** Datos fluyen en una sola dirección
- **Reactivo:** UI se actualiza automáticamente cuando cambian los StateFlow
- **Validado:** Cada capa valida según su responsabilidad
- **Manejo de errores:** Errores se propagan y se muestran en la UI
- **Estados de carga:** UI muestra indicadores durante operaciones

### Inicialización de datos

Al crear la base de datos por primera vez:
1. `AppDatabase.onCreate()` se ejecuta
2. `DatabaseMigrations.createTables()` crea todas las tablas
3. `DatabaseSeeder.seed()` inserta datos de prueba:
   - 10 usuarios (3 alumnos, 5 docentes, 2 admins)
   - 20 materias de diferentes niveles
   - 20 grupos relacionando materias con docentes
   - 38 horarios distribuidos en la semana
   - 21 inscripciones (alumnos en grupos)
   - 34 asistencias registradas

## Patrones de diseño implementados

### ✅ Singleton Pattern
- **`AppDatabase`** implementa patrón Singleton
- Instancia única gestionada mediante `getInstance(context)`
- Evita múltiples conexiones a la base de datos
- Facilita pruebas y gestión de recursos

### ✅ Repository Pattern
- Repositorios en `data/repository/` abstraen el acceso a datos
- Casos de uso trabajan con Repositories, no directamente con DAOs
- Permite cambiar la fuente de datos (local/remote) sin afectar casos de uso
- Facilita migración futura a Room o servicios REST

### ✅ Data Access Object (DAO) Pattern
- DAOs separados por entidad en `data/local/dao/`
- Cada DAO maneja operaciones CRUD de una tabla específica
- Separación clara de responsabilidades
- Facilita mantenimiento y pruebas

### ✅ Separation of Concerns
- **DatabaseMigrations:** Esquema y migraciones
- **DatabaseSeeder:** Datos iniciales
- **AppDatabase:** Gestión de conexión
- **DAOs:** Operaciones CRUD por entidad
- **Repositories:** Abstracción de acceso a datos
- **UseCases:** Lógica de negocio
- **ViewModels:** Estado de UI

## Patrones de diseño recomendados para añadir
- **Inyección de dependencias (Hilt/Dagger):** Para eliminar dependencias manuales en ViewModels y casos de uso
- **Room Migration:** Migrar de `SQLiteOpenHelper` a Room para ganar seguridad en el acceso a datos y migraciones automáticas

## Librerías principales
- Android Gradle Plugin 8.11.2, Kotlin 2.0.21.
- AndroidX Core KTX 1.10.1, Lifecycle Runtime KTX 2.6.1.
- Activity Compose 1.8.0, Compose BOM 2024.09.00, Material 3.
- Navigation Compose 2.9.4.
- Retrofit 2.9.0, Gson Converter, OkHttp Logging Interceptor 4.11.0.
- JUnit 4.13.2, AndroidX Test (JUnit 1.1.5, Espresso 3.5.1) y tooling de Compose para pruebas.

## Requisitos de ejecución
- Android Studio Iguana (o superior compatible con AGP 8.11).
- JDK 17 (configurado automáticamente por el JDK embebido de Android Studio).
- Android SDK 26+ (minSdk 26, target/compileSdk 36).
- Gradle Wrapper incluido (`./gradlew` o `gradlew.bat`), sin dependencias externas adicionales.
- Para datos locales no se requiere backend; la base SQLite se inicializa con datos de prueba.

### Ejecución en emulador o dispositivo
- **Emulador:** Configurar un dispositivo virtual con API ≥ 26; la app funciona sin dependencias de hardware específico.
- **Dispositivo físico:** Activar modo desarrollador y depuración USB; la app corre directamente en un teléfono Android compatible (se almacenan datos en el propio dispositivo).

### Compilación y depuración desde terminal

El proyecto incluye un script `run-debug.sh` para facilitar la compilación e instalación:

```bash
# Compilar, instalar y ejecutar la app en modo debug
./run-debug.sh
```

Este script:
1. Compila la aplicación (`./gradlew assembleDebug`)
2. Instala en el dispositivo conectado (`./gradlew installDebug`)
3. Inicia la aplicación automáticamente

**Comandos manuales útiles:**

```bash
# Compilar APK de debug
./gradlew assembleDebug

# Compilar APK de release
./gradlew assembleRelease

# Instalar en dispositivo conectado
./gradlew installDebug

# Ver logs en tiempo real
adb logcat | grep -i asistenciaapp

# Ver dispositivos conectados
adb devices
```

**Nota:** Si `adb` no está en tu PATH, puedes encontrarlo en `~/Library/Android/sdk/platform-tools/adb` (macOS) o agregarlo a tu PATH.

## Datos de prueba (Seeders)

La aplicación incluye datos de prueba completos que se insertan automáticamente al crear la base de datos por primera vez. Estos datos facilitan las pruebas y el desarrollo sin necesidad de crear datos manualmente.

### Usuarios de prueba (10 registros)

**Alumnos (3):**
- `alumno1` / `1234` - Ana García (Registro: 211882)
- `alumno2` / `1234` - Juan Pérez (Registro: 212732)
- `alumno3` / `1234` - Carlos López (Registro: 210882)

**Docentes (5):**
- `docente1` / `1234` - Marcos Rodríguez (Registro: 342232)
- `docente2` / `1234` - Maria Fernández (Registro: 45532)
- `docente3` / `1234` - Julia Martínez (Registro: 56322)
- `docente4` / `1234` - Roberto Sánchez (Registro: 67890)
- `docente5` / `1234` - Laura González (Registro: 78901)

**Administradores (2):**
- `admin1` / `1234` - Admin Principal (Registro: 11111)
- `admin2` / `1234` - Super Admin (Registro: 22222)

### Materias de prueba (20 registros)

Incluye materias de diferentes niveles académicos:
- **Nivel 1:** Programación I, Matemática Discreta, Cálculo I, Física I, Química General
- **Nivel 2:** Programación II, Base de Datos I, Estructura de Datos, Arquitectura de Computadoras, Cálculo II, Álgebra Lineal, Física II
- **Nivel 3:** Programación III, Base de Datos II, Algoritmos y Complejidad, Sistemas Operativos, Ética Profesional
- **Nivel 4:** Redes de Computadoras, Ingeniería de Software, Gestión de Proyectos

### Grupos de prueba (20 registros)

- Varios grupos por materia (paralelos A y B)
- Diferentes docentes asignados
- Capacidades variadas (20-50 estudiantes)
- Semestre 1, Gestión 2025

### Horarios de prueba (38 registros)

- Múltiples horarios por grupo
- Distribuidos de Lunes a Viernes
- Horarios variados: 08:00-10:00, 10:00-12:00, 14:00-16:00, 16:00-18:00

### Inscripciones de prueba (21 registros)

- Los 3 alumnos inscritos en múltiples grupos
- Distribución variada para pruebas de cruces de horarios
- Fechas de inscripción: 15-19 de enero 2025

### Asistencias de prueba (34 registros)

- Registros de asistencia distribuidos en diferentes fechas
- Múltiples asistencias por alumno y grupo
- Fechas: 20-24 de enero 2025

**Nota:** Para resetear los datos de prueba en desarrollo, se puede usar el método `DatabaseSeeder.clearSeedData()`.

## Evaluación frente al diagrama genérico de 3 capas
- **Presentación:** Composables desacoplados que consumen ViewModels; la navegación por roles está bien encapsulada. Los ViewModels reciben dependencias de forma manual (oportunidad de mejora con inyección de dependencias).
- **Dominio:** Casos de uso encapsulan reglas de negocio y usan Repositories para acceder a datos, manteniendo separación de responsabilidades. ViewModels gestionan estado reactivo con `StateFlow`.
- **Datos:** Arquitectura completa con Singleton, DAOs separados por entidad, Repositories que abstraen el acceso, y separación clara entre migraciones, seeders y acceso a datos. Falta implementar manejo de errores robusto y migración a Room para mejor seguridad de threads.

## Oportunidades de mejora

### ✅ Completado
- ✅ Introducir repositorios e inyección de dependencias (p. ej. Hilt) para desacoplar casos de uso de SQLite
- ✅ Separar responsabilidades en DatabaseMigrations, DatabaseSeeder y AppDatabase
- ✅ Implementar patrón Singleton en AppDatabase
- ✅ Crear DAOs separados por entidad
- ✅ Corregir `UserSession.getUserRol()` para devolver el rol correcto
- ✅ Actualizar pantallas y ViewModels para usar la nueva arquitectura (Repositories en lugar de AppDatabase directo)
- ✅ Implementar UI State Management en todos los ViewModels (sealed classes para estados)
- ✅ Agregar validaciones en UseCases con `ValidationResult`
- ✅ Crear `Validators` utility para validaciones reutilizables

### Pendiente
- Introducir inyección de dependencias (Hilt) para eliminar dependencias manuales en ViewModels y UseCases
- Migrar `AppDatabase` a Room para ganar seguridad en el acceso a datos y migraciones automáticas
- Aplicar encriptado/Hash a contraseñas y separar datos sensibles de la app cliente
- Completar la capa remota mediante Retrofit, sincronizando datos locales/remotos y habilitando pruebas unitarias con fuentes simuladas
- Añadir pruebas instrumentadas de navegación y validación de flujos críticos (login, inscripción, asistencia)
- Implementar vista del docente con funcionalidades específicas
- Mejorar diseños de pantallas con Material Design 3 más completo
- Agregar manejo de errores más robusto con retry y logging


