package com.bo.asistenciaapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.bo.asistenciaapp.data.local.dao.AsistenciaDao
import com.bo.asistenciaapp.data.local.dao.GrupoDao
import com.bo.asistenciaapp.data.local.dao.HorarioDao
import com.bo.asistenciaapp.data.local.dao.InscripcionDao
import com.bo.asistenciaapp.data.local.dao.MateriaDao
import com.bo.asistenciaapp.data.local.dao.UsuarioDao

/**
 * Singleton para gestionar la conexión a la base de datos SQLite.
 * 
 * Responsabilidad: Gestionar la conexión y proporcionar acceso a los DAOs.
 * 
 * Arquitectura:
 * AppDatabase (Singleton) → DAOs → Repositories → UseCases → ViewModels → UI
 * 
 * Separación de responsabilidades:
 * - DatabaseMigrations: Estructura de la base de datos (esquema y migraciones)
 * - DatabaseSeeder: Datos iniciales de prueba
 * - AppDatabase: Gestión de conexión y acceso a DAOs (Singleton)
 * - DAOs: Operaciones CRUD por entidad (UsuarioDao, MateriaDao, etc.)
 * - Repositories: Abstraen el acceso a datos (usan DAOs)
 * - UseCases: Lógica de negocio (usan Repositories)
 * 
 * ⚠️ IMPORTANTE: 
 * - Usar getInstance(context) para obtener la instancia singleton
 * - Los DAOs se crean lazy para optimizar el rendimiento
 * - NO usar directamente desde casos de uso o ViewModels
 */
class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "asistenciadb.db", null, 17) {  // ⭐ Versión incrementada para nueva columna tolerancia_minutos

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia singleton de AppDatabase.
         * 
         * @param context Contexto de la aplicación
         * @return Instancia única de AppDatabase
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear todas las tablas usando DatabaseMigrations
        DatabaseMigrations.createTables(db)
        
        // Insertar datos de prueba usando DatabaseSeeder
        DatabaseSeeder.seed(db)
        
        Log.d("AppDatabase", "Base de datos creada e inicializada")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migrar la base de datos usando DatabaseMigrations
        DatabaseMigrations.migrate(db, oldVersion, newVersion)
        
        // Reinsertar datos de prueba después de la migración
        DatabaseSeeder.seed(db)
        
        Log.d("AppDatabase", "Base de datos migrada de versión $oldVersion a $newVersion")
    }

    // DAOs lazy para optimizar el rendimiento
    // Se crean solo cuando se acceden por primera vez
    
    /**
     * DAO para operaciones relacionadas con usuarios.
     */
    val usuarioDao: UsuarioDao by lazy {
        UsuarioDao(writableDatabase)
    }
    
    /**
     * DAO para operaciones relacionadas con materias.
     */
    val materiaDao: MateriaDao by lazy {
        MateriaDao(writableDatabase)
    }
    
    /**
     * DAO para operaciones relacionadas con grupos.
     */
    val grupoDao: GrupoDao by lazy {
        GrupoDao(writableDatabase)
    }
    
    /**
     * DAO para operaciones relacionadas con horarios.
     */
    val horarioDao: HorarioDao by lazy {
        HorarioDao(writableDatabase)
    }
    
    /**
     * DAO para operaciones relacionadas con inscripciones (boletas).
     */
    val inscripcionDao: InscripcionDao by lazy {
        InscripcionDao(writableDatabase)
    }
    
    /**
     * DAO para operaciones relacionadas con asistencias.
     */
    val asistenciaDao: AsistenciaDao by lazy {
        AsistenciaDao(writableDatabase)
    }
}
