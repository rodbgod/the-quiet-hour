package br.com.rodsil.quiethour.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rodsil.quiethour.data.Settings
import br.com.rodsil.quiethour.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
  /** Null until the stored settings load, so the app never flashes the default theme before the chosen one. */
  val settings: StateFlow<Settings?> = repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  fun update(transform: (Settings) -> Settings) {
    viewModelScope.launch { repository.update(transform) }
  }
}
