package br.com.rodsil.quiethour.data

import br.com.rodsil.quiethour.ritual.Streak
import br.com.rodsil.quiethour.ritual.streakOn
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RitualRepository @Inject constructor(private val records: RitualRecordDao) {
  /** Idempotent: the first completion of a day is kept, so redoing the ritual never changes the record. */
  suspend fun complete(date: LocalDate, passageId: String, pauseSkipped: Boolean) =
    withContext(Dispatchers.IO) { records.insertCompletedDay(CompletedDay(date.toEpochDay(), passageId, pauseSkipped)) }

  suspend fun saveReflection(date: LocalDate, passageId: String, text: String) =
    withContext(Dispatchers.IO) { records.upsertReflection(Reflection(date.toEpochDay(), passageId, text)) }

  suspend fun streak(today: LocalDate): Streak =
    withContext(Dispatchers.IO) { streakOn(today, records.completedEpochDays().map(LocalDate::ofEpochDay).toSet()) }
}
