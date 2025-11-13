package com.bo.asistenciaapp.data.local

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Responsabilidad: Gestionar los datos iniciales (seeders) de la base de datos.
 * 
 * Este archivo contiene todos los datos de prueba que se insertan
 * cuando se crea la base de datos por primera vez.
 * 
 * Separado de AppDatabase para mantener clara la separación de responsabilidades:
 * - DatabaseMigrations: Estructura de la base de datos (esquema)
 * - DatabaseSeeder: Datos iniciales de prueba
 * - AppDatabase: Acceso a datos (CRUD operations)
 */
object DatabaseSeeder {
    
    private const val TAG = "DatabaseSeeder"
    
    /**
     * Inserta todos los datos de prueba en la base de datos.
     * Se ejecuta después de crear las tablas en onCreate.
     * 
     * @param db Instancia de SQLiteDatabase donde se insertarán los datos
     */
    fun seed(db: SQLiteDatabase) {
        seedUsuarios(db)
        seedMaterias(db)
        seedGrupos(db)
        seedHorarios(db)
        seedBoletas(db)
        seedAsistencias(db)
        
        Log.d(TAG, "Todos los datos de prueba insertados correctamente")
    }
    
    /**
     * Inserta usuarios de prueba en la base de datos.
     * 
     * Usuarios disponibles (10 total):
     * - Alumnos: alumno1-3 (contraseña: 1234)
     * - Docentes: docente1-5 (contraseña: 1234)
     * - Admin: admin1-2 (contraseña: 1234)
     */
    private fun seedUsuarios(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO usuarios(nombres, apellidos, username, contrasena, registro, rol)
            VALUES
            ('Ana', 'García', 'alumno1', '1234', '211882', 'Alumno'),
            ('Juan', 'Pérez', 'alumno2', '1234', '212732', 'Alumno'),
            ('Carlos', 'López', 'alumno3', '1234', '210882', 'Alumno'),
            ('Marcos', 'Rodríguez', 'docente1', '1234', '342232', 'Docente'),
            ('Maria', 'Fernández', 'docente2', '1234', '45532', 'Docente'),
            ('Julia', 'Martínez', 'docente3', '1234', '56322', 'Docente'),
            ('Roberto', 'Sánchez', 'docente4', '1234', '67890', 'Docente'),
            ('Laura', 'González', 'docente5', '1234', '78901', 'Docente'),
            ('Admin', 'Principal', 'admin1', '1234', '11111', 'Admin'),
            ('Super', 'Admin', 'admin2', '1234', '22222', 'Admin')
        """.trimIndent()
        )
        Log.d(TAG, "10 usuarios de prueba insertados")
    }
    
    /**
     * Inserta materias de prueba en la base de datos.
     * 
     * 20 materias de diferentes niveles académicos.
     */
    private fun seedMaterias(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO materias(nombre, sigla, nivel)
            VALUES
            ('Programación I', 'PROG1', 1),
            ('Programación II', 'PROG2', 2),
            ('Programación III', 'PROG3', 3),
            ('Base de Datos I', 'BD1', 2),
            ('Base de Datos II', 'BD2', 3),
            ('Estructura de Datos', 'ED', 2),
            ('Algoritmos y Complejidad', 'ALG', 3),
            ('Sistemas Operativos', 'SO', 3),
            ('Redes de Computadoras', 'REDES', 4),
            ('Ingeniería de Software', 'IS', 4),
            ('Arquitectura de Computadoras', 'ARQ', 2),
            ('Matemática Discreta', 'MD', 1),
            ('Cálculo I', 'CAL1', 1),
            ('Cálculo II', 'CAL2', 2),
            ('Álgebra Lineal', 'ALG_LIN', 2),
            ('Física I', 'FIS1', 1),
            ('Física II', 'FIS2', 2),
            ('Química General', 'QUIM', 1),
            ('Ética Profesional', 'ETICA', 3),
            ('Gestión de Proyectos', 'GP', 4)
        """.trimIndent()
        )
        Log.d(TAG, "20 materias de prueba insertadas")
    }
    
    /**
     * Inserta grupos de prueba en la base de datos.
     * 
     * ⭐ PATRÓN STRATEGY CON DATOS DE BD:
     * Ahora incluye valores de tolerancia_minutos variados para probar
     * el cálculo flexible de estado de asistencia:
     * - 5 min: Política muy estricta
     * - 10 min: Política estándar (por defecto)
     * - 15 min: Política flexible
     * - 20 min: Política muy flexible
     * 
     * Crea grupos variados relacionando materias con docentes.
     * Nota: Los IDs de materias y docentes se asumen basados en el orden de inserción.
     */
    private fun seedGrupos(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO grupos(materia_id, materia_nombre, docente_id, docente_nombre, semestre, gestion, capacidad, grupo, tolerancia_minutos)
            VALUES
            (1, 'Programación I', 4, 'Marcos Rodríguez', 1, 2025, 30, 'A', 10),
            (1, 'Programación I', 5, 'Maria Fernández', 1, 2025, 25, 'B', 15),
            (2, 'Programación II', 4, 'Marcos Rodríguez', 1, 2025, 28, 'A', 10),
            (2, 'Programación II', 6, 'Julia Martínez', 1, 2025, 30, 'B', 20),
            (3, 'Programación III', 4, 'Marcos Rodríguez', 1, 2025, 20, 'A', 5),
            (4, 'Base de Datos I', 5, 'Maria Fernández', 1, 2025, 30, 'A', 10),
            (4, 'Base de Datos I', 7, 'Roberto Sánchez', 1, 2025, 25, 'B', 15),
            (5, 'Base de Datos II', 5, 'Maria Fernández', 1, 2025, 22, 'A', 10),
            (6, 'Estructura de Datos', 6, 'Julia Martínez', 1, 2025, 28, 'A', 10),
            (6, 'Estructura de Datos', 7, 'Roberto Sánchez', 1, 2025, 30, 'B', 20),
            (7, 'Algoritmos y Complejidad', 4, 'Marcos Rodríguez', 1, 2025, 20, 'A', 5),
            (8, 'Sistemas Operativos', 7, 'Roberto Sánchez', 1, 2025, 25, 'A', 10),
            (8, 'Sistemas Operativos', 8, 'Laura González', 1, 2025, 28, 'B', 15),
            (9, 'Redes de Computadoras', 8, 'Laura González', 1, 2025, 30, 'A', 10),
            (10, 'Ingeniería de Software', 5, 'Maria Fernández', 1, 2025, 25, 'A', 15),
            (11, 'Arquitectura de Computadoras', 6, 'Julia Martínez', 1, 2025, 30, 'A', 10),
            (12, 'Matemática Discreta', 7, 'Roberto Sánchez', 1, 2025, 35, 'A', 10),
            (13, 'Cálculo I', 8, 'Laura González', 1, 2025, 40, 'A', 15),
            (19, 'Ética Profesional', 5, 'Maria Fernández', 1, 2025, 50, 'A', 20),
            (20, 'Gestión de Proyectos', 6, 'Julia Martínez', 1, 2025, 30, 'A', 10)
        """.trimIndent()
        )
        Log.d(TAG, "20 grupos de prueba insertados con tolerancias configurables")
    }
    
    /**
     * Inserta horarios de prueba en la base de datos.
     * 
     * Asigna horarios variados a los grupos creados.
     */
    private fun seedHorarios(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO horarios(grupo_id, dia, hora_inicio, hora_fin)
            VALUES
            (1, 'Lunes', '08:00', '10:00'),
            (1, 'Miércoles', '08:00', '10:00'),
            (2, 'Martes', '10:00', '12:00'),
            (2, 'Jueves', '10:00', '12:00'),
            (3, 'Lunes', '14:00', '16:00'),
            (3, 'Miércoles', '14:00', '16:00'),
            (4, 'Martes', '14:00', '16:00'),
            (4, 'Jueves', '14:00', '16:00'),
            (5, 'Lunes', '16:00', '18:00'),
            (5, 'Miércoles', '16:00', '18:00'),
            (6, 'Martes', '08:00', '10:00'),
            (6, 'Viernes', '08:00', '10:00'),
            (7, 'Lunes', '10:00', '12:00'),
            (7, 'Miércoles', '10:00', '12:00'),
            (8, 'Martes', '16:00', '18:00'),
            (8, 'Jueves', '16:00', '18:00'),
            (9, 'Lunes', '08:00', '10:00'),
            (9, 'Viernes', '08:00', '10:00'),
            (10, 'Martes', '10:00', '12:00'),
            (10, 'Viernes', '10:00', '12:00'),
            (11, 'Miércoles', '14:00', '16:00'),
            (11, 'Viernes', '14:00', '16:00'),
            (12, 'Lunes', '10:00', '12:00'),
            (12, 'Miércoles', '10:00', '12:00'),
            (13, 'Martes', '08:00', '10:00'),
            (13, 'Jueves', '08:00', '10:00'),
            (14, 'Lunes', '14:00', '16:00'),
            (14, 'Miércoles', '14:00', '16:00'),
            (15, 'Martes', '14:00', '16:00'),
            (15, 'Jueves', '14:00', '16:00'),
            (16, 'Lunes', '16:00', '18:00'),
            (16, 'Miércoles', '16:00', '18:00'),
            (17, 'Martes', '16:00', '18:00'),
            (17, 'Jueves', '16:00', '18:00'),
            (18, 'Lunes', '08:00', '10:00'),
            (18, 'Miércoles', '08:00', '10:00'),
            (19, 'Viernes', '10:00', '12:00'),
            (20, 'Viernes', '14:00', '16:00')
        """.trimIndent()
        )
        Log.d(TAG, "38 horarios de prueba insertados")
    }
    
    /**
     * Inserta boletas (inscripciones) de prueba en la base de datos.
     * 
     * Inscribe alumnos en diferentes grupos para pruebas.
     */
    private fun seedBoletas(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO boletas(alumno_id, grupo_id, fecha, semestre, gestion)
            VALUES
            (1, 1, '2025-01-15', 1, 2025),
            (1, 4, '2025-01-15', 1, 2025),
            (1, 6, '2025-01-15', 1, 2025),
            (1, 9, '2025-01-15', 1, 2025),
            (1, 12, '2025-01-15', 1, 2025),
            (2, 1, '2025-01-16', 1, 2025),
            (2, 3, '2025-01-16', 1, 2025),
            (2, 7, '2025-01-16', 1, 2025),
            (2, 10, '2025-01-16', 1, 2025),
            (2, 13, '2025-01-16', 1, 2025),
            (3, 2, '2025-01-17', 1, 2025),
            (3, 5, '2025-01-17', 1, 2025),
            (3, 8, '2025-01-17', 1, 2025),
            (3, 11, '2025-01-17', 1, 2025),
            (3, 14, '2025-01-17', 1, 2025),
            (1, 15, '2025-01-18', 1, 2025),
            (2, 16, '2025-01-18', 1, 2025),
            (3, 17, '2025-01-18', 1, 2025),
            (1, 19, '2025-01-19', 1, 2025),
            (2, 20, '2025-01-19', 1, 2025),
            (3, 19, '2025-01-19', 1, 2025)
        """.trimIndent()
        )
        Log.d(TAG, "21 boletas (inscripciones) de prueba insertadas")
    }
    
    /**
     * Inserta asistencias de prueba en la base de datos.
     * 
     * Registra asistencias de alumnos en diferentes grupos y fechas.
     */
    private fun seedAsistencias(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO asistencias(alumno_id, grupo_id, fecha)
            VALUES
            (1, 1, '2025-01-20'),
            (1, 1, '2025-01-22'),
            (1, 4, '2025-01-21'),
            (1, 4, '2025-01-23'),
            (1, 6, '2025-01-21'),
            (1, 6, '2025-01-24'),
            (1, 9, '2025-01-20'),
            (1, 9, '2025-01-24'),
            (1, 12, '2025-01-22'),
            (2, 1, '2025-01-20'),
            (2, 1, '2025-01-22'),
            (2, 3, '2025-01-20'),
            (2, 3, '2025-01-22'),
            (2, 7, '2025-01-21'),
            (2, 7, '2025-01-23'),
            (2, 10, '2025-01-21'),
            (2, 10, '2025-01-24'),
            (2, 13, '2025-01-22'),
            (3, 2, '2025-01-21'),
            (3, 2, '2025-01-23'),
            (3, 5, '2025-01-20'),
            (3, 5, '2025-01-22'),
            (3, 8, '2025-01-21'),
            (3, 8, '2025-01-23'),
            (3, 11, '2025-01-22'),
            (3, 11, '2025-01-24'),
            (3, 14, '2025-01-20'),
            (3, 14, '2025-01-22'),
            (1, 15, '2025-01-21'),
            (2, 16, '2025-01-20'),
            (3, 17, '2025-01-21'),
            (1, 19, '2025-01-24'),
            (2, 20, '2025-01-24'),
            (3, 19, '2025-01-24')
        """.trimIndent()
        )
        Log.d(TAG, "34 asistencias de prueba insertadas")
    }
    
    /**
     * Limpia todos los datos de prueba de la base de datos.
     * Útil para resetear el estado inicial en desarrollo.
     * 
     * ⚠️ CUIDADO: Esto elimina todos los datos de las tablas.
     */
    fun clearSeedData(db: SQLiteDatabase) {
        db.execSQL("DELETE FROM asistencias")
        db.execSQL("DELETE FROM boletas")
        db.execSQL("DELETE FROM horarios")
        db.execSQL("DELETE FROM grupos")
        db.execSQL("DELETE FROM materias")
        db.execSQL("DELETE FROM usuarios")
        Log.d(TAG, "Datos de prueba eliminados")
    }
}

