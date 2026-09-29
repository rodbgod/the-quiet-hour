package br.com.rodsil.quiethour.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import br.com.rodsil.quiethour.content.ContentPack
import br.com.rodsil.quiethour.content.Passage
import br.com.rodsil.quiethour.journal.JournalEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

private const val EXPORT_DIRECTORY = "exports"
private const val EXPORT_FILE = "the-quiet-hour-journal.md"
private const val EXPORT_AUTHORITY_SUFFIX = ".exports"

class JournalRepository
@Inject
constructor(
  @ApplicationContext private val context: Context,
  private val records: RitualRecordDao,
  private val contentPack: ContentPack,
  private val passages: PassageRepository,
) {
  /** Completed days, newest first, plus yesterday's passage when yesterday was missed. */
  fun journal(today: LocalDate): Flow<List<JournalEntry>> =
    combine(flow { emit(missedYesterday(today)) }, records.journal(), records.favoritePassageIds()) {
        yesterday,
        rows,
        favoriteIds ->
        val passagesById = contentPack.passages.associateBy { it.id }
        val favorites = favoriteIds.toSet()
        val completed =
          rows.mapNotNull { row ->
            passagesById[row.passageId]?.let {
              JournalEntry(LocalDate.ofEpochDay(row.epochDay), it, row.reflection, it.id in favorites, completed = true)
            }
          }
        val missed =
          yesterday?.let {
            JournalEntry(today.minusDays(1), it, reflection = null, favorite = it.id in favorites, completed = false)
          }
        (completed + listOfNotNull(missed)).sortedByDescending { it.date }
      }
      .flowOn(Dispatchers.IO)

  suspend fun setFavorite(passageId: String, favorite: Boolean) =
    withContext(Dispatchers.IO) {
      if (favorite) records.insertFavorite(Favorite(passageId)) else records.deleteFavorite(passageId)
    }

  /** Writes the export to the cache and returns a content URI the share sheet can read. */
  suspend fun exportFile(markdown: String): Uri =
    withContext(Dispatchers.IO) {
      val file = File(File(context.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }, EXPORT_FILE)
      file.writeText(markdown)
      FileProvider.getUriForFile(context, "${context.packageName}$EXPORT_AUTHORITY_SUFFIX", file)
    }

  private suspend fun missedYesterday(today: LocalDate): Passage? {
    val yesterday = today.minusDays(1)
    val completed = withContext(Dispatchers.IO) { records.isCompleted(yesterday.toEpochDay()) }
    return if (completed) null else passages.passageFor(yesterday)
  }
}
