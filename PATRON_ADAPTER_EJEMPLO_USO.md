# Patrón Adapter - Ejemplo de Uso

## 📋 Archivos Creados

### 1. Interface Target (Lo que el cliente espera)
- ✅ `data/export/adapter/DataExportAdapter.kt`

### 2. Adapters Concretos (Adaptaciones específicas)
- ✅ `data/export/adapter/AsistenciaExcelAdapter.kt` - Adapta Apache POI
- ✅ `data/export/adapter/AsistenciaPDFAdapter.kt` - Adapta PdfDocument de Android

### 3. Modelo de Resultado
- ✅ `domain/model/ExportResult.kt`

### 4. UseCase (Cliente del patrón)
- ✅ `domain/usecase/ExportarAsistenciaCU.kt`

### 5. Componente UI
- ✅ `presentation/docente/ExportarAsistenciasDialog.kt`

### 6. Dependencias
- ✅ `app/build.gradle.kts` - Agregadas dependencias de Apache POI

---

## 🎯 Diagrama del Patrón Implementado

```
┌─────────────────────────┐
│  ExportarAsistenciaCU   │  ← CLIENT (no conoce implementaciones)
│  (UseCase)              │
│                         │
│  + exportar(idGrupo,    │
│       adapter)          │
└────────────┬────────────┘
             │ usa
             ▼
┌─────────────────────────┐
│  <<interface>>          │
│  DataExportAdapter<T>   │  ← TARGET (lo que el cliente espera)
│                         │
│  + exportar()           │
│  + obtenerExtension()   │
│  + obtenerTipoMime()    │
└────────────┬────────────┘
             △
             │ implementa
    ┌────────┴────────┐
    │                 │
┌───▼──────────────┐ ┌▼───────────────────┐
│ AsistenciaExcel  │ │ AsistenciaPDF      │  ← ADAPTERS
│ Adapter          │ │ Adapter            │
│                  │ │                    │
│ - Apache POI     │ │ - PdfDocument      │  ← ADAPTEES
└──────────────────┘ └────────────────────┘
```

---

## 🚀 Cómo Usar en las Pantallas Existentes

### Opción 1: Usar el botón predefinido

El uso más simple es con el componente `ExportarAsistenciasButton`:

```kotlin
// En VerGruposDocenteScreen.kt o AdminHome.kt

import com.bo.asistenciaapp.presentation.docente.ExportarAsistenciasButton
import com.bo.asistenciaapp.domain.usecase.ExportarAsistenciaCU
import com.bo.asistenciaapp.data.repository.AsistenciaRepository

@Composable
fun VerGruposDocenteScreen() {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val asistenciaRepository = AsistenciaRepository(db)
    val exportarCU = ExportarAsistenciaCU(asistenciaRepository)
    
    // ... resto de la pantalla
    
    // Agregar el botón donde se necesite
    ExportarAsistenciasButton(
        idGrupo = grupoId,
        exportarCU = exportarCU,
        modifier = Modifier.fillMaxWidth()
    )
}
```

### Opción 2: Usar el diálogo directamente

Para más control sobre cuándo mostrar el diálogo:

```kotlin
import com.bo.asistenciaapp.presentation.docente.ExportarAsistenciasDialog

@Composable
fun MiPantalla() {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val asistenciaRepository = AsistenciaRepository(db)
    val exportarCU = ExportarAsistenciaCU(asistenciaRepository)
    
    var mostrarDialogoExportar by remember { mutableStateOf(false) }
    
    // Tu UI
    Column {
        // ... otros componentes
        
        Button(onClick = { mostrarDialogoExportar = true }) {
            Text("Exportar Asistencias")
        }
    }
    
    // Mostrar diálogo cuando se requiera
    if (mostrarDialogoExportar) {
        ExportarAsistenciasDialog(
            idGrupo = grupoSeleccionado.id,
            exportarCU = exportarCU,
            onDismiss = { mostrarDialogoExportar = false }
        )
    }
}
```

---

## 📝 Ejemplo Completo: Integración en VerGruposDocenteScreen

```kotlin
package com.bo.asistenciaapp.presentation.docente

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.data.repository.AsistenciaRepository
import com.bo.asistenciaapp.domain.usecase.ExportarAsistenciaCU

@Composable
fun VerGruposDocenteScreen(
    idDocente: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Inicializar dependencias
    val db = AppDatabase.getInstance(context)
    val asistenciaRepository = AsistenciaRepository(db)
    val exportarCU = ExportarAsistenciaCU(asistenciaRepository)
    
    // Estados
    val grupos = remember { 
        db.grupoDao.obtenerPorDocente(idDocente) 
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Grupos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (grupos.isEmpty()) {
                Text("No tienes grupos asignados")
            } else {
                grupos.forEach { grupo ->
                    GrupoCard(
                        grupo = grupo,
                        exportarCU = exportarCU
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun GrupoCard(
    grupo: Grupo,
    exportarCU: ExportarAsistenciaCU
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = grupo.materiaNombre,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Grupo ${grupo.nombre}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ⭐ BOTÓN DE EXPORTACIÓN
            ExportarAsistenciasButton(
                idGrupo = grupo.id,
                exportarCU = exportarCU,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

---

## 🔧 Ejemplo: Agregar Nuevo Formato (CSV)

Para agregar un nuevo formato de exportación sin modificar código existente:

### 1. Crear el nuevo Adapter

```kotlin
// data/export/adapter/AsistenciaCSVAdapter.kt

package com.bo.asistenciaapp.data.export.adapter

import com.bo.asistenciaapp.domain.model.Asistencia

class AsistenciaCSVAdapter : DataExportAdapter<Asistencia> {
    
    override fun exportar(data: List<Asistencia>, nombreArchivo: String): ByteArray {
        val csv = StringBuilder()
        
        // Encabezados
        csv.append("ID,ID Alumno,ID Grupo,Fecha,Grupo,Materia\n")
        
        // Datos
        data.forEach { asistencia ->
            csv.append("${asistencia.id},")
            csv.append("${asistencia.alumnoId},")
            csv.append("${asistencia.grupoId},")
            csv.append("${asistencia.fecha},")
            csv.append("${asistencia.grupo},")
            csv.append("${asistencia.materiaNombre}\n")
        }
        
        return csv.toString().toByteArray(Charsets.UTF_8)
    }
    
    override fun obtenerExtension(): String = "csv"
    
    override fun obtenerTipoMime(): String = "text/csv"
    
    override fun obtenerNombreFormato(): String = "CSV"
}
```

### 2. Agregar botón en el diálogo

```kotlin
// En ExportarAsistenciasDialog.kt, agregar un nuevo botón:

// Botón CSV
BotonFormato(
    icono = Icons.Default.Description,
    titulo = "Exportar a CSV",
    descripcion = "Archivo de texto separado por comas",
    color = Color(0xFF43A047),  // Verde
    onClick = {
        formatoSeleccionado = "CSV"
        exportando = true
        coroutineScope.launch {
            exportarAsistencias(
                context = context,
                idGrupo = idGrupo,
                adapter = AsistenciaCSVAdapter(),  // ⭐ Nuevo adapter
                exportarCU = exportarCU,
                onSuccess = {
                    exportando = false
                    onDismiss()
                },
                onError = { mensaje ->
                    exportando = false
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
)
```

### ✅ Ventajas del Patrón

- **NO** modificamos `ExportarAsistenciaCU`
- **NO** modificamos `DataExportAdapter`
- **NO** modificamos los adapters existentes
- Solo agregamos código nuevo

---

## 🧪 Testing

### Test del Adapter

```kotlin
class AsistenciaExcelAdapterTest {
    
    private lateinit var adapter: AsistenciaExcelAdapter
    
    @Before
    fun setup() {
        adapter = AsistenciaExcelAdapter()
    }
    
    @Test
    fun `exportar debe generar archivo xlsx valido`() {
        // Arrange
        val asistencias = listOf(
            Asistencia(1, 1, 1, "2025-01-20", "A", "Programación I"),
            Asistencia(2, 1, 1, "2025-01-21", "A", "Programación I")
        )
        
        // Act
        val resultado = adapter.exportar(asistencias, "test")
        
        // Assert
        assertTrue(resultado.isNotEmpty())
        assertEquals("xlsx", adapter.obtenerExtension())
        assertEquals("Excel", adapter.obtenerNombreFormato())
    }
}
```

### Test del UseCase

```kotlin
class ExportarAsistenciaCUTest {
    
    @Test
    fun `exportar debe usar el adapter sin conocer su tipo`() {
        // Arrange
        val repository = mock<AsistenciaRepository>()
        val useCase = ExportarAsistenciaCU(repository)
        val adapter = mock<DataExportAdapter<Asistencia>>()
        
        whenever(repository.obtenerPorGrupo(1)).thenReturn(
            listOf(
                Asistencia(1, 1, 1, "2025-01-20", "A", "Programación I")
            )
        )
        
        whenever(adapter.exportar(any(), any())).thenReturn(byteArrayOf())
        whenever(adapter.obtenerExtension()).thenReturn("test")
        whenever(adapter.obtenerTipoMime()).thenReturn("test/test")
        whenever(adapter.obtenerNombreFormato()).thenReturn("Test")
        
        // Act
        val resultado = useCase.exportar(1, adapter)
        
        // Assert
        assertTrue(resultado is ExportResult.Success)
        verify(adapter).exportar(any(), any())
    }
}
```

---

## 📱 Permisos de Android

Para guardar archivos en Android 6.0+, agrega al `AndroidManifest.xml`:

```xml
<manifest>
    <!-- Permiso para escribir en almacenamiento externo (Android < 10) -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
    
    <!-- Android 10+ no necesita permisos para Downloads -->
</manifest>
```

---

## 🎨 Personalización

### Cambiar colores de los botones

```kotlin
// En ExportarAsistenciasDialog.kt, modificar los colores:

// Excel - Cambia de verde a azul
color = Color(0xFF2196F3)

// PDF - Cambia de rojo a naranja
color = Color(0xFFFF9800)
```

### Cambiar el nombre del archivo generado

```kotlin
// En ExportarAsistenciaCU.kt, modificar generarNombreArchivo():

private fun generarNombreArchivo(idGrupo: Int): String {
    val fecha = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    return "asistencias_grupo_${idGrupo}_$fecha"
}
// Resultado: asistencias_grupo_1_20250113.xlsx
```

---

## 🔍 Debugging

Para ver los logs de exportación:

```bash
# Ver logs en tiempo real
adb logcat | grep ExportarAsistenciaCU

# Filtrar por nivel de log
adb logcat ExportarAsistenciaCU:D *:S
```

---

## 📦 Ubicación de Archivos Exportados

Los archivos se guardan en:

```
/storage/emulated/0/Download/
    ├── asistencias_grupo_1.xlsx
    ├── asistencias_grupo_1.pdf
    └── asistencias_grupo_2.xlsx
```

En el explorador de archivos del dispositivo aparecerán en la carpeta **"Descargas"** o **"Downloads"**.

---

## ✅ Checklist de Implementación

- [x] Crear interface `DataExportAdapter`
- [x] Implementar `AsistenciaExcelAdapter`
- [x] Implementar `AsistenciaPDFAdapter`
- [x] Crear sealed class `ExportResult`
- [x] Implementar `ExportarAsistenciaCU`
- [x] Crear `ExportarAsistenciasDialog`
- [x] Agregar dependencias de Apache POI
- [ ] Integrar en `VerGruposDocenteScreen`
- [ ] Integrar en `AdminHome`
- [ ] Agregar permisos al Manifest (si es necesario)
- [ ] Probar en dispositivo real
- [ ] Crear tests unitarios

---

## 🚀 Próximos Pasos

1. **Sincronizar Gradle** para descargar las dependencias de Apache POI
2. **Integrar el botón** en las pantallas existentes
3. **Probar** la exportación en un emulador o dispositivo
4. **Agregar más formatos** si es necesario (CSV, HTML, etc.)

---

**¡El patrón Adapter está completamente implementado y listo para usar!** 🎉

