package br.com.rodsil.quiethour.data

import br.com.rodsil.quiethour.content.ContentPack
import br.com.rodsil.quiethour.content.EXCLUSION_WINDOW_DAYS
import br.com.rodsil.quiethour.content.Passage
import br.com.rodsil.quiethour.content.selectPassageId
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PassageRepository
@Inject
constructor(
  private val contentPack: ContentPack,
  private val deliveredPassages: DeliveredPassageDao,
  private val installId: InstallId,
) {
  /** Returns the passage delivered on [date], selecting and persisting one on first request so it never changes. */
  suspend fun passageFor(date: LocalDate): Passage =
    withContext(Dispatchers.IO) {
      val passagesById = contentPack.passages.associateBy { it.id }
      deliveredPassages.passageIdOn(date.toEpochDay())?.let(passagesById::get)?.let {
        return@withContext it
      }

      val deliveries =
        deliveredPassages
          .deliveredBetween(date.minusDays(EXCLUSION_WINDOW_DAYS).toEpochDay(), date.toEpochDay())
          .associate { LocalDate.ofEpochDay(it.epochDay) to it.passageId }
      val passageId = selectPassageId(installId.value(), date, passagesById.keys.toList(), deliveries)
      deliveredPassages.insert(DeliveredPassage(date.toEpochDay(), passageId))
      passagesById.getValue(passageId)
    }
}
