package com.bo.asistenciaapp.data.local

import android.database.sqlite.SQLiteDatabase

/**
 * Responsabilidad: Gestionar las migraciones de la base de datos.
 * 
 * Este archivo contiene todas las definiciones de esquema (CREATE TABLE)
 * y las migraciones entre versiones de la base de datos.
 * 
 * Separado de AppDatabase para mantener clara la separación de responsabilidades:
 * - DatabaseMigrations: Estructura de la base de datos (esquema)
 * - DatabaseSeeder: Datos iniciales de prueba
 * - AppDatabase: Acceso a datos (CRUD operations)
 */
object DatabaseMigrations {
    
    /**
     * Crea todas las tablas de la base de datos.
     * Se ejecuta cuando se crea la base de datos por primera vez (onCreate).
     * 
     * @param db Instancia de SQLiteDatabase donde se crearán las tablas
     */
    fun createTables(db: SQLiteDatabase) {
        createUsuariosTable(db)
        createMateriasTable(db)
        createGruposTable(db)
        createHorariosTable(db)
        createBoletasTable(db)
        createAsistenciasTable(db)
    }
    
    /**
     * Migra la base de datos desde una versión anterior a una nueva.
     * Se ejecuta cuando se detecta un cambio en la versión de la base de datos (onUpgrade).
     * 
     * @param db Instancia de SQLiteDatabase
     * @param oldVersion Versión anterior de la base de datos
     * @param newVersion Nueva versión de la base de datos
     */
    fun migrate(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Ejemplo de migración por versiones:
        // when {
        //     oldVersion < 2 -> migrateToVersion2(db)
        //     oldVersion < 3 -> migrateToVersion3(db)
        // }
        
        // Por ahora, recreamos todas las tablas
        // En producción, esto debería ser migraciones incrementales
        dropAllTables(db)
        createTables(db)
    }
    
    /**
     * Crea la tabla de usuarios.
     */
    private fun createUsuariosTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombres TEXT NOT NULL,
                apellidos TEXT NOT NULL,
                username TEXT UNIQUE NOT NULL,
                contrasena TEXT NOT NULL,
                registro TEXT NOT NULL,
                rol TEXT NOT NULL
            )
        """.trimIndent()
        )
    }
    
    /**
     * Crea la tabla de materias.
     */
    private fun createMateriasTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS materias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                sigla TEXT NOT NULL UNIQUE,
                nivel INTEGER NOT NULL
            )
        """.trimIndent()
        )
    }
    
    /**
     * Crea la tabla de grupos.
     * 
     * ⭐ PATRÓN STRATEGY - Campo tolerancia_minutos:
     * Define el tiempo máximo (en minutos) permitido para considerar un retraso
     * antes de marcar FALTA. Este campo permite personalizar la política de 
     * asistencia por grupo, haciendo flexible el cálculo del estado.
     * 
     * Valores permitidos: 0-60 minutos
     * Valor por defecto: 10 minutos (estándar)
     */
    private fun createGruposTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS grupos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                materia_id INTEGER NOT NULL,
                materia_nombre TEXT NOT NULL,
                docente_id INTEGER NOT NULL,
                docente_nombre TEXT NOT NULL,
                grupo TEXT NOT NULL,
                semestre INTEGER NOT NULL,
                gestion INTEGER NOT NULL,
                capacidad INTEGER NOT NULL,
                nro_inscritos INTEGER DEFAULT 0,
                tolerancia_minutos INTEGER DEFAULT 10 NOT NULL CHECK(tolerancia_minutos >= 0 AND tolerancia_minutos <= 60),
                tipo_estrategia TEXT DEFAULT 'RETRASO' NOT NULL CHECK(tipo_estrategia IN ('PRESENTE', 'RETRASO', 'FALTA')),
                FOREIGN KEY(materia_id) REFERENCES materias(id),
                FOREIGN KEY(docente_id) REFERENCES usuarios(id)
            )
        """.trimIndent()
        )
    }
    
    /**
     * Crea la tabla de horarios.
     */
    private fun createHorariosTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS horarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                grupo_id INTEGER NOT NULL,
                dia TEXT NOT NULL,
                hora_inicio TEXT NOT NULL,
                hora_fin TEXT NOT NULL,
                FOREIGN KEY(grupo_id) REFERENCES grupos(id)
            )
        """.trimIndent()
        )
    }
    
    /**
     * Crea la tabla de boletas (inscripciones).
     */
    private fun createBoletasTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS boletas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alumno_id INTEGER NOT NULL,
                grupo_id INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                semestre INTEGER NOT NULL,
                gestion INTEGER NOT NULL,
                FOREIGN KEY(alumno_id) REFERENCES usuarios(id),
                FOREIGN KEY(grupo_id) REFERENCES grupos(id)
            )
        """.trimIndent()
        )
    }
    
    /**
     * Crea la tabla de asistencias.
     * 
     * Campos agregados:
     * - hora_marcada: Hora en que el alumno marcó asistencia (formato HH:mm)
     * - estado: Estado de la asistencia (PRESENTE, RETRASO, FALTA)
     */
    private fun createAsistenciasTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS asistencias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alumno_id INTEGER NOT NULL,
                grupo_id INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                hora_marcada TEXT,
                estado TEXT CHECK(estado IN ('PRESENTE', 'RETRASO', 'FALTA')),
                FOREIGN KEY(alumno_id) REFERENCES usuarios(id),
                FOREIGN KEY(grupo_id) REFERENCES grupos(id)
            )
        """.trimIndent()
        )
    }
    
    /**
     * Elimina todas las tablas (usado en migraciones).
     * ⚠️ CUIDADO: Esto elimina todos los datos.
     */
    private fun dropAllTables(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS asistencias")
        db.execSQL("DROP TABLE IF EXISTS boletas")
        db.execSQL("DROP TABLE IF EXISTS horarios")
        db.execSQL("DROP TABLE IF EXISTS grupos")
        db.execSQL("DROP TABLE IF EXISTS materias")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
    }
}

