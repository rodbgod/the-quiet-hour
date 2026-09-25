package br.com.rodsil.quiethour.ritual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
private val SECTION_GAP = 16.dp
private const val REFLECTION_MIN_LINES = 3
private const val REFLECTION_MAX_LINES = 10

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
fun QuestionScreen(passage: Passage, onBegin: () -> Unit) {
  Column(Modifier.fillMaxSize().safeDrawingPadding().padding(SCREEN_PADDING)) {
    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
      Text(
        passage.question,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.semantics { heading() },
      )
    }
    TextButton(onClick = onBegin, modifier = Modifier.align(Alignment.End)) {
      Text(stringResource(R.string.question_begin_pause))
    }
  }
}

@Composable
fun ReflectionScreen(question: String, onDone: (String) -> Unit) {
  var reflection by rememberSaveable { mutableStateOf("") }
  Column(
    Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(SCREEN_PADDING),
    verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
  ) {
    Text(question, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    OutlinedTextField(
      value = reflection,
      onValueChange = { reflection = it },
      modifier = Modifier.fillMaxWidth(),
      label = { Text(stringResource(R.string.reflection_label)) },
      textStyle = MaterialTheme.typography.bodyMedium,
      minLines = REFLECTION_MIN_LINES,
      maxLines = REFLECTION_MAX_LINES,
    )
    Text(
      stringResource(R.string.reflection_privacy),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.weight(1f))
    TextButton(onClick = { onDone(reflection) }, modifier = Modifier.align(Alignment.End)) {
      Text(stringResource(R.string.reflection_done))
    }
  }
}

@Composable
fun CloseScreen(streak: Streak, onClose: () -> Unit) {
  Column(
    Modifier.fillMaxSize().safeDrawingPadding().padding(SCREEN_PADDING),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    if (streak is Streak.Active) {
      Text(
        stringResource(R.string.close_streak_day, streak.days),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.semantics { heading() },
      )
    }
    Text(stringResource(R.string.close_see_you), style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(ATTRIBUTION_GAP))
    TextButton(onClick = onClose) { Text(stringResource(R.string.close_done)) }
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
  TheQuietHourTheme { QuestionScreen(PREVIEW_PASSAGE, onBegin = {}) }
}
