package com.bo.asistenciaapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores para tema oscuro.
 * 
 * Utiliza colores más claros y suaves para mejor legibilidad
 * en fondos oscuros.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = OnPrimaryDark,
    primaryContainer = Blue40,
    onPrimaryContainer = OnPrimaryLight,
    
    secondary = Green80,
    onSecondary = OnPrimaryDark,
    secondaryContainer = Green40,
    onSecondaryContainer = OnPrimaryLight,
    
    tertiary = Orange80,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = Orange40,
    onTertiaryContainer = OnPrimaryLight,
    
    error = Error80,
    onError = OnPrimaryDark,
    errorContainer = Error40,
    onErrorContainer = OnPrimaryLight,
    
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Neutral80.copy(alpha = 0.1f),
    onSurfaceVariant = Neutral80,
    
    outline = Neutral80.copy(alpha = 0.5f),
    outlineVariant = Neutral80.copy(alpha = 0.3f),
    
    scrim = BackgroundDark.copy(alpha = 0.8f),
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = SurfaceDark,
    inversePrimary = Blue40
)

/**
 * Esquema de colores para tema claro.
 * 
 * Utiliza colores más intensos y contrastados para mejor legibilidad
 * en fondos claros.
 */
private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = OnPrimaryLight,
    primaryContainer = Blue80.copy(alpha = 0.2f),
    onPrimaryContainer = Blue40,
    
    secondary = Green40,
    onSecondary = OnPrimaryLight,
    secondaryContainer = Green80.copy(alpha = 0.2f),
    onSecondaryContainer = Green40,
    
    tertiary = Orange40,
    onTertiary = OnPrimaryLight,
    tertiaryContainer = Orange80.copy(alpha = 0.2f),
    onTertiaryContainer = Orange40,
    
    error = Error40,
    onError = OnPrimaryLight,
    errorContainer = Error80.copy(alpha = 0.2f),
    onErrorContainer = Error40,
    
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Neutral40.copy(alpha = 0.1f),
    onSurfaceVariant = Neutral40,
    
    outline = Neutral40.copy(alpha = 0.5f),
    outlineVariant = Neutral40.copy(alpha = 0.3f),
    
    scrim = BackgroundLight.copy(alpha = 0.8f),
    inverseSurface = OnSurfaceLight,
    inverseOnSurface = SurfaceLight,
    inversePrimary = Blue80
)

/**
 * Tema principal de la aplicación AsistenciaApp.
 * 
 * Este composable configura el tema Material Design 3 para toda la aplicación,
 * incluyendo colores, tipografía y otros aspectos visuales.
 * 
 * Características:
 * - Soporte para tema claro y oscuro
 * - Soporte para colores dinámicos en Android 12+ (Material You)
 * - Paleta de colores académica profesional
 * - Tipografía completa según Material Design 3
 * 
 * @param darkTheme Si es true, aplica el tema oscuro. Por defecto sigue la configuración del sistema.
 * @param dynamicColor Si es true y el dispositivo lo soporta (Android 12+), usa colores dinámicos del sistema.
 * @param content Contenido composable que usará este tema.
 * 
 * Ejemplo de uso:
 * ```kotlin
 * AsistenciaAppTheme {
 *     // Tu contenido aquí
 * }
 * ```
 */
@Composable
fun AsistenciaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Usar colores dinámicos si está disponible y habilitado
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Usar esquemas personalizados según el tema
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBar = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}