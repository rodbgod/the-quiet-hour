package br.com.rodsil.quiethour.ritual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rodsil.quiethour.content.Passage
import br.com.rodsil.quiethour.data.PassageRepository
import br.com.rodsil.quiethour.data.RitualRepository
import br.com.rodsil.quiethour.data.Settings
import br.com.rodsil.quiethour.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RitualState {
  data object Loading : RitualState

  data object Failed : RitualState

  /**
   * [date] is fixed when the ritual starts, so a ritual begun before midnight completes for that day. [completedToday]
   * opens the app on the closing screen instead of replaying the ritual.
   */
  data class Ready(val passage: Passage, val date: LocalDate, val completedToday: Boolean) : RitualState
}

@HiltViewModel
class RitualViewModel
@Inject
constructor(
  private val passages: PassageRepository,
  private val ritual: RitualRepository,
  private val chime: Chime,
  settingsRepository: SettingsRepository,
  private val clock: Clock,
) : ViewModel() {
  private val mutableState = MutableStateFlow<RitualState>(RitualState.Loading)
  val state: StateFlow<RitualState> = mutableState

  private val mutableStreak = MutableStateFlow<Streak>(Streak.None)
  val streak: StateFlow<Streak> = mutableStreak

  val settings: StateFlow<Settings> = settingsRepository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

  init {
    load()
  }

  fun load() {
    mutableState.value = RitualState.Loading
    viewModelScope.launch {
      val today = LocalDate.now(clock)
      mutableState.value =
        runCatching<RitualState> {
            val passage = passages.passageFor(today)
            mutableStreak.value = ritual.streak(today)
            RitualState.Ready(passage, today, completedToday = ritual.isCompleted(today))
          }
          .getOrDefault(RitualState.Failed)
    }
  }

  fun completePause(skipped: Boolean) {
    val ready = mutableState.value as? RitualState.Ready ?: return
    if (!skipped && settings.value.chime) chime.play()
    viewModelScope.launch {
      ritual.complete(ready.date, ready.passage.id, pauseSkipped = skipped)
      mutableStreak.value = ritual.streak(ready.date)
    }
  }

  fun saveReflection(text: String) {
    val ready = mutableState.value as? RitualState.Ready ?: return
    if (text.isBlank()) return
    viewModelScope.launch { ritual.saveReflection(ready.date, ready.passage.id, text.trim()) }
  }
}
