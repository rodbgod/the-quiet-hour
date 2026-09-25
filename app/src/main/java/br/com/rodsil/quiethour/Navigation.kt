package br.com.rodsil.quiethour

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.com.rodsil.quiethour.content.Passage
import br.com.rodsil.quiethour.ritual.CloseScreen
import br.com.rodsil.quiethour.ritual.PassageScreen
import br.com.rodsil.quiethour.ritual.PauseScreen
import br.com.rodsil.quiethour.ritual.QuestionScreen
import br.com.rodsil.quiethour.ritual.ReflectionScreen
import br.com.rodsil.quiethour.ritual.RitualFailedScreen
import br.com.rodsil.quiethour.ritual.RitualState
import br.com.rodsil.quiethour.ritual.RitualViewModel

@Composable
fun MainNavigation(viewModel: RitualViewModel = hiltViewModel()) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  when (val current = state) {
    RitualState.Loading -> Unit
    RitualState.Failed -> RitualFailedScreen(onRetry = viewModel::load)
    is RitualState.Ready -> RitualNavigation(current.passage, viewModel)
  }
}

@Composable
private fun RitualNavigation(passage: Passage, viewModel: RitualViewModel) {
  val backStack = rememberNavBackStack(PassageStep)
  val streak by viewModel.streak.collectAsStateWithLifecycle()
  val activity = LocalActivity.current

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
        entry<CloseStep> { CloseScreen(streak, onClose = { activity?.finish() }) }
      },
  )
}

/** Replaces the pause with the reflection step so back never re-enters a finished pause. */
private fun finishPause(viewModel: RitualViewModel, backStack: NavBackStack<NavKey>, skipped: Boolean) {
  viewModel.completePause(skipped)
  backStack.removeLastOrNull()
  backStack.add(ReflectionStep)
}
