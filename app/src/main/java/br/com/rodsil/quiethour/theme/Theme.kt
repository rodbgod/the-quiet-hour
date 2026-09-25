package br.com.rodsil.quiethour.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
  lightColorScheme(
    primary = Stone,
    background = Parchment,
    surface = Parchment,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = InkMuted,
    surfaceContainerHigh = Linen,
  )

private val DarkColors =
  darkColorScheme(
    primary = Sand,
    background = Night,
    surface = Night,
    onBackground = Chalk,
    onSurface = Chalk,
    onSurfaceVariant = ChalkMuted,
    surfaceContainerHigh = Charcoal,
  )

@Composable
fun TheQuietHourTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = Typography, content = content)
}
