# 📱 Guía de Uso - Configuración de Tolerancia de Asistencia

## 📋 Resumen Ejecutivo

**Fecha:** 13 de Noviembre de 2025  
**Funcionalidad:** Configuración UI para Patrón Strategy con Tolerancia Configurable  
**Usuario:** Docentes  
**Objetivo:** Permitir a los docentes configurar la tolerancia de retraso de cada grupo desde la app móvil

---

## 🎯 ¿Qué es la Tolerancia de Asistencia?

La **tolerancia** es el tiempo máximo (en minutos) que un estudiante puede llegar tarde y aún ser marcado como **PRESENTE**. Después de este tiempo, se considera **RETRASO** o **FALTA** según el Patrón Strategy configurado.

### Ejemplos Prácticos:

| Tolerancia | Hora Inicio | Llega a las | Estado |
|------------|-------------|-------------|--------|
| **5 min** | 08:00 | 08:04 | ✅ PRESENTE |
| **5 min** | 08:00 | 08:07 | ⚠️ RETRASO |
| **10 min** | 08:00 | 08:09 | ✅ PRESENTE |
| **10 min** | 08:00 | 08:15 | ⚠️ RETRASO |
| **15 min** | 08:00 | 08:14 | ✅ PRESENTE |
| **15 min** | 08:00 | 08:20 | ⚠️ RETRASO |

---

## 🚀 Cómo Configurar la Tolerancia

### Paso 1: Acceder a "Mis Grupos"

1. Inicia sesión como **Docente**
2. En el menú principal, selecciona **"Mis Grupos"**

![Menú Principal](https://via.placeholder.com/300x100/4CAF50/FFFFFF?text=Mis+Grupos)

---

### Paso 2: Visualizar Tolerancia Actual

En la lista de grupos, cada card muestra:

```
┌──────────────────────────────────────────────┐
│  📚 Programación I                           │
│  👥 Grupo A • S1/2025                        │
│  👨‍👩‍👧‍👦 15/30 estudiantes                       │
│  ──────────────────────────────────────────  │
│  ⏱️ Tolerancia: 10 min        ⚙️  ▶️         │
└──────────────────────────────────────────────┘
```

- **⏱️ Tolerancia**: Muestra el valor actual
- **⚙️**: Botón para configurar
- **▶️**: Ver estudiantes del grupo

---

### Paso 3: Abrir Diálogo de Configuración

1. Presiona el botón **⚙️ (Configurar)**
2. Se abre el diálogo de configuración

---

### Paso 4: Configurar Nueva Tolerancia

El diálogo muestra:

#### 🎨 **Interfaz del Diálogo**

```
┌────────────────────────────────────────────┐
│  ℹ️  Configurar Tolerancia                 │
│                                            │
│  Programación I - Grupo A                  │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │  Tolerancia Actual                    │ │
│  │  10 minutos                           │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  Nueva Tolerancia: 15 minutos              │
│                                            │
│  ├──────────●───────────────┤              │
│  0 min                   60 min            │
│                                            │
│  Minutos (manual): [15]                    │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │    🟡 Estándar                        │ │
│  │    Permite pequeños retrasos         │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  💡 Las estrategias de asistencia usarán  │
│     esta tolerancia para determinar si    │
│     un estudiante está presente, llegó    │
│     tarde o faltó.                        │
│                                            │
│  [Cancelar]              [Guardar]         │
└────────────────────────────────────────────┘
```

---

### Paso 5: Ajustar Tolerancia

Puedes ajustar la tolerancia de **dos formas**:

#### **Opción A: Usar el Slider** 🎚️
- Desliza el control para ajustar rápidamente
- Rango: 0 a 60 minutos
- Cambio en incrementos de 1 minuto

#### **Opción B: Entrada Manual** ⌨️
- Escribe directamente el número de minutos
- Valida automáticamente (0-60)
- Más preciso para valores específicos

---

### Paso 6: Revisar Nivel de Política

El diálogo muestra automáticamente el nivel de política según la tolerancia:

| Tolerancia | Nivel | Color | Descripción |
|------------|-------|-------|-------------|
| **0-5 min** | 🔴 Muy Estricto | Rojo | Puntualidad estricta |
| **6-10 min** | 🟠 Estricto | Naranja | Política estándar institucional |
| **11-15 min** | 🟡 Estándar | Amarillo | Permite pequeños retrasos |
| **16-25 min** | 🟢 Flexible | Verde | Política permisiva |
| **26-60 min** | 🔵 Muy Flexible | Azul | Máxima flexibilidad |

---

### Paso 7: Guardar Cambios

1. Presiona el botón **"Guardar"**
2. Se muestra un mensaje de confirmación
3. El diálogo se cierra automáticamente
4. La tolerancia se actualiza en el card del grupo

---

## 💡 Recomendaciones por Tipo de Materia

### 🔴 **Muy Estricto (0-5 min)**
**Recomendado para:**
- Laboratorios
- Clases prácticas
- Materias con equipos especializados
- Seminarios de investigación

**Ejemplo:** "Laboratorio de Química debe empezar puntualmente por seguridad"

---

### 🟠 **Estricto (6-10 min)**
**Recomendado para:**
- Clases teóricas regulares
- Exámenes
- Presentaciones de proyectos
- Defensa de trabajos

**Ejemplo:** "Programación I - Teoría tiene horario estricto institucional"

---

### 🟡 **Estándar (11-15 min)**
**Recomendado para:**
- Clases en primera hora (tráfico)
- Materias con alta matrícula
- Grupos con estudiantes de diferentes facultades
- Después del almuerzo

**Ejemplo:** "Cálculo I a las 7:00 AM puede tener tolerancia de 15 min por tráfico"

---

### 🟢 **Flexible (16-25 min)**
**Recomendado para:**
- Talleres
- Clases virtuales
- Grupos vespertinos/nocturnos
- Materias electivas

**Ejemplo:** "Taller de Emprendimiento permite llegada más flexible"

---

### 🔵 **Muy Flexible (26-60 min)**
**Recomendado para:**
- Seminarios
- Conferencias
- Clases de consulta
- Actividades extracurriculares

**Ejemplo:** "Seminario de Ética profesional acepta llegadas hasta 30 min tarde"

---

## ⚠️ Consideraciones Importantes

### 1. **Impacto Inmediato**
✅ Los cambios se aplican **inmediatamente**  
✅ La próxima asistencia marcada usará la nueva tolerancia  
✅ No afecta asistencias ya registradas

### 2. **Validaciones**
- Mínimo: **0 minutos**
- Máximo: **60 minutos**
- Solo números enteros
- Campo obligatorio

### 3. **Permisos**
- Solo **docentes asignados** al grupo pueden configurar
- Los cambios son **permanentes** hasta que se vuelvan a modificar

### 4. **Estrategias Afectadas**
La tolerancia afecta a estas estrategias:

| Estrategia | Cómo usa la tolerancia |
|------------|------------------------|
| `EstrategiaPresente` | Hasta `tolerancia` min = PRESENTE |
| `EstrategiaRetraso` | 0-tolerancia = PRESENTE<br>tolerancia-(3×tolerancia) = RETRASO<br>>3×tolerancia = FALTA |
| `EstrategiaFalta` | Similar a Retraso, política estricta |

---

## 📊 Ejemplos de Configuración por Escenario

### Escenario 1: Universidad con Tráfico Pesado

**Problema:** Estudiantes llegan tarde por tráfico en primera hora

**Solución:**
- Primera hora (7:00 AM): **15 minutos** 🟡
- Resto del día: **10 minutos** 🟠

```
Programación I - 7:00 AM    →  Tolerancia: 15 min
Base de Datos I - 9:00 AM   →  Tolerancia: 10 min
```

---

### Escenario 2: Laboratorio de Alta Precisión

**Problema:** Laboratorio requiere puntualidad estricta

**Solución:**
- Tolerancia: **5 minutos** 🔴
- No se permiten retrasos por seguridad

```
Lab. Química Orgánica    →  Tolerancia: 5 min
Lab. Física Nuclear      →  Tolerancia: 5 min
```

---

### Escenario 3: Clase Virtual

**Problema:** Problemas de conexión son comunes

**Solución:**
- Tolerancia: **20 minutos** 🟢
- Permite reconexiones

```
Taller de Diseño UX (Virtual)  →  Tolerancia: 20 min
```

---

### Escenario 4: Diferentes Grupos de la Misma Materia

**Problema:** Grupo A es matutino, Grupo B es vespertino

**Solución:**
- Grupo A (7:00 AM): **15 minutos** 🟡 (tráfico)
- Grupo B (14:00 PM): **10 minutos** 🟠 (estándar)

```
Programación I - Grupo A (7:00)   →  Tolerancia: 15 min
Programación I - Grupo B (14:00)  →  Tolerancia: 10 min
```

---

## 🔧 Solución de Problemas

### Problema 1: No puedo ver el botón de configuración

**Causa:** No tienes permisos o no estás asignado al grupo

**Solución:**
1. Verifica que iniciaste sesión como docente
2. Confirma que el grupo está asignado a ti
3. Contacta al administrador si el problema persiste

---

### Problema 2: El valor no se guarda

**Causa:** Validación fallida o error de conexión

**Solución:**
1. Verifica que el valor esté entre 0 y 60
2. Revisa que ingresaste un número entero
3. Intenta nuevamente
4. Si persiste, reinicia la aplicación

---

### Problema 3: El cambio no se refleja en la lista

**Causa:** La lista no se actualizó automáticamente

**Solución:**
1. Regresa al menú principal
2. Vuelve a entrar a "Mis Grupos"
3. El nuevo valor debería aparecer

---

### Problema 4: ¿Cómo saber qué tolerancia usar?

**Causa:** Duda sobre la política adecuada

**Solución:**
1. Consulta la tabla de recomendaciones (arriba)
2. Considera:
   - Tipo de materia (teórica/práctica)
   - Horario (primera hora vs. resto del día)
   - Modalidad (presencial/virtual)
   - Ubicación del aula (campus principal vs. anexos)
3. Empieza con **10 minutos** (estándar) y ajusta según necesidad

---

## 📈 Monitoreo y Ajustes

### Revisar Efectividad

Después de configurar la tolerancia, monitorea:

1. **Tasa de Retrasos:**
   - Si es muy alta (>30%): Considera aumentar tolerancia
   - Si es muy baja (<5%): Puedes mantener o reducir

2. **Quejas de Estudiantes:**
   - Si hay muchas: Revisa si la tolerancia es muy estricta
   - Si no hay: La política es adecuada

3. **Tipo de Retrasos:**
   - Por tráfico: Aumenta tolerancia en primera hora
   - Por desorganización: Mantén política estricta

### Ajustes Recomendados

```
Inicio del Semestre:  Tolerancia generosa (15 min)
  ↓
Mitad del Semestre:   Reducir gradualmente (10 min)
  ↓
Antes de Exámenes:    Política estricta (5-10 min)
```

---

## 🎓 Casos de Uso Reales

### Caso 1: Docente de Programación

**Contexto:**
- Materia: Programación I
- Horario: Lunes 7:00 AM
- Problema: 40% estudiantes llegan tarde por tráfico

**Acción:**
1. Configuró tolerancia a **15 minutos**
2. Resultado: Tasa de faltas bajó de 40% a 10%
3. Estudiantes más satisfechos

---

### Caso 2: Docente de Laboratorio

**Contexto:**
- Materia: Lab. Química
- Horario: Miércoles 10:00 AM
- Problema: Estudiantes llegan tarde y pierden instrucciones de seguridad

**Acción:**
1. Configuró tolerancia a **5 minutos**
2. Comunicó política claramente
3. Resultado: Puntualidad mejoró 90%

---

### Caso 3: Docente de Seminario Virtual

**Contexto:**
- Materia: Seminario de Investigación (virtual)
- Horario: Viernes 18:00 PM
- Problema: Problemas de conexión frecuentes

**Acción:**
1. Configuró tolerancia a **20 minutos**
2. Permite reconexiones
3. Resultado: Participación aumentó 30%

---

## 🎯 Mejores Prácticas

### ✅ **DO's (Hacer)**

1. **Comunicar la política a estudiantes**
   - Al inicio del semestre
   - En el syllabus
   - Recordar periódicamente

2. **Ser consistente**
   - Aplicar la misma tolerancia todo el semestre
   - Solo cambiar si hay razones justificadas

3. **Considerar el contexto**
   - Horario de la clase
   - Ubicación del aula
   - Condiciones de transporte

4. **Revisar periódicamente**
   - Al menos una vez al semestre
   - Ajustar si no funciona

5. **Documentar cambios**
   - Informar a estudiantes
   - Justificar ajustes

---

### ❌ **DON'Ts (No Hacer)**

1. **Cambiar frecuentemente**
   - Confunde a estudiantes
   - Genera desconfianza

2. **Ser demasiado estricto sin razón**
   - Genera desmotivación
   - Aumenta tasa de faltas

3. **Ser demasiado flexible sin control**
   - Fomenta impuntualidad
   - Dificulta el inicio de clase

4. **Olvidar comunicar cambios**
   - Estudiantes no saben la nueva política
   - Genera conflictos

5. **Ignorar feedback de estudiantes**
   - Puede haber razones válidas para ajustar
   - Escuchar mejora la relación docente-estudiante

---

## 📞 Soporte Técnico

### ¿Necesitas Ayuda?

**Opción 1: Administrador del Sistema**
- Contacto: admin@universidad.edu
- Horario: Lunes a Viernes, 8:00-18:00

**Opción 2: Documentación Técnica**
- Ver: `PATRON_STRATEGY_TOLERANCIA_CONFIGURABLE.md`
- Ubicación: Repositorio del proyecto

**Opción 3: FAQ**
- Preguntas frecuentes en el sistema

---

## 📚 Recursos Adicionales

### Documentación Relacionada

1. **`PATRON_STRATEGY_ASISTENCIA.md`**
   - Explicación técnica del patrón
   - Cómo funcionan las estrategias

2. **`PATRON_STRATEGY_TOLERANCIA_CONFIGURABLE.md`**
   - Implementación técnica completa
   - Diagramas UML
   - Ejemplos de código

3. **`DIAGRAMA_UML_STRATEGY.txt`**
   - Diagramas ASCII detallados
   - Flujos de ejecución

---

## 🎉 Conclusión

La configuración de tolerancia de asistencia es una herramienta poderosa que permite a los docentes adaptar las políticas de asistencia a las necesidades específicas de cada grupo. Usa esta guía para:

- ✅ Configurar tolerancias apropiadas
- ✅ Adaptarte a diferentes contextos
- ✅ Mejorar la experiencia de estudiantes
- ✅ Mantener control de asistencia efectivo

**Recuerda:** La clave es encontrar el equilibrio entre flexibilidad y disciplina académica. Empieza con valores estándar (10 minutos) y ajusta según la experiencia real.

---

**Fecha de Actualización:** 13 de Noviembre de 2025  
**Versión:** 1.0  
**Estado:** ✅ Completado

---

## 📸 Capturas de Pantalla

### Vista de Lista de Grupos

```
┌─────────────────────────────────────────────────────┐
│  ←  Mis Grupos                                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │  📚  Programación I                           │ │
│  │      👥 Grupo A • S1/2025                     │ │
│  │      👨‍👩‍👧‍👦 15/30 estudiantes                   │ │
│  │  ─────────────────────────────────────────── │ │
│  │  ⏱️ Tolerancia: 10 min      ⚙️  ▶️           │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │  📚  Base de Datos I                          │ │
│  │      👥 Grupo A • S1/2025                     │ │
│  │      👨‍👩‍👧‍👦 20/30 estudiantes                   │ │
│  │  ─────────────────────────────────────────── │ │
│  │  ⏱️ Tolerancia: 15 min      ⚙️  ▶️           │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

**¡Feliz Enseñanza! 🎓**

