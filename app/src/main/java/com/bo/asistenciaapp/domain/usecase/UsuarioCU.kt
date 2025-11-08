package com.bo.asistenciaapp.domain.usecase

import com.bo.asistenciaapp.data.local.AppDatabase
import com.bo.asistenciaapp.domain.model.Usuario

class UsuarioCU(private val db: AppDatabase) {
    fun obtenerUsuarios(): List<Usuario> {
        return db.obtenerUsuarios()
    }

    fun obtenerDocentes(): List<Usuario> {
        return db.obtenerDocentes()
    }

    fun agregarUsuario(nombres: String, apellidos: String, registro: String, rol: String, username: String, contrasena: String ) {
        db.agregarUsuario(
            nombres,
            apellidos,
            registro,
            rol,
            username,
            contrasena
        )
    }
}