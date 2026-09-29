package br.com.rodsil.quiethour

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.rodsil.quiethour.settings.SettingsViewModel
import br.com.rodsil.quiethour.theme.TheQuietHourTheme
import br.com.rodsil.quiethour.theme.isDarkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val settingsViewModel: SettingsViewModel = hiltViewModel()
      val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
      val current = settings ?: return@setContent
      val dark = isDarkTheme(current.theme)
      LaunchedEffect(dark) { enableEdgeToEdge(systemBarStyle(dark), systemBarStyle(dark)) }
      TheQuietHourTheme(current.theme, current.textSize) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(current, settingsViewModel)
        }
      }
    }
  }
}

/** Keeps status bar icons legible when the chosen theme differs from the system's light or dark mode. */
private fun systemBarStyle(dark: Boolean): SystemBarStyle =
  if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
