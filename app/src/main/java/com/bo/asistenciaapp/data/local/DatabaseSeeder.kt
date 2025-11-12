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
        // Aquí se pueden agregar más seeders:
        // seedMaterias(db)
        // seedGrupos(db)
        // etc.
        
        Log.d(TAG, "Datos de prueba insertados correctamente")
    }
    
    /**
     * Inserta usuarios de prueba en la base de datos.
     * 
     * Usuarios disponibles:
     * - Alumnos: alumno1, alumno2, alumno3 (contraseña: 1234)
     * - Docentes: docente1, docente2, docente3 (contraseña: 1234)
     * - Admin: admin1 (contraseña: 1234)
     */
    private fun seedUsuarios(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO usuarios(nombres, apellidos, username, contrasena, registro, rol)
            VALUES
            ('Ana', 'Alumno', 'alumno1', '1234', '211882', 'Alumno'),
            ('Juan', 'Alumno', 'alumno2', '1234', '212732', 'Alumno'),
            ('Carlos', 'Alumno', 'alumno3', '1234', '210882', 'Alumno'),
            ('Marcos', 'Docente', 'docente1', '1234', '342232', 'Docente'),
            ('Maria', 'Docente', 'docente2', '1234', '45532', 'Docente'),
            ('Julia', 'Docente', 'docente3', '1234', '56322', 'Docente'),
            ('Admin', 'Admin', 'admin1', '1234', '11111', 'Admin')
        """.trimIndent()
        )
        Log.d(TAG, "Usuarios de prueba insertados")
    }
    
    /**
     * Ejemplo de método para insertar materias de prueba.
     * Descomentar y usar cuando se necesiten datos de prueba de materias.
     */
    // private fun seedMaterias(db: SQLiteDatabase) {
    //     db.execSQL(
    //         """
    //         INSERT INTO materias(nombre, sigla, nivel)
    //         VALUES
    //         ('Base de Datos', 'BD', 3),
    //         ('Programación', 'PROG', 2)
    //     """.trimIndent()
    //     )
    //     Log.d(TAG, "Materias de prueba insertadas")
    // }
    
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

