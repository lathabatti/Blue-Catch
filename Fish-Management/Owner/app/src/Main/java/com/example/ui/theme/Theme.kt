package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = TealNeon,
    secondary = BlueDeep,
    tertiary = OrangeAlert,
    background = OceanDark,
    surface = OceanSurface,
    onPrimary = Color(0xFF070F18),
    onSecondary = Color.White,
    onTertiary = Color(0xFF070F18),
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = OceanCard,
    onSurfaceVariant = LightIceBlue,
    error = CoralRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A5C),
    secondary = Color(0xFF0061A4),
    tertiary = Color(0xFF825500),
    background = Color(0xFFFAF9F6),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0C1926),
    onSurface = Color(0xFF0C1926),
    surfaceVariant = Color(0xFFE5F0FA),
    onSurfaceVariant = Color(0xFF1B3554),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set false to ensure our beautiful custom ocean colors shine!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Force our dark theme as default for professional dark slate atmosphere
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
