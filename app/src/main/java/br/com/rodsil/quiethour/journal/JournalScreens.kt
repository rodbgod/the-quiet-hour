package br.com.rodsil.quiethour.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.rodsil.quiethour.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val SCREEN_PADDING = 24.dp
private val ROW_PADDING = 16.dp
private val SECTION_GAP = 24.dp
private const val REFLECTION_PREVIEW_LINES = 2
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
fun JournalScreen(
  entries: List<JournalEntry>?,
  query: String,
  favoritesOnly: Boolean,
  onSearch: (String) -> Unit,
  onFavoritesOnlyChange: (Boolean) -> Unit,
  onExport: () -> Unit,
  onOpen: (JournalEntry) -> Unit,
) {
  Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = SCREEN_PADDING)) {
    Row(Modifier.fillMaxWidth().padding(top = SCREEN_PADDING), verticalAlignment = Alignment.CenterVertically) {
      Text(
        stringResource(R.string.journal_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.weight(1f).semantics { heading() },
      )
      TextButton(onClick = onExport) { Text(stringResource(R.string.journal_export)) }
    }
    OutlinedTextField(
      value = query,
      onValueChange = onSearch,
      modifier = Modifier.fillMaxWidth(),
      label = { Text(stringResource(R.string.journal_search)) },
      singleLine = true,
      textStyle = MaterialTheme.typography.bodyMedium,
    )
    FilterChip(
      selected = favoritesOnly,
      onClick = { onFavoritesOnlyChange(!favoritesOnly) },
      label = { Text(stringResource(R.string.journal_favorites_only)) },
    )
    if (entries?.isEmpty() == true) {
      val message = if (query.isBlank() && !favoritesOnly) R.string.journal_empty else R.string.journal_no_matches
      Text(
        stringResource(message),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = ROW_PADDING),
      )
    }
    LazyColumn(Modifier.fillMaxSize()) {
      items(entries.orEmpty(), key = { it.date.toEpochDay() }) { entry ->
        JournalRow(entry, onOpen = { onOpen(entry) })
        HorizontalDivider()
      }
    }
  }
}

@Composable
private fun JournalRow(entry: JournalEntry, onOpen: () -> Unit) {
  Column(
    Modifier.fillMaxWidth()
      .clickable(onClickLabel = stringResource(R.string.journal_open_entry), onClick = onOpen)
      .padding(vertical = ROW_PADDING),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(entryHeading(entry), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(entry.passage.question, style = MaterialTheme.typography.bodyMedium)
    entry.reflection?.let {
      Text(
        it,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = REFLECTION_PREVIEW_LINES,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun entryHeading(entry: JournalEntry): String {
  val parts =
    listOfNotNull(
      entry.date.format(DATE_FORMAT),
      entry.passage.author,
      stringResource(R.string.journal_missed).takeUnless { entry.completed },
      stringResource(R.string.journal_favorite_marker).takeIf { entry.favorite },
    )
  return parts.joinToString(" · ")
}

@Composable
fun JournalEntryScreen(entry: JournalEntry?, onFavoriteChange: (Boolean) -> Unit) {
  if (entry == null) return
  var showContext by rememberSaveable { mutableStateOf(false) }
  val passage = entry.passage
  Column(
    Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(SCREEN_PADDING),
    verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
  ) {
    Text(entryHeading(entry), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Column {
      Text(passage.text, style = MaterialTheme.typography.bodyLarge)
      Spacer(Modifier.height(ROW_PADDING))
      Text("${passage.work}, ${passage.reference}", style = MaterialTheme.typography.bodyMedium)
      Text(passage.translation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      TextButton(onClick = { showContext = !showContext }) {
        Text(stringResource(if (showContext) R.string.entry_hide_context else R.string.entry_show_context))
      }
      if (showContext) Text(passage.context, style = MaterialTheme.typography.bodyMedium)
    }
    Text(passage.question, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
    Text(
      entry.reflection ?: stringResource(if (entry.completed) R.string.entry_no_reflection else R.string.entry_missed_day),
      style = MaterialTheme.typography.bodyMedium,
      color = if (entry.reflection == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
    )
    TextButton(onClick = { onFavoriteChange(!entry.favorite) }) {
      Text(stringResource(if (entry.favorite) R.string.entry_remove_favorite else R.string.entry_add_favorite))
    }
  }
}
