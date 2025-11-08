package com.bo.asistenciaapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.bo.asistenciaapp.domain.model.Asistencia
import com.bo.asistenciaapp.domain.model.Boleta
import com.bo.asistenciaapp.domain.model.Grupo
import com.bo.asistenciaapp.domain.model.Horario
import com.bo.asistenciaapp.domain.model.Materia
import com.bo.asistenciaapp.domain.model.Usuario

class AppDatabase(context: Context) :
    SQLiteOpenHelper(context, "asistenciadb.db", null, 16) {

    override fun onCreate(db: SQLiteDatabase) {
        // Usuarios
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

        // Materias, grupos y horarios
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS materias (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    sigla TEXT NOT NULL UNIQUE,
                    nivel INTEGER NOT NULL
                )
            """
        )

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
                    FOREIGN KEY(materia_id) REFERENCES materias(id),
                    FOREIGN KEY(docente_id) REFERENCES usuarios(id)
                )
            """
        )

        // Log.d("AppDatabase", "CREATED GRUPOS")
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
            """
        )

        // Boletas de Inscripcion
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
        """
        )

        // Asistencias
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS asistencias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alumno_id INTEGER NOT NULL,
                grupo_id INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                FOREIGN KEY(alumno_id) REFERENCES usuarios(id),
                FOREIGN KEY(grupo_id) REFERENCES grupos(id)
                )
            """
        )


        // Datos de prueba (usuario, contraseña, rol)
        db.execSQL(
            """
            INSERT INTO usuarios(nombres, apellidos,username, contrasena, registro, rol)
            VALUES
            ('Ana', 'Alumno', 'alumno1', '1234', '211882','Alumno'),
            ('Juan', 'Alumno', 'alumno2', '1234', '212732','Alumno'),
            ('Carlos', 'Alumno', 'alumno3', '1234', '210882','Alumno'),
            ('Marcos', 'Docente', 'docente1', '1234', '342232','Docente'),
            ('Maria', 'Docente', 'docente2', '1234', '45532','Docente'),
            ('Julia', 'Docente', 'docente3', '1234', '56322','Docente'),
            ('Admin', 'Admin', 'admin1', '1234', '11111','Admin')
        """.trimIndent()
        )
        Log.d("AppDatabase", "Datos de prueba insertados")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        db.execSQL("DROP TABLE IF EXISTS usuarios")
//        db.execSQL("DROP TABLE IF EXISTS grupos")
        onCreate(db)
    }

    fun validarUsuario(username: String, contrasena: String): Usuario? {
        readableDatabase.rawQuery(
            "SELECT * FROM usuarios WHERE username=? AND contrasena=?",
            arrayOf(username, contrasena)
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                Usuario(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nombres = cursor.getString(cursor.getColumnIndexOrThrow("nombres")),
                    apellidos = cursor.getString(cursor.getColumnIndexOrThrow("apellidos")),
                    registro = cursor.getString(cursor.getColumnIndexOrThrow("registro")),
                    rol = cursor.getString(cursor.getColumnIndexOrThrow("rol")),
                    username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                )
            } else {
                null
            }
        }
    }

    fun obtenerUsuarios(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(
            "SELECT id, nombres, apellidos, registro, rol, username FROM usuarios ORDER BY id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Usuario(
                        id = c.getInt(0),
                        nombres = c.getString(1),
                        apellidos = c.getString(2),
                        registro = c.getString(3),
                        rol = c.getString(4),
                        username = c.getString(5),
                    )
                )
            }
        }
        return lista
    }

    fun obtenerDocentes(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(
            "SELECT id, nombres, apellidos, registro, rol, username FROM usuarios WHERE rol LIKE '%docente%' ORDER BY id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Usuario(
                        id = c.getInt(0),
                        nombres = c.getString(1),
                        apellidos = c.getString(2),
                        registro = c.getString(3),
                        rol = c.getString(4),
                        username = c.getString(5),
                    )
                )
            }
        }
        return lista
    }

    fun agregarUsuario(
        nombres: String,
        apellidos: String,
        registro: String,
        rol: String,
        username: String,
        contrasena: String
    ) {
        writableDatabase.execSQL(
            "INSERT INTO usuarios(nombres, apellidos, registro, rol, username, contrasena) VALUES (?,?,?,?,?,?)",
            arrayOf(nombres, apellidos, registro, rol, username, contrasena)
        )
    }

    fun eliminarUsuario(id: Int) {
        writableDatabase.execSQL("DELETE FROM usuarios where id=?", arrayOf(id))
    }

    fun actualizarUsuario(id: Int, nombres: String, apellidos: String, rol: String) {
        writableDatabase.execSQL(
            "UPDATE usuarios SET nombres=?, apellidos=?, rol=? WHERE id=?",
            arrayOf(nombres, apellidos, rol, id)
        )
    }

    fun obtenerMaterias(): List<Materia> {
        val lista = mutableListOf<Materia>()
        readableDatabase.rawQuery(
            "SELECT id, nombre, sigla, nivel FROM materias ORDER BY id DESC", null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Materia(
                        id = c.getInt(0),
                        nombre = c.getString(1),
                        sigla = c.getString(2),
                        nivel = c.getInt(3)
                    )
                )
            }
        }
        return lista
    }

    fun agregarMateria(nombre: String, sigla: String, nivel: Int) {
        writableDatabase.execSQL(
            "INSERT INTO materias(nombre, sigla, nivel) VALUES (?,?,?)",
            arrayOf(nombre, sigla, nivel)
        )
    }

    fun eliminarMateria(id: Int) {
        writableDatabase.execSQL(
            "DELETE FROM materias where id=?", arrayOf(id)
        )
    }

    fun obtenerGrupos(): List<Grupo> {
        var lista = mutableListOf<Grupo>()
        readableDatabase.rawQuery(
            "SELECT id, materia_id, materia_nombre, docente_id, docente_nombre, semestre, gestion, capacidad, nro_inscritos, grupo FROM grupos",
            null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Grupo(
                        id = c.getInt(0),
                        materiaId = c.getInt(1),
                        materiaNombre = c.getString(2),
                        docenteId = c.getInt(3),
                        docenteNombre = c.getString(4),
                        semestre = c.getInt(5),
                        gestion = c.getInt(6),
                        capacidad = c.getInt(7),
                        nroInscritos = c.getInt(8),
                        grupo = c.getString(9)
                    )
                )
            }
            return lista
        }
    }

    fun agregarGrupo(
        materiaId: Int,
        materiaNombre: String,
        docenteId: Int,
        docenteNombre: String,
        semestre: Int,
        gestion: Int,
        capacidad: Int,
        grupo: String
    ) {
        writableDatabase.execSQL(
            "INSERT INTO grupos(materia_id, materia_nombre, docente_id, docente_nombre, semestre, gestion, capacidad, grupo) VALUES (?,?,?,?,?,?,?,?)",
            arrayOf(
                materiaId,
                materiaNombre,
                docenteId,
                docenteNombre,
                semestre,
                gestion,
                capacidad,
                grupo
            )
        )
    }

    fun eliminarGrupo(id: Int) {
        writableDatabase.execSQL(
            "DELETE FROM grupos WHERE id=?", arrayOf(id)
        )
    }

    fun obtenerHorarios(): List<Horario> {
        val lista = mutableListOf<Horario>()
        readableDatabase.rawQuery(
            """
                SELECT 
                h.id, 
                h.grupo_id, 
                h.dia, 
                h.hora_inicio, 
                h.hora_fin, 
                g.materia_nombre,
                g.grupo
                FROM 
                horarios h join grupos g on h.grupo_id = g.id 
                ORDER BY g.id DESC
            """.trimIndent(), null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Horario(
                        id = c.getInt(0),
                        grupoId = c.getInt(1),
                        dia = c.getString(2),
                        horaInicio = c.getString(3),
                        horaFin = c.getString(4),
                        materia = c.getString(5),
                        grupo = c.getString(6),
                    )
                )
            }
        }
        return lista
    }

    fun agregarHorario(grupoId: Int, dia: String, horaInicio: String, horaFin: String) {
        writableDatabase.execSQL(
            "INSERT INTO horarios(grupo_id, dia, hora_inicio, hora_fin) VALUES (?,?,?,?)",
            arrayOf(grupoId, dia, horaInicio, horaFin)
        )
    }

    fun eliminarHorario(id: Int) {
        writableDatabase.execSQL(
            "DELETE FROM horarios WHERE id=?", arrayOf(id)
        )
    }

    fun obtenerBoletasPorAlumno(alumnoId: Int): List<Boleta> {
        val lista = mutableListOf<Boleta>()
        readableDatabase.rawQuery(
            """
                SELECT 
                b.id, 
                b.alumno_id, 
                b.grupo_id, 
                b.fecha, 
                b.semestre, 
                b.gestion,
                g.grupo,
                g.materia_nombre,
                h.dia,
                h.hora_inicio,
                h.hora_fin
                FROM boletas b join grupos g on b.grupo_id = g.id
                join horarios h on h.grupo_id = g.id
                WHERE b.alumno_id=?
            """.trimIndent(),
            arrayOf(alumnoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Boleta(
                        id = c.getInt(0),
                        alumnoId = c.getInt(1),
                        grupoId = c.getInt(2),
                        fecha = c.getString(3),
                        semestre = c.getInt(4),
                        gestion = c.getInt(5),
                        grupo = c.getString(6),
                        materiaNombre = c.getString(7),
                        dia = c.getString(8),
                        horario = c.getString(8) + " - " + c.getString(9)
                    )
                )
            }
        }
        return lista
    }

    fun registrarBoleta(alumnoId: Int, grupoId: Int, fecha: String, semestre: Int, gestion: Int) {
        writableDatabase.execSQL(
            "INSERT INTO boletas(alumno_id, grupo_id, fecha, semestre, gestion) VALUES (?,?,?,?,?)",
            arrayOf(alumnoId, grupoId, fecha, semestre, gestion)
        )
    }

    fun tieneCruceDeHorario(alumnoId: Int, grupoId: Int): Boolean {
        // horarios del grupo a inscribir
        val horariosNuevo = mutableListOf<Pair<String, StringRange>>()
        readableDatabase.rawQuery(
            "SELECT dia, hora_inicio, hora_fin FROM horarios WHERE grupo_id=?",
            arrayOf(grupoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                horariosNuevo.add(
                    Pair(
                        c.getString(0),
                        StringRange(c.getString(1), c.getString(2))
                    )
                )
            }
        }

        // horarios de grupos ya inscritos
        val horariosPrevios = mutableListOf<Pair<String, StringRange>>()
        readableDatabase.rawQuery(
            """
                SELECT h.dia, h.hora_inicio, h.hora_fin
                FROM horarios h
                INNER JOIN boletas b ON b.grupo_id = h.grupo_id
                WHERE b.alumno_id = ?
            """, arrayOf(alumnoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                horariosPrevios.add(
                    Pair(
                        c.getString(0),
                        StringRange(c.getString(1), c.getString(2))
                    )
                )
            }
        }

        // comparar
        for (nuevo in horariosNuevo) {
            for (prev in horariosPrevios) {
                if (nuevo.first == prev.first &&
                    nuevo.second.overlaps(prev.second)
                ) return true
            }
        }
        return false
    }

    fun registrarAsistencia(alumnoId: Int, grupoId: Int, fecha: String) {
        writableDatabase.execSQL(
            "INSERT INTO asistencias(alumno_id, grupo_id, fecha) VALUES (?,?,?)",
            arrayOf(alumnoId, grupoId, fecha)
        )
    }

    fun obtenerAsistenciasPorAlumno(alumnoId: Int): List<Asistencia> {
        val lista = mutableListOf<Asistencia>()
        readableDatabase.rawQuery(
            """
                SELECT 
                a.id, 
                a.alumno_id, 
                a.grupo_id, 
                a.fecha,
                g.grupo,
                g.materia_nombre
                FROM 
                asistencias a join grupos g 
                WHERE a.alumno_id=?
            """.trimIndent(),
            arrayOf(alumnoId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Asistencia(
                        id = c.getInt(0),
                        alumnoId = c.getInt(1),
                        grupoId = c.getInt(2),
                        fecha = c.getString(3),
                        grupo = c.getString(4),
                        materiaNombre = c.getString(5)
                    )
                )
            }
        }
        return lista
    }

    fun puedeMarcarAsistencia(alumnoId: Int, grupoId: Int): Boolean {
        val cal = java.util.Calendar.getInstance()
        val diaSemana = cal.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.LONG, java.util.Locale.getDefault())
        val horaActual = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(cal.time)

        readableDatabase.rawQuery(
            """
        SELECT 1
        FROM horarios h
        INNER JOIN boletas b ON b.grupo_id = h.grupo_id
        WHERE b.alumno_id = ? AND h.grupo_id = ?
          AND lower(h.dia) = lower(?)
          AND h.hora_inicio <= ? AND h.hora_fin >= ?
        """.trimIndent(),
            arrayOf(alumnoId.toString(), grupoId.toString(), diaSemana, horaActual, horaActual)
        ).use { c ->
            return c.moveToFirst()
        }
    }

}