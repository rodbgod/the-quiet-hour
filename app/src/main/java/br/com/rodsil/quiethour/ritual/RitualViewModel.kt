package br.com.rodsil.quiethour.ritual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rodsil.quiethour.content.Passage
import br.com.rodsil.quiethour.data.PassageRepository
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

  data class Ready(val passage: Passage) : RitualState
}

@HiltViewModel
class RitualViewModel @Inject constructor(private val passages: PassageRepository, private val clock: Clock) :
  ViewModel() {
  private val mutableState = MutableStateFlow<RitualState>(RitualState.Loading)
  val state: StateFlow<RitualState> = mutableState

  init {
    load()
  }

  fun load() {
    mutableState.value = RitualState.Loading
    viewModelScope.launch {
      mutableState.value =
        runCatching { passages.passageFor(LocalDate.now(clock)) }
          .fold(onSuccess = { RitualState.Ready(it) }, onFailure = { RitualState.Failed })
    }
  }
}
