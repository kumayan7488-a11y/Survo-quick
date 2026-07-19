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

private val DarkColorScheme =
  darkColorScheme(
    primary = TealPrimaryDark,
    secondary = TealSecondaryDark,
    tertiary = AmberAccentDark,
    background = DarkBackground,
    surface = CardBackgroundDark,
    onPrimary = Color(0xFF21005D),
    onSecondary = Color(0xFF21005D),
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondaryContainer = Color(0xFF332D41),
    onSecondaryContainer = Color(0xFFEADDFF),
    tertiaryContainer = Color(0xFF21005D),
    onTertiaryContainer = Color(0xFFEADDFF),
    surfaceVariant = Color(0xFF312E38),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF49454F)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TealPrimary,
    secondary = TealSecondary,
    tertiary = TealSecondary, // Make tertiary more deep purple
    background = MintBackground,
    surface = Color.White, // Standard card or box surfaces
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    primaryContainer = Color(0xFFD0BCFF), // Lavender
    onPrimaryContainer = Color(0xFF21005D), // Deep Purple
    secondaryContainer = Color(0xFFEADDFF), // Soft Lavender
    onSecondaryContainer = Color(0xFF21005D),
    tertiaryContainer = Color(0xFFEADDFF),
    onTertiaryContainer = Color(0xFF21005D),
    surfaceVariant = Color(0xFFF3EDF7), // Bento item background
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCAC4D0)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color so our custom themed branding remains consistent
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
