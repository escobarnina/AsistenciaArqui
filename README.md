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
El proyecto sigue una estructura en capas que se alinea con un diagrama genérico de tres capas:

```
┌───────────────────────────────┐
│ Capa de Presentación          │  Screens Compose, NavHost, temas
├───────────────▲───────────────┤
│ Capa de Dominio               │  Modelos, Casos de uso, ViewModels
├───────────────▲───────────────┤
│ Capa de Datos  ▼              │  SQLite (AppDatabase), UserSession
└───────────────────────────────┘
```

- **Presentación (`presentation/`, `ui/`):** Composables para login, dashboards y formularios de administración/alumno. Usa `AppNavHost` para dirigir según rol.
- **Dominio (`domain/`):** Modelos de negocio y casos de uso (`UsuarioCU`, `MateriaCU`, etc.) que orquestan la lógica usando Repositories.
- **Datos (`data/`):** Arquitectura completa con Singleton (`AppDatabase`), DAOs por entidad, Repositories que abstraen el acceso a datos, y `UserSession` para manejo de sesión en `SharedPreferences`.

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
│   └── viewmodel/                    # ViewModels (estado UI)
│       ├── VMUsuario.kt
│       ├── VMMateria.kt
│       ├── VMGrupo.kt
│       ├── VMHorario.kt
│       ├── VMInscripcion.kt
│       └── VMAsistencia.kt
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
Encapsulan la lógica de negocio y orquestan operaciones usando Repositories:
- **`UsuarioCU`**: `validarUsuario()`, `obtenerUsuarios()`, `obtenerDocentes()`, `agregarUsuario()`, `eliminarUsuario()`, `actualizarUsuario()`
- **`MateriaCU`**: `obtenerMaterias()`, `agregarMateria()`, `eliminarMateria()`
- **`GrupoCU`**: `obtenerGrupos()`, `agregarGrupo()`, `eliminarGrupo()`
- **`HorarioCU`**: `obtenerHorarios()`, `agregarHorario()`, `eliminarHorario()`
- **`InscripcionCU`**: `obtenerInscripciones()`, `agregarInscripcion()`, `tieneCruceDeHorario()`
- **`AsistenciaCU`**: `obtenerAsistencias()`, `marcarAsistencia()`, `puedeMarcarAsistencia()`

**Arquitectura:** Los casos de uso usan Repositories, que a su vez usan DAOs, siguiendo el patrón Repository.

#### ViewModels (`domain/viewmodel/`)
Gestionan el estado de la UI y exponen datos reactivos:
- **`VMUsuario`**, **`VMMateria`**, **`VMGrupo`**, **`VMHorario`**, **`VMInscripcion`**, **`VMAsistencia`**
- Usan `StateFlow` para exponer estado reactivo a la UI
- Ejecutan casos de uso en corutinas (`viewModelScope`)
- **Problema actual:** Dependencias manuales (sin inyección de dependencias)

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

1. **`LoginScreen.kt`**
   - Valida credenciales usando `UsuarioRepository` → `UsuarioDao` → `AppDatabase`
   - Guarda sesión en `UserSession` al autenticar
   - Redirige según rol del usuario

2. **`AdminHomeScreen.kt`**
   - Dashboard principal del administrador
   - Navegación a todas las pantallas de gestión
   - Opción de logout

3. **`GestionarUsuariosScreen.kt`** (PUsuario.kt)
   - CRUD completo de usuarios
   - Usa `VMUsuario` y `UsuarioCU`
   - Permite crear, editar y eliminar usuarios

4. **`GestionarMateriasScreen.kt`** (PMateria.kt)
   - CRUD completo de materias
   - Validación de siglas únicas

5. **`GestionarGruposScreen.kt`** (PGrupo.kt)
   - CRUD completo de grupos
   - Relaciona materias con docentes

6. **`GestionarHorarios.kt`** (PHorario.kt)
   - CRUD completo de horarios
   - Asigna horarios a grupos

7. **`AlumnoHomeScreen.kt`**
   - Dashboard principal del alumno
   - Acceso a sus inscripciones y asistencias

8. **`GestionarInscripciones.kt`**
   - Alumno puede ver y gestionar sus inscripciones
   - Filtrado por semestre y año

9. **`GestionarAsistenca.kt`**
   - Alumno puede ver sus asistencias registradas

### Flujo de datos

```
┌─────────────────────────────────────────────────────────┐
│  PRESENTACIÓN (Compose Screens)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │ LoginScreen  │  │ AdminHome   │  │ AlumnoHome  │    │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │
│         │                 │                  │            │
│         └─────────────────┼──────────────────┘            │
│                           │                              │
│                    AppNavHost                            │
└───────────────────────────┼──────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│  DOMINIO - ViewModels                                    │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ VMUsuario    │  │ VMMateria    │                      │
│  │ VMGrupo      │  │ VMHorario    │                      │
│  └──────┬───────┘  └──────┬───────┘                      │
└─────────┼─────────────────┼────────────────────────────┘
          │                 │
          ▼                 ▼
┌─────────────────────────────────────────────────────────┐
│  DOMINIO - Casos de Uso                                  │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ UsuarioCU    │  │ MateriaCU    │                      │
│  │ GrupoCU      │  │ HorarioCU    │                      │
│  └──────┬───────┘  └──────┬───────┘                      │
└─────────┼─────────────────┼────────────────────────────┘
          │                 │
          ▼                 ▼
┌─────────────────────────────────────────────────────────┐
│  DATOS - Repositories                                    │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ UsuarioRepo  │  │ MateriaRepo  │                      │
│  │ GrupoRepo    │  │ HorarioRepo  │                      │
│  └──────┬───────┘  └──────┬───────┘                      │
└─────────┼─────────────────┼────────────────────────────┘
          │                 │
          ▼                 ▼
┌─────────────────────────────────────────────────────────┐
│  DATOS - DAOs                                            │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ UsuarioDao   │  │ MateriaDao   │                      │
│  │ GrupoDao     │  │ HorarioDao   │                      │
│  └──────┬───────┘  └──────┬───────┘                      │
└─────────┼─────────────────┼────────────────────────────┘
          │                 │
          ▼                 ▼
┌─────────────────────────────────────────────────────────┐
│  DATOS - AppDatabase (Singleton)                        │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ AppDatabase   │  │ UserSession │                      │
│  │ (SQLite)     │  │ (SharedPref)│                      │
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
1. **Inicio:** `MainActivity` carga `AppNavHost`.
2. **Autenticación:** `LoginScreen` valida usuario/contraseña usando `UsuarioRepository` → `UsuarioDao` → `AppDatabase` y persiste la sesión en `UserSession`.
3. **Ruteo por rol:** Según rol almacenado, la navegación dirige a `AdminHome`, `AlumnoHome` o futuras pantallas de docente.
4. **Gestión de datos:** Las pantallas invocan ViewModels → UseCases → Repositories → DAOs → `AppDatabase` para listar, crear o actualizar entidades.
5. **Persistencia de cambios:** Las operaciones insertan/actualizan filas en SQLite a través de los DAOs y refrescan los `StateFlow` de los ViewModels correspondientes.
6. **Inicialización de datos:** Al crear la base de datos por primera vez, `DatabaseSeeder` inserta automáticamente datos de prueba (10 usuarios, 20 materias, 20 grupos, 38 horarios, 21 inscripciones, 34 asistencias).

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
- ✅ **Completado:** Introducir repositorios e inyección de dependencias (p. ej. Hilt) para desacoplar casos de uso de SQLite.
- ✅ **Completado:** Separar responsabilidades en DatabaseMigrations, DatabaseSeeder y AppDatabase.
- ✅ **Completado:** Implementar patrón Singleton en AppDatabase.
- ✅ **Completado:** Crear DAOs separados por entidad.
- Corregir `UserSession.getUserRol()` para devolver el rol y no el nombre almacenado.
- Introducir inyección de dependencias (Hilt) para eliminar dependencias manuales en ViewModels.
- Migrar `AppDatabase` a Room para ganar seguridad en el acceso a datos y migraciones automáticas.
- Aplicar encriptado/Hash a contraseñas y separar datos sensibles de la app cliente.
- Completar la capa remota mediante Retrofit, sincronizando datos locales/remotos y habilitando pruebas unitarias con fuentes simuladas.
- Añadir pruebas instrumentadas de navegación y validación de flujos críticos (login, inscripción, asistencia).
- Actualizar pantallas y ViewModels para usar la nueva arquitectura (Repositories en lugar de AppDatabase directo).


