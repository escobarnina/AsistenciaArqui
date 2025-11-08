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


