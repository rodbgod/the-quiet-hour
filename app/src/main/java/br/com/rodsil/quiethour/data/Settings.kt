package br.com.rodsil.quiethour.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppTheme {
  SYSTEM,
  LIGHT,
  DARK,
  SEPIA,
}

enum class TextSize(val scale: Float) {
  SMALL(0.9f),
  DEFAULT(1f),
  LARGE(1.15f),
}

val PAUSE_DURATIONS_SECONDS = listOf(30, 60, 120, 180)
private const val DEFAULT_PAUSE_SECONDS = 60
private val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(7, 0)

data class Settings(
  val pauseSeconds: Int = DEFAULT_PAUSE_SECONDS,
  val chime: Boolean = false,
  val reminderTime: LocalTime = DEFAULT_REMINDER_TIME,
  val theme: AppTheme = AppTheme.SYSTEM,
  val textSize: TextSize = TextSize.DEFAULT,
)

private val PAUSE_SECONDS = intPreferencesKey("pause_seconds")
private val CHIME = booleanPreferencesKey("chime")
private val REMINDER_MINUTE_OF_DAY = intPreferencesKey("reminder_minute_of_day")
private val THEME = stringPreferencesKey("theme")
private val TEXT_SIZE = stringPreferencesKey("text_size")

class SettingsRepository @Inject constructor(private val preferences: DataStore<Preferences>) {
  val settings: Flow<Settings> = preferences.data.map { it.toSettings() }

  suspend fun update(transform: (Settings) -> Settings) {
    preferences.edit { stored ->
      val updated = transform(stored.toSettings())
      stored[PAUSE_SECONDS] = updated.pauseSeconds
      stored[CHIME] = updated.chime
      stored[REMINDER_MINUTE_OF_DAY] = updated.reminderTime.toSecondOfDay() / SECONDS_PER_MINUTE
      stored[THEME] = updated.theme.name
      stored[TEXT_SIZE] = updated.textSize.name
    }
  }
}

private const val SECONDS_PER_MINUTE = 60

private fun Preferences.toSettings(): Settings {
  val defaults = Settings()
  return Settings(
    pauseSeconds = this[PAUSE_SECONDS]?.takeIf { it in PAUSE_DURATIONS_SECONDS } ?: defaults.pauseSeconds,
    chime = this[CHIME] ?: defaults.chime,
    reminderTime =
      this[REMINDER_MINUTE_OF_DAY]?.let { LocalTime.ofSecondOfDay(it.toLong() * SECONDS_PER_MINUTE) }
        ?: defaults.reminderTime,
    theme = AppTheme.entries.firstOrNull { it.name == this[THEME] } ?: defaults.theme,
    textSize = TextSize.entries.firstOrNull { it.name == this[TEXT_SIZE] } ?: defaults.textSize,
  )
}
