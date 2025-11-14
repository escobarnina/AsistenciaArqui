package com.bo.asistenciaapp.presentation.common

import android.content.Context
import android.view.Gravity
import android.widget.Toast

/**
 * Utilidades para mostrar mensajes Toast en la aplicación.
 * 
 * Proporciona funciones helper para mostrar Toast en posiciones específicas,
 * especialmente útil cuando la aplicación ocupa toda la pantalla y los Toast
 * inferiores quedan ocultos por los botones de navegación de Android.
 */
object ToastUtils {
    
    /**
     * Muestra un Toast en la parte superior de la pantalla.
     * 
     * Útil cuando la aplicación ocupa toda la pantalla y los Toast inferiores
     * quedan ocultos por los botones de navegación de Android.
     * 
     * @param context Contexto de Android
     * @param mensaje Mensaje a mostrar
     * @param duracion Duración del Toast (Toast.LENGTH_SHORT o Toast.LENGTH_LONG)
     */
    fun mostrarSuperior(context: Context, mensaje: String, duracion: Int = Toast.LENGTH_LONG) {
        val toast = Toast.makeText(context, mensaje, duracion)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }
    
    /**
     * Muestra un Toast en la parte inferior de la pantalla (comportamiento por defecto).
     * 
     * @param context Contexto de Android
     * @param mensaje Mensaje a mostrar
     * @param duracion Duración del Toast (Toast.LENGTH_SHORT o Toast.LENGTH_LONG)
     */
    fun mostrarInferior(context: Context, mensaje: String, duracion: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, mensaje, duracion).show()
    }
}

