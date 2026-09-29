package br.com.rodsil.quiethour.settings

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.rodsil.quiethour.R
import br.com.rodsil.quiethour.data.AppTheme
import br.com.rodsil.quiethour.data.PAUSE_DURATIONS_SECONDS
import br.com.rodsil.quiethour.data.Settings
import br.com.rodsil.quiethour.data.TextSize
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val SCREEN_PADDING = 24.dp
private val SECTION_GAP = 28.dp
private val CHIP_GAP = 8.dp
private const val SECONDS_PER_MINUTE = 60
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

private val THEME_LABELS =
  mapOf(
    AppTheme.SYSTEM to R.string.settings_theme_system,
    AppTheme.LIGHT to R.string.settings_theme_light,
    AppTheme.DARK to R.string.settings_theme_dark,
    AppTheme.SEPIA to R.string.settings_theme_sepia,
  )

private val TEXT_SIZE_LABELS =
  mapOf(
    TextSize.SMALL to R.string.settings_text_small,
    TextSize.DEFAULT to R.string.settings_text_default,
    TextSize.LARGE to R.string.settings_text_large,
  )

@Composable
fun SettingsScreen(settings: Settings, onChange: ((Settings) -> Settings) -> Unit) {
  val context = LocalContext.current
  Column(
    Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(SCREEN_PADDING),
    verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
  ) {
    Text(
      stringResource(R.string.settings_title),
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )

    Section(R.string.settings_pause) {
      Choices(PAUSE_DURATIONS_SECONDS, selected = settings.pauseSeconds, label = { pauseLabel(it) }) { seconds ->
        onChange { it.copy(pauseSeconds = seconds) }
      }
    }

    Row(
      Modifier.fillMaxWidth().toggleable(value = settings.chime, role = Role.Switch) { chime ->
        onChange { it.copy(chime = chime) }
      },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(stringResource(R.string.settings_chime), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
      Switch(checked = settings.chime, onCheckedChange = null)
    }

    Section(R.string.settings_reminder) {
      TextButton(
        onClick = {
          val time = settings.reminderTime
          TimePickerDialog(
              context,
              { _, hour, minute -> onChange { it.copy(reminderTime = LocalTime.of(hour, minute)) } },
              time.hour,
              time.minute,
              DateFormat.is24HourFormat(context),
            )
            .show()
        }
      ) {
        Text(settings.reminderTime.format(TIME_FORMAT))
      }
    }

    Section(R.string.settings_theme) {
      Choices(AppTheme.entries, selected = settings.theme, label = { stringResource(THEME_LABELS.getValue(it)) }) { theme ->
        onChange { it.copy(theme = theme) }
      }
    }

    Section(R.string.settings_text_size) {
      Choices(TextSize.entries, selected = settings.textSize, label = { stringResource(TEXT_SIZE_LABELS.getValue(it)) }) {
        size ->
        onChange { it.copy(textSize = size) }
      }
    }
  }
}

@Composable
private fun Section(@StringRes title: Int, content: @Composable () -> Unit) {
  Column {
    Text(stringResource(title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
    content()
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Choices(options: List<T>, selected: T, label: @Composable (T) -> String, onSelect: (T) -> Unit) {
  FlowRow(horizontalArrangement = Arrangement.spacedBy(CHIP_GAP)) {
    options.forEach { option ->
      FilterChip(selected = option == selected, onClick = { onSelect(option) }, label = { Text(label(option)) })
    }
  }
}

@Composable
private fun pauseLabel(seconds: Int): String =
  if (seconds < SECONDS_PER_MINUTE) stringResource(R.string.settings_pause_seconds, seconds)
  else stringResource(R.string.settings_pause_minutes, seconds / SECONDS_PER_MINUTE)
