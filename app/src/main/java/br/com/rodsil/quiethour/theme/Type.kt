package br.com.rodsil.quiethour.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

val Typography =
  Typography(
    bodyLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp, lineHeight = 34.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, lineHeight = 26.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontSize = 26.sp, lineHeight = 36.sp),
  )
