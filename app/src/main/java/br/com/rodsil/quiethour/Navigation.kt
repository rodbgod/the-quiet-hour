package br.com.rodsil.quiethour

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.com.rodsil.quiethour.data.Settings
import br.com.rodsil.quiethour.journal.JournalEntryScreen
import br.com.rodsil.quiethour.journal.JournalScreen
import br.com.rodsil.quiethour.journal.JournalViewModel
import br.com.rodsil.quiethour.ritual.CloseScreen
import br.com.rodsil.quiethour.ritual.PassageScreen
import br.com.rodsil.quiethour.ritual.PauseScreen
import br.com.rodsil.quiethour.ritual.QuestionScreen
import br.com.rodsil.quiethour.ritual.ReflectionScreen
import br.com.rodsil.quiethour.ritual.RitualFailedScreen
import br.com.rodsil.quiethour.ritual.RitualState
import br.com.rodsil.quiethour.ritual.RitualViewModel
import br.com.rodsil.quiethour.settings.SettingsScreen
import br.com.rodsil.quiethour.settings.SettingsViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

private const val MILLIS_PER_SECOND = 1_000L
private const val EXPORT_MIME_TYPE = "text/markdown"

@Composable
fun MainNavigation(settings: Settings, settingsViewModel: SettingsViewModel, viewModel: RitualViewModel = hiltViewModel()) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  when (val current = state) {
    RitualState.Loading -> Unit
    RitualState.Failed -> RitualFailedScreen(onRetry = viewModel::load)
    is RitualState.Ready -> AppNavigation(current, settings, viewModel, settingsViewModel)
  }
}

@Composable
private fun AppNavigation(
  ritual: RitualState.Ready,
  settings: Settings,
  viewModel: RitualViewModel,
  settingsViewModel: SettingsViewModel,
  journalViewModel: JournalViewModel = hiltViewModel(),
) {
  val passage = ritual.passage
  val backStack = rememberNavBackStack(if (ritual.completedToday) CloseStep else PassageStep)
  val streak by viewModel.streak.collectAsStateWithLifecycle()
  val journal by journalViewModel.entries.collectAsStateWithLifecycle()
  val query by journalViewModel.query.collectAsStateWithLifecycle()
  val favoritesOnly by journalViewModel.favoritesOnly.collectAsStateWithLifecycle()
  val activity = LocalActivity.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<PassageStep> { PassageScreen(passage, onContinue = { backStack.add(QuestionStep) }) }
        entry<QuestionStep> { QuestionScreen(passage, onBegin = { backStack.add(PauseStep) }) }
        entry<PauseStep> {
          PauseScreen(
            question = passage.question,
            durationMs = settings.pauseSeconds * MILLIS_PER_SECOND,
            onFinished = { finishPause(viewModel, backStack, skipped = false) },
            onSkip = { finishPause(viewModel, backStack, skipped = true) },
            onAbandon = { backStack.removeLastOrNull() },
          )
        }
        entry<ReflectionStep> {
          ReflectionScreen(
            passage.question,
            onDone = { reflection ->
              viewModel.saveReflection(reflection)
              backStack.add(CloseStep)
              backStack.removeAll { it != CloseStep }
            },
          )
        }
        entry<CloseStep> {
          CloseScreen(
            streak,
            onJournal = { backStack.add(JournalStep) },
            onSettings = { backStack.add(SettingsStep) },
            onClose = { activity?.finish() },
          )
        }
        entry<JournalStep> {
          JournalScreen(
            entries = journal,
            query = query,
            favoritesOnly = favoritesOnly,
            onSearch = journalViewModel::search,
            onFavoritesOnlyChange = journalViewModel::showFavoritesOnly,
            onExport = { scope.launch { shareExport(context, journalViewModel.exportFile()) } },
            onOpen = { backStack.add(JournalEntryStep(it.date.toEpochDay())) },
          )
        }
        entry<JournalEntryStep> { key ->
          val date = LocalDate.ofEpochDay(key.epochDay)
          val entry by remember(date) { journalViewModel.entry(date) }.collectAsStateWithLifecycle(null)
          JournalEntryScreen(
            entry,
            onFavoriteChange = { favorite -> entry?.let { journalViewModel.setFavorite(it.passage.id, favorite) } },
          )
        }
        entry<SettingsStep> { SettingsScreen(settings, onChange = settingsViewModel::update) }
      },
  )
}

/** Replaces the pause with the reflection step so back never re-enters a finished pause. */
private fun finishPause(viewModel: RitualViewModel, backStack: NavBackStack<NavKey>, skipped: Boolean) {
  viewModel.completePause(skipped)
  backStack.removeLastOrNull()
  backStack.add(ReflectionStep)
}

private fun shareExport(context: Context, file: Uri) {
  val send =
    Intent(Intent.ACTION_SEND)
      .setType(EXPORT_MIME_TYPE)
      .putExtra(Intent.EXTRA_STREAM, file)
      .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  context.startActivity(Intent.createChooser(send, context.getString(R.string.journal_export)))
}
