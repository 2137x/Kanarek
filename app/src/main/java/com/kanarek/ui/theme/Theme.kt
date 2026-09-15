package com.kanarek.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.kanarek.data.AppThemeMode

private val LightColors =
    lightColorScheme(
        primary = Accent,
        onPrimary = Color.White,
        primaryContainer = AccentContainer,
        onPrimaryContainer = LightText,
        tertiary = Signal,
        onTertiary = Color(0xFF3D2900),
        background = LightBg,
        onBackground = LightText,
        surface = LightSurface,
        onSurface = LightText,
        surfaceVariant = LightSurfaceMuted,
        onSurfaceVariant = LightTextMuted,
        outline = LightBorder,
    )

private val DarkColors =
    darkColorScheme(
        primary = AccentDark,
        onPrimary = Color(0xFF00315C),
        primaryContainer = AccentContainerDark,
        onPrimaryContainer = DarkText,
        tertiary = SignalDark,
        onTertiary = Color(0xFF422D00),
        background = DarkBg,
        onBackground = DarkText,
        surface = DarkSurface,
        onSurface = DarkText,
        surfaceVariant = DarkSurfaceMuted,
        onSurfaceVariant = DarkTextMuted,
        outline = DarkBorder,
    )

@Composable
fun KanarekTheme(
    mode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme =
        when (mode) {
            AppThemeMode.SYSTEM -> systemDark
            AppThemeMode.LIGHT -> false
            AppThemeMode.DARK -> true
        }
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColors
            else -> LightColors
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = KanarekTypography,
        shapes = KanarekShapes,
        content = content,
    )
}
