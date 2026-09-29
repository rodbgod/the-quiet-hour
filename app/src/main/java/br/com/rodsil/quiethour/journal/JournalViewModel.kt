package br.com.rodsil.quiethour.journal

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rodsil.quiethour.data.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

@HiltViewModel
class JournalViewModel @Inject constructor(private val journal: JournalRepository, clock: Clock) : ViewModel() {
  private val mutableQuery = MutableStateFlow("")
  val query: StateFlow<String> = mutableQuery

  private val mutableFavoritesOnly = MutableStateFlow(false)
  val favoritesOnly: StateFlow<Boolean> = mutableFavoritesOnly

  /** Null until the journal has loaded, so an empty journal is distinguishable from a loading one. */
  private val allEntries: StateFlow<List<JournalEntry>?> =
    journal
      .journal(LocalDate.now(clock))
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

  val entries: StateFlow<List<JournalEntry>?> =
    combine(allEntries, mutableQuery, mutableFavoritesOnly) { entries, query, favoritesOnly ->
        entries?.matching(query, favoritesOnly)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

  fun entry(date: LocalDate): Flow<JournalEntry?> = allEntries.map { entries -> entries?.firstOrNull { it.date == date } }

  fun search(query: String) {
    mutableQuery.value = query
  }

  fun showFavoritesOnly(favoritesOnly: Boolean) {
    mutableFavoritesOnly.value = favoritesOnly
  }

  fun setFavorite(passageId: String, favorite: Boolean) {
    viewModelScope.launch { journal.setFavorite(passageId, favorite) }
  }

  suspend fun exportFile(): Uri = journal.exportFile(journalMarkdown(allEntries.value.orEmpty()))
}
