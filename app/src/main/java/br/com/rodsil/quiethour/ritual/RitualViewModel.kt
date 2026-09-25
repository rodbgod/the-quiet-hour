package br.com.rodsil.quiethour.ritual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rodsil.quiethour.content.Passage
import br.com.rodsil.quiethour.data.PassageRepository
import br.com.rodsil.quiethour.data.RitualRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface RitualState {
  data object Loading : RitualState

  data object Failed : RitualState

  /** [date] is fixed when the ritual starts, so a ritual begun before midnight completes for that day. */
  data class Ready(val passage: Passage, val date: LocalDate) : RitualState
}

@HiltViewModel
class RitualViewModel
@Inject
constructor(private val passages: PassageRepository, private val ritual: RitualRepository, private val clock: Clock) :
  ViewModel() {
  private val mutableState = MutableStateFlow<RitualState>(RitualState.Loading)
  val state: StateFlow<RitualState> = mutableState

  private val mutableStreak = MutableStateFlow<Streak>(Streak.None)
  val streak: StateFlow<Streak> = mutableStreak

  init {
    load()
  }

  fun load() {
    mutableState.value = RitualState.Loading
    viewModelScope.launch {
      val today = LocalDate.now(clock)
      mutableState.value =
        runCatching { passages.passageFor(today) }
          .fold(onSuccess = { RitualState.Ready(it, today) }, onFailure = { RitualState.Failed })
    }
  }

  fun completePause(skipped: Boolean) {
    val ready = mutableState.value as? RitualState.Ready ?: return
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
