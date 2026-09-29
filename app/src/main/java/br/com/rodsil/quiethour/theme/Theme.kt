package br.com.rodsil.quiethour.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import br.com.rodsil.quiethour.data.AppTheme
import br.com.rodsil.quiethour.data.TextSize

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

private val SepiaColors =
  lightColorScheme(
    primary = Walnut,
    background = Paper,
    surface = Paper,
    onBackground = Umber,
    onSurface = Umber,
    onSurfaceVariant = UmberMuted,
    surfaceContainerHigh = Vellum,
  )

private val FIXED_COLOR_SCHEMES =
  mapOf(AppTheme.LIGHT to LightColors, AppTheme.DARK to DarkColors, AppTheme.SEPIA to SepiaColors)

@Composable
fun isDarkTheme(theme: AppTheme): Boolean =
  theme == AppTheme.DARK || (theme == AppTheme.SYSTEM && isSystemInDarkTheme())

@Composable
fun TheQuietHourTheme(
  theme: AppTheme = AppTheme.SYSTEM,
  textSize: TextSize = TextSize.DEFAULT,
  content: @Composable () -> Unit,
) {
  val colors = FIXED_COLOR_SCHEMES[theme] ?: if (isSystemInDarkTheme()) DarkColors else LightColors
  MaterialTheme(colorScheme = colors, typography = typography(textSize.scale), content = content)
}
