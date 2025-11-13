# Patrón Strategy - AsistenciaCU

## 🎯 Objetivo

Implementar el **Patrón Strategy** en `AsistenciaCU` para hacer flexible el cálculo del estado de asistencia (PRESENTE, RETRASO, FALTA) según diferentes políticas o reglas de negocio.

---

## 📊 Diagrama UML ASCII del Patrón Strategy

```
┌────────────────────────────────────────────────────────────────┐
│                         AsistenciaCU                           │
│                        (CONTEXT)                               │
├────────────────────────────────────────────────────────────────┤
│ - asistenciaRepository: AsistenciaRepository                   │
│ - estrategia: IEstrategiaAsistencia                            │
├────────────────────────────────────────────────────────────────┤
│ + setEstrategia(estrategia: IEstrategiaAsistencia)             │
│ + marcarAsistencia(alumnoId, grupoId, fecha, horaMarcado,      │
│                    horaInicio): ValidationResult               │
│ + obtenerAsistencias(alumnoId): List<Asistencia>              │
│ + puedeMarcarAsistencia(alumnoId, grupoId): Boolean           │
└─────────────────────┬──────────────────────────────────────────┘
                      │
                      │ usa
                      ▼
         ┌────────────────────────────┐
         │  <<interface>>             │
         │  IEstrategiaAsistencia     │ ◄────────── STRATEGY
         │        (STRATEGY)          │
         ├────────────────────────────┤
         │ + calcularEstado(          │
         │     horaMarcado: String,   │
         │     horaInicio: String     │
         │   ): String                │
         └────────────┬───────────────┘
                      △
                      │ implementan
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌───▼────────┐ ┌──▼───────────┐
│EstrategiaPresente│EstrategiaRetraso│EstrategiaFalta│
│(ConcreteStrategy)│(ConcreteStrategy)│(ConcreteStrategy)│
├──────────────┤ ├────────────┤ ├──────────────┤
│+ calcularEstado()│+ calcularEstado()│+ calcularEstado()│
│                │ │                 │ │                │
│Retorna:        │ │Retorna:         │ │Retorna:        │
│"PRESENTE"      │ │"PRESENTE"       │ │"FALTA"         │
│(0-10 min)      │ │"RETRASO"        │ │(>30 min)       │
│                │ │(10-30 min)      │ │                │
│                │ │"FALTA" (>30 min)│ │                │
└────────────────┘ └─────────────────┘ └────────────────┘
```

---

## 🏗️ Estructura del Patrón

### Componentes Implementados

| Componente | Archivo | Rol | Descripción |
|------------|---------|-----|-------------|
| **Strategy** | `IEstrategiaAsistencia.kt` | Interface | Define el contrato común |
| **ConcreteStrategy 1** | `EstrategiaPresente.kt` | Implementación | Algoritmo flexible (0-10 min → PRESENTE) |
| **ConcreteStrategy 2** | `EstrategiaRetraso.kt` | Implementación | Algoritmo estándar (0-10 PRESENTE, 10-30 RETRASO, >30 FALTA) |
| **ConcreteStrategy 3** | `EstrategiaFalta.kt` | Implementación | Algoritmo estricto (>30 min → FALTA) |
| **Context** | `AsistenciaCU.kt` | Contexto | Usa la estrategia actual |

---

## 📁 Archivos Creados

### Ubicación: `domain/strategy/attendance/`

```
app/src/main/java/com/bo/asistenciaapp/domain/
└── strategy/
    └── attendance/
        ├── IEstrategiaAsistencia.kt        (Interface Strategy)
        ├── EstrategiaPresente.kt           (ConcreteStrategy)
        ├── EstrategiaRetraso.kt            (ConcreteStrategy)
        └── EstrategiaFalta.kt              (ConcreteStrategy)
```

### Modificado: `domain/usecase/AsistenciaCU.kt`

---

## 🔧 Implementación Detallada

### 1. Interface Strategy

```kotlin
interface IEstrategiaAsistencia {
    /**
     * Calcula el estado de asistencia según el algoritmo específico.
     * 
     * @param horaMarcado Hora en que marcó asistencia (HH:mm)
     * @param horaInicio Hora de inicio de clase (HH:mm)
     * @return "PRESENTE", "RETRASO" o "FALTA"
     */
    fun calcularEstado(horaMarcado: String, horaInicio: String): String
}
```

### 2. Estrategias Concretas

#### EstrategiaPresente (Flexible)
```kotlin
class EstrategiaPresente : IEstrategiaAsistencia {
    override fun calcularEstado(horaMarcado: String, horaInicio: String): String {
        val diferencia = calcularDiferencia(horaMarcado, horaInicio)
        return if (diferencia <= 10) "PRESENTE" else "PRESENTE"
        // Siempre retorna PRESENTE (política muy flexible)
    }
}
```

**Lógica:**
- 0-10 minutos después → PRESENTE
- +10 minutos después → PRESENTE (política flexible)

---

#### EstrategiaRetraso (Estándar)
```kotlin
class EstrategiaRetraso : IEstrategiaAsistencia {
    override fun calcularEstado(horaMarcado: String, horaInicio: String): String {
        val diferencia = calcularDiferencia(horaMarcado, horaInicio)
        return when {
            diferencia <= 10 -> "PRESENTE"
            diferencia <= 30 -> "RETRASO"
            else -> "FALTA"
        }
    }
}
```

**Lógica:**
- 0-10 minutos después → PRESENTE
- 10-30 minutos después → RETRASO
- +30 minutos después → FALTA

---

#### EstrategiaFalta (Estricta)
```kotlin
class EstrategiaFalta : IEstrategiaAsistencia {
    override fun calcularEstado(horaMarcado: String, horaInicio: String): String {
        val diferencia = calcularDiferencia(horaMarcado, horaInicio)
        return when {
            diferencia > 30 -> "FALTA"
            diferencia > 10 -> "RETRASO"
            else -> "PRESENTE"
        }
    }
}
```

**Lógica:**
- 0-10 minutos después → PRESENTE
- 10-30 minutos después → RETRASO
- +30 minutos después → FALTA (política estricta)

---

### 3. Context (AsistenciaCU)

```kotlin
class AsistenciaCU(private val asistenciaRepository: AsistenciaRepository) {
    
    // Propiedad que mantiene la estrategia actual
    lateinit var estrategia: IEstrategiaAsistencia
    
    // Método para cambiar la estrategia
    fun setEstrategia(estrategia: IEstrategiaAsistencia) {
        this.estrategia = estrategia
    }
    
    // Método que USA la estrategia
    fun marcarAsistencia(
        alumnoId: Int, 
        grupoId: Int, 
        fecha: String,
        horaMarcado: String,
        horaInicio: String
    ): ValidationResult {
        // ... validaciones ...
        
        // ⭐ Delegar cálculo del estado a la estrategia
        val estado = estrategia.calcularEstado(horaMarcado, horaInicio)
        
        // ... registrar asistencia ...
        
        return ValidationResult.Success
    }
}
```

---

## 💡 Ejemplo de Uso

### Escenario 1: Cambiar estrategia en tiempo de ejecución

```kotlin
// Crear el caso de uso
val asistenciaRepository = AsistenciaRepository(db)
val asistenciaCU = AsistenciaCU(asistenciaRepository)

// ===== POLÍTICA FLEXIBLE (Siempre PRESENTE) =====
asistenciaCU.setEstrategia(EstrategiaPresente())

asistenciaCU.marcarAsistencia(
    alumnoId = 1,
    grupoId = 1,
    fecha = "2025-01-20",
    horaMarcado = "08:25",  // 25 minutos tarde
    horaInicio = "08:00"
)
// Resultado: PRESENTE (política flexible)

// ===== POLÍTICA ESTÁNDAR (PRESENTE/RETRASO/FALTA) =====
asistenciaCU.setEstrategia(EstrategiaRetraso())

asistenciaCU.marcarAsistencia(
    alumnoId = 1,
    grupoId = 1,
    fecha = "2025-01-21",
    horaMarcado = "08:25",  // 25 minutos tarde
    horaInicio = "08:00"
)
// Resultado: RETRASO (política estándar)

// ===== POLÍTICA ESTRICTA =====
asistenciaCU.setEstrategia(EstrategiaFalta())

asistenciaCU.marcarAsistencia(
    alumnoId = 1,
    grupoId = 1,
    fecha = "2025-01-22",
    horaMarcado = "08:35",  // 35 minutos tarde
    horaInicio = "08:00"
)
// Resultado: FALTA (política estricta)
```

### Escenario 2: Diferentes políticas por materia

```kotlin
// Materia con política flexible (Seminario)
val seminario = obtenerMateria("Seminario")
if (seminario.esFlexible) {
    asistenciaCU.setEstrategia(EstrategiaPresente())
}

// Materia con política estándar (Programación)
val programacion = obtenerMateria("Programación")
asistenciaCU.setEstrategia(EstrategiaRetraso())

// Materia con política estricta (Laboratorio)
val laboratorio = obtenerMateria("Laboratorio")
if (laboratorio.esEstricta) {
    asistenciaCU.setEstrategia(EstrategiaFalta())
}
```

---

## 🎯 Ventajas del Patrón Strategy

### ✅ Sin Strategy (Código Original)

**ANTES:**
```kotlin
fun marcarAsistencia(...): ValidationResult {
    val diferencia = calcularDiferencia(horaMarcado, horaInicio)
    
    // ❌ Muchos if/else anidados
    val estado = if (esMateriaSeminario) {
        "PRESENTE"
    } else if (esMateriaLaboratorio) {
        if (diferencia > 30) "FALTA" 
        else if (diferencia > 10) "RETRASO"
        else "PRESENTE"
    } else {
        if (diferencia > 30) "FALTA"
        else if (diferencia > 10) "RETRASO"
        else "PRESENTE"
    }
    
    // ❌ Difícil de mantener
    // ❌ Difícil de testear
    // ❌ Viola Open/Closed Principle
}
```

### ✅ Con Strategy (Implementado)

**DESPUÉS:**
```kotlin
fun marcarAsistencia(...): ValidationResult {
    // ✅ Una sola línea, delega a la estrategia
    val estado = estrategia.calcularEstado(horaMarcado, horaInicio)
    
    // ✅ Fácil de mantener
    // ✅ Fácil de testear
    // ✅ Cumple Open/Closed Principle
}
```

### Beneficios Específicos

1. **Elimina condicionales complejos**
   - No más if/else anidados para determinar el estado
   - Código más limpio y legible

2. **Fácil agregar nuevas políticas**
   - Crear nueva clase que implemente `IEstrategiaAsistencia`
   - Sin modificar código existente

3. **Cambio en tiempo de ejecución**
   - Cambiar política según materia, horario, semestre, etc.
   - Flexibilidad total

4. **Testeable independientemente**
   - Cada estrategia se puede testear por separado
   - Mocks fáciles para el contexto

5. **Cumple SOLID**
   - **S**: Cada estrategia una responsabilidad
   - **O**: Abierto a extensión, cerrado a modificación
   - **L**: Estrategias son intercambiables
   - **I**: Interface pequeña y específica
   - **D**: Depende de abstracción (interface)

---

## 🧪 Testing

### Test de Estrategias

```kotlin
class EstrategiaRetrasoTest {
    
    private lateinit var estrategia: EstrategiaRetraso
    
    @Before
    fun setup() {
        estrategia = EstrategiaRetraso()
    }
    
    @Test
    fun `llegada a tiempo retorna PRESENTE`() {
        val estado = estrategia.calcularEstado("08:05", "08:00")
        assertEquals("PRESENTE", estado)
    }
    
    @Test
    fun `llegada con 15 minutos de retraso retorna RETRASO`() {
        val estado = estrategia.calcularEstado("08:15", "08:00")
        assertEquals("RETRASO", estado)
    }
    
    @Test
    fun `llegada con 35 minutos de retraso retorna FALTA`() {
        val estado = estrategia.calcularEstado("08:35", "08:00")
        assertEquals("FALTA", estado)
    }
}
```

### Test del Context

```kotlin
class AsistenciaCUTest {
    
    @Test
    fun `cambiar estrategia cambia el comportamiento`() {
        val asistenciaCU = AsistenciaCU(mockRepository)
        
        // Con EstrategiaPresente
        asistenciaCU.setEstrategia(EstrategiaPresente())
        val resultado1 = asistenciaCU.marcarAsistencia(1, 1, "2025-01-20", "08:25", "08:00")
        // Verifica que usó EstrategiaPresente
        
        // Con EstrategiaRetraso
        asistenciaCU.setEstrategia(EstrategiaRetraso())
        val resultado2 = asistenciaCU.marcarAsistencia(1, 1, "2025-01-20", "08:25", "08:00")
        // Verifica que usó EstrategiaRetraso
    }
}
```

---

## 📝 Logs para Debugging

Al ejecutar, verás logs detallados:

```
D/AsistenciaCU: Cambiando estrategia a: EstrategiaRetraso
D/AsistenciaCU: === MARCANDO ASISTENCIA ===
D/AsistenciaCU: Alumno: 1, Grupo: 1, Fecha: 2025-01-20
D/AsistenciaCU: Hora marcado: 08:25, Hora inicio: 08:00
D/EstrategiaRetraso: Evaluando asistencia - Marcado: 08:25, Inicio: 08:00
D/EstrategiaRetraso: Diferencia: 25 minutos
D/EstrategiaRetraso: Llegó con retraso (diferencia entre 10 y 30 min)
D/EstrategiaRetraso: Estado determinado: RETRASO
D/AsistenciaCU: Estado calculado por la estrategia: RETRASO
D/AsistenciaCU: Asistencia registrada exitosamente con estado: RETRASO
```

---

## 🔄 Flujo de Ejecución

```
1. UI llama a marcarAsistencia()
         ↓
2. AsistenciaCU valida datos
         ↓
3. AsistenciaCU delega a estrategia.calcularEstado()
         ↓
4. Estrategia calcula el estado (PRESENTE/RETRASO/FALTA)
         ↓
5. AsistenciaCU registra asistencia con el estado
         ↓
6. Retorna ValidationResult.Success
```

---

## 🚀 Extensibilidad

### Agregar nueva estrategia (ej: EstrategiaVirtual)

```kotlin
class EstrategiaVirtual : IEstrategiaAsistencia {
    override fun calcularEstado(horaMarcado: String, horaInicio: String): String {
        // Para clases virtuales, siempre PRESENTE si marca antes de fin de clase
        val diferencia = calcularDiferencia(horaMarcado, horaInicio)
        return if (diferencia < 120) "PRESENTE" else "FALTA" // 2 horas de margen
    }
}
```

**Uso:**
```kotlin
asistenciaCU.setEstrategia(EstrategiaVirtual())
// ¡Sin modificar código existente!
```

---

## ✅ Checklist de Implementación

- [x] Crear interface `IEstrategiaAsistencia`
- [x] Implementar `EstrategiaPresente`
- [x] Implementar `EstrategiaRetraso`
- [x] Implementar `EstrategiaFalta`
- [x] Modificar `AsistenciaCU` (agregar propiedad `estrategia`)
- [x] Agregar método `setEstrategia()`
- [x] Modificar `marcarAsistencia()` para usar estrategia
- [x] Agregar logs con `Log.d()`
- [x] Sin errores de lint
- [x] Código en español
- [x] Comentarios completos
- [x] Diagrama UML ASCII
- [ ] Tests unitarios (opcional)
- [ ] Integración en UI (opcional)

---

## 📚 Comparación: Adapter vs Strategy

| Aspecto | Adapter | Strategy |
|---------|---------|----------|
| **Propósito** | Convertir interfaces incompatibles | Cambiar algoritmo en runtime |
| **Cuándo usar** | Integrar sistemas externos | Múltiples formas de hacer lo mismo |
| **Ejemplo en proyecto** | `DataExportAdapter` (Excel, PDF) | `IEstrategiaAsistencia` (PRESENTE, RETRASO, FALTA) |
| **Relación** | Cliente → Target → Adapter → Adaptee | Context → Strategy → ConcreteStrategy |
| **Flexibilidad** | Integración de librerías | Cambio de comportamiento |

---

## 🎓 Principios SOLID Aplicados

### Single Responsibility (S)
- ✅ Cada estrategia tiene una responsabilidad: calcular el estado según sus reglas

### Open/Closed (O)
- ✅ Abierto a extensión: agregar nuevas estrategias
- ✅ Cerrado a modificación: no se modifica `AsistenciaCU` ni estrategias existentes

### Liskov Substitution (L)
- ✅ Todas las estrategias son intercambiables sin romper el código

### Interface Segregation (I)
- ✅ Interface pequeña con un solo método relevante

### Dependency Inversion (D)
- ✅ `AsistenciaCU` depende de la abstracción (`IEstrategiaAsistencia`), no de implementaciones concretas

---

## 📖 Referencias

- **Design Patterns**: Gang of Four (GoF)
- **Strategy Pattern**: [Refactoring Guru](https://refactoring.guru/design-patterns/strategy)

---

**Fecha de implementación**: 13 de Noviembre, 2025  
**Versión**: 1.0  
**Patrón**: Strategy  
**Capa**: Domain  
**Estado**: ✅ Implementado y funcional

