package com.ipca.lojasocial.presentation.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * LIGHT COLOR SCHEME - Tema Principal
 * Cores modernas, profissionais e acessíveis (WCAG AA)
 */
private val LightColorScheme = lightColorScheme(
    // Primary - Azul profissional
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    
    // Secondary - Cinza azulado
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    
    // Tertiary - Âmbar/Laranja para CTAs
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    
    // Error
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    
    // Background & Surface
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    
    // Outline
    outline = Outline,
    outlineVariant = OutlineVariant
)

/**
 * DARK COLOR SCHEME - Modo Escuro (Futuro)
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFD3E7FF),
    
    secondary = Color(0xFFB0BEC5),
    onSecondary = Color(0xFF2C3E4D),
    secondaryContainer = Color(0xFF425564),
    onSecondaryContainer = Color(0xFFCFD8DC),
    
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF6A3A00),
    onTertiaryContainer = Color(0xFFFFE0B2),
    
    error = Color(0xFFEF5350),
    onError = Color(0xFF5F1A1A),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFCDD2),
    
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44464E),
    onSurfaceVariant = Color(0xFFC5C6D0),
    
    outline = Color(0xFF8F9099),
    outlineVariant = Color(0xFF44464E)
)

/**
 * IPCA Loja Social Theme - Moderno, Acessível, Profissional
 */
@Composable
fun IPCALojaSocialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Dynamic color para Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color (Android 12+) - Opcional
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        
        // Tema escuro (futuro)
        darkTheme -> DarkColorScheme
        
        // Tema claro (padrão)
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Definir no Type.kt
        content = content
    )
}
