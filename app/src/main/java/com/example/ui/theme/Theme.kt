package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BoldPrimary,
    onPrimary = BoldOnPrimary,
    primaryContainer = BoldOnPrimaryContainer,
    onPrimaryContainer = BoldPrimaryContainer,
    secondary = BoldSecondary,
    onSecondary = BoldOnSecondary,
    background = Color(0xFF131314),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF131314),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF232429),
    onSurfaceVariant = Color(0xFFC2C6D0),
    errorContainer = BoldErrorContainer,
    onErrorContainer = BoldOnErrorContainer,
    outline = Color(0xFF2F3035),
    outlineVariant = Color(0xFF44474E)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BoldPrimary,
    onPrimary = BoldOnPrimary,
    primaryContainer = BoldPrimaryContainer,
    onPrimaryContainer = BoldOnPrimaryContainer,
    secondary = BoldSecondary,
    onSecondary = BoldOnSecondary,
    background = BoldBackground,
    onBackground = BoldOnBackground,
    surface = BoldSurface,
    onSurface = BoldOnSurface,
    surfaceVariant = BoldSurfaceVariant,
    onSurfaceVariant = BoldOnSurfaceVariant,
    errorContainer = BoldErrorContainer,
    onErrorContainer = BoldOnErrorContainer,
    outline = BoldOutline,
    outlineVariant = BoldOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
