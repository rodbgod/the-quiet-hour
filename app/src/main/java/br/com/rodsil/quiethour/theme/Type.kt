package br.com.rodsil.quiethour.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

/** Scales on top of the system font size, so the in-app setting never overrides accessibility preferences. */
fun typography(scale: Float) =
  Typography(
    bodyLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp * scale, lineHeight = 34.sp * scale),
    bodyMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 16.sp * scale, lineHeight = 24.sp * scale),
    titleMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp * scale, lineHeight = 26.sp * scale),
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontSize = 26.sp * scale, lineHeight = 36.sp * scale),
  )
