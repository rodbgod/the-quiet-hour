package br.com.rodsil.quiethour.ritual

import java.time.LocalDate

sealed interface Streak {
  data object None : Streak

  data class Active(val days: Int, val lastCompletedDate: LocalDate) : Streak

  data class AtRisk(val days: Int, val lastCompletedDate: LocalDate) : Streak

  data class Broken(val previousDays: Int, val brokenAt: LocalDate) : Streak
}

/**
 * Derives the streak from local completion dates alone, so timezone, DST and clock changes can only move "today",
 * never corrupt stored state. A completion dated after [today] (clock moved back) still counts as active.
 */
// ponytail: crossing the date line eastward can skip a local date and break the streak; add a grace day if reported.
fun streakOn(today: LocalDate, completedDates: Set<LocalDate>): Streak {
  val lastCompleted = completedDates.maxOrNull() ?: return Streak.None
  val days = generateSequence(lastCompleted) { it.minusDays(1) }.takeWhile { it in completedDates }.count()
  return when {
    lastCompleted >= today -> Streak.Active(days, lastCompleted)
    lastCompleted == today.minusDays(1) -> Streak.AtRisk(days, lastCompleted)
    else -> Streak.Broken(days, lastCompleted.plusDays(1))
  }
}
