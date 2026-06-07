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

private val DarkColorScheme =
  darkColorScheme(
    primary = DbzOrangePrimary,
    secondary = DbzOrangeLight,
    tertiary = DbzAccentGold,
    background = DbzDarkBg,
    surface = DbzCardBg,
    onPrimary = DbzTextLight,
    onSecondary = DbzBlack,
    onTertiary = DbzBlack,
    onBackground = DbzTextLight,
    onSurface = DbzTextLight
  )

private val LightColorScheme =
  darkColorScheme( // Lock to Dark scheme defaults to maintain cinematic quality
    primary = DbzOrangePrimary,
    secondary = DbzOrangeLight,
    tertiary = DbzAccentGold,
    background = DbzDarkBg,
    surface = DbzCardBg,
    onPrimary = DbzTextLight,
    onSecondary = DbzBlack,
    onTertiary = DbzBlack,
    onBackground = DbzTextLight,
    onSurface = DbzTextLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force cinematic dark environment by default
  dynamicColor: Boolean = false, // Disable system dynamic color overriding our DBZ orange theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
