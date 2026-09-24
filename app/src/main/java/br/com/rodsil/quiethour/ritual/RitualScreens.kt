package br.com.rodsil.quiethour.ritual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.rodsil.quiethour.R
import br.com.rodsil.quiethour.content.Passage
import br.com.rodsil.quiethour.theme.TheQuietHourTheme

private val SCREEN_PADDING = 32.dp
private val ATTRIBUTION_GAP = 24.dp

@Composable
fun PassageScreen(passage: Passage, onContinue: () -> Unit) {
  Column(Modifier.fillMaxSize().safeDrawingPadding().padding(SCREEN_PADDING)) {
    Column(
      Modifier.weight(1f).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(passage.text, style = MaterialTheme.typography.bodyLarge)
      Spacer(Modifier.height(ATTRIBUTION_GAP))
      Text(passage.author, style = MaterialTheme.typography.titleMedium)
      Text(
        "${passage.work}, ${passage.reference}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    TextButton(onClick = onContinue, modifier = Modifier.align(Alignment.End)) {
      Text(stringResource(R.string.ritual_continue))
    }
  }
}

@Composable
fun QuestionScreen(passage: Passage) {
  Box(Modifier.fillMaxSize().safeDrawingPadding().padding(SCREEN_PADDING), contentAlignment = Alignment.Center) {
    Text(
      passage.question,
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
  }
}

@Composable
fun RitualFailedScreen(onRetry: () -> Unit) {
  Column(
    Modifier.fillMaxSize().safeDrawingPadding().padding(SCREEN_PADDING),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(stringResource(R.string.ritual_load_failed), style = MaterialTheme.typography.bodyLarge)
    TextButton(onClick = onRetry) { Text(stringResource(R.string.ritual_retry)) }
  }
}

private val PREVIEW_PASSAGE =
  Passage(
    id = "preview",
    text = "Take away thy opinion, and then there is taken away the complaint, \"I have been harmed.\"",
    author = "Marcus Aurelius",
    work = "Meditations",
    reference = "Book IV.7",
    translation = "George Long (1862)",
    question = "What harm are you still carrying that lives mostly in the story you tell yourself about it?",
    context = "",
  )

@Preview(showBackground = true)
@Composable
private fun PassageScreenPreview() {
  TheQuietHourTheme { PassageScreen(PREVIEW_PASSAGE, onContinue = {}) }
}

@Preview(showBackground = true)
@Composable
private fun QuestionScreenPreview() {
  TheQuietHourTheme { QuestionScreen(PREVIEW_PASSAGE) }
}
