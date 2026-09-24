package br.com.rodsil.quiethour.content

import java.time.LocalDate

const val EXCLUSION_WINDOW_DAYS = 180L

/**
 * Picks the passage for [date], deterministic per install. Passages delivered in the preceding window are excluded;
 * the window shrinks to the pack size minus one so a small pack always leaves at least one candidate.
 */
fun selectPassageId(
  installId: String,
  date: LocalDate,
  passageIds: List<String>,
  deliveries: Map<LocalDate, String>,
): String {
  val windowStart = date.minusDays(minOf(EXCLUSION_WINDOW_DAYS, passageIds.size - 1L))
  val recentlyDelivered = deliveries.filterKeys { it >= windowStart && it < date }.values.toSet()
  val candidates = passageIds.sorted().filterNot { it in recentlyDelivered }
  return candidates[Math.floorMod("$installId$date".hashCode(), candidates.size)]
}
