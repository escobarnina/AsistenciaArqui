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
- **Dominio (`domain/`):** Modelos de negocio y casos de uso (`UsuarioCU`, `MateriaCU`, etc.) que orquestan la lógica.
- **Datos (`data/`):** `AppDatabase` expone consultas y comandos SQL; `UserSession` maneja sesión en `SharedPreferences`. Existen paquetes `remote` y `repository` vacíos listos para futuras integraciones.

## Composición del proyecto

### Estructura de directorios

```
app/src/main/java/com/bo/asistenciaapp/
├── MainActivity.kt                    # Punto de entrada de la aplicación
├── data/                              # CAPA DE DATOS
│   └── local/
│       ├── AppDatabase.kt            # SQLite (SQLiteOpenHelper)
│       ├── UserSession.kt            # SharedPreferences para sesión
│       └── StringRange.kt            # Utilidades
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

### Capa de Datos (`data/local/`)

**Responsabilidad:** Persistencia y acceso a datos.

#### `AppDatabase.kt` (SQLiteOpenHelper)
- Base de datos SQLite: `asistenciadb.db` (versión 16)
- Tablas principales:
  - `usuarios`: id, nombres, apellidos, username, contraseña, registro, rol
  - `materias`: id, nombre, sigla (única), nivel
  - `grupos`: id, materia_id, materia_nombre, docente_id, docente_nombre, paralelo
  - `horarios`: id, grupo_id, dia_semana, hora_inicio, hora_fin
  - `inscripciones`: id, alumno_id, grupo_id, semestre, año
  - `asistencias`: id, inscripcion_id, fecha, estado
  - `boletas`: id, inscripcion_id, nota_final
- Métodos CRUD directos (sin abstracción Repository)
- Inicializa con datos de prueba al crear la base de datos

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
Encapsulan la lógica de negocio y orquestan operaciones:
- **`UsuarioCU`**: `obtenerUsuarios()`, `obtenerDocentes()`, `agregarUsuario()`
- **`MateriaCU`**: Operaciones CRUD de materias
- **`GrupoCU`**: Operaciones CRUD de grupos
- **`HorarioCU`**: Operaciones CRUD de horarios
- **`InscripcionCU`**: Gestionar inscripciones de alumnos
- **`AsistenciaCU`**: Gestionar asistencias

**Nota:** Los casos de uso dependen directamente de `AppDatabase` (sin capa Repository).

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
   - Valida credenciales contra `AppDatabase`
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
│  DOMINIO (ViewModels + Casos de Uso)                     │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ VMUsuario    │  │ UsuarioCU    │                      │
│  │ VMGrupo      │  │ GrupoCU      │                      │
│  │ VMMateria    │  │ MateriaCU    │                      │
│  └──────┬───────┘  └──────┬───────┘                      │
│         │                 │                               │
│         └─────────────────┘                               │
└───────────────────────────┼──────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│  DATOS (AppDatabase + UserSession)                      │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ AppDatabase  │  │ UserSession │                      │
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

1. **Sin Repository Pattern:** Los casos de uso acceden directamente a `AppDatabase`
2. **Sin inyección de dependencias:** Dependencias manuales en ViewModels
3. **SQLite directo:** No usa Room (usa `SQLiteOpenHelper`)
4. **Retrofit configurado:** Preparado pero sin endpoints implementados
5. **Navegación por roles:** `AppNavHost` redirige según rol guardado
6. **Estado reactivo:** ViewModels usan `StateFlow` para actualizar UI

## Flujo del sistema
1. **Inicio:** `MainActivity` carga `AppNavHost`.
2. **Autenticación:** `LoginScreen` valida usuario/contraseña en `AppDatabase` y persiste la sesión.
3. **Ruteo por rol:** Según rol almacenado, la navegación dirige a `AdminHome`, `AlumnoHome` o futuras pantallas de docente.
4. **Gestión de datos:** Las pantallas invocan casos de uso y métodos de `AppDatabase` para listar, crear o actualizar entidades.
5. **Persistencia de cambios:** Las operaciones insertan/actualizan filas en SQLite y refrescan los `StateFlow` de los ViewModels correspondientes.

## Patrones de diseño recomendados para añadir
- **Singleton (Capa de Datos):** Exponer `AppDatabase` como instancia única inyectada para evitar recreaciones múltiples y facilitar pruebas.
- **Repository (Capa de Dominio ↔ Datos):** Implementar clases en `data/repository` que abstraigan el origen de datos (local/remote) y permitan a los casos de uso trabajar con interfaces; simplifica migrar a Room o a servicios REST reales.

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

## Evaluación frente al diagrama genérico de 3 capas
- **Presentación:** Composables desacoplados que consumen ViewModels; la navegación por roles está bien encapsulada, aunque los ViewModels reciben dependencias de forma manual.
- **Dominio:** Casos de uso encapsulan reglas pero aún dependen directamente de `AppDatabase`, lo que mezcla responsabilidades.
- **Datos:** `AppDatabase` concentra consultas SQL, pero falta abstracción para fuentes remotas y manejo de errores/threads.

## Oportunidades de mejora
- Corregir `UserSession.getUserRol()` para devolver el rol y no el nombre almacenado.
- Introducir repositorios e inyección de dependencias (p. ej. Hilt) para desacoplar casos de uso de SQLite.
- Migrar `AppDatabase` a Room para ganar seguridad en el acceso a datos y migraciones automáticas.
- Aplicar encriptado/Hash a contraseñas y separar datos sensibles de la app cliente.
- Completar la capa remota mediante Retrofit, sincronizando datos locales/remotos y habilitando pruebas unitarias con fuentes simuladas.
- Añadir pruebas instrumentadas de navegación y validación de flujos críticos (login, inscripción, asistencia).


