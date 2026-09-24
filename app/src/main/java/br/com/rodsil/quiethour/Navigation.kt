package br.com.rodsil.quiethour

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.com.rodsil.quiethour.content.Passage
import br.com.rodsil.quiethour.ritual.PassageScreen
import br.com.rodsil.quiethour.ritual.QuestionScreen
import br.com.rodsil.quiethour.ritual.RitualFailedScreen
import br.com.rodsil.quiethour.ritual.RitualState
import br.com.rodsil.quiethour.ritual.RitualViewModel

@Composable
fun MainNavigation(viewModel: RitualViewModel = hiltViewModel()) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  when (val current = state) {
    RitualState.Loading -> Unit
    RitualState.Failed -> RitualFailedScreen(onRetry = viewModel::load)
    is RitualState.Ready -> RitualNavigation(current.passage)
  }
}

@Composable
private fun RitualNavigation(passage: Passage) {
  val backStack = rememberNavBackStack(PassageStep)
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<PassageStep> { PassageScreen(passage, onContinue = { backStack.add(QuestionStep) }) }
        entry<QuestionStep> { QuestionScreen(passage) }
      },
  )
}
