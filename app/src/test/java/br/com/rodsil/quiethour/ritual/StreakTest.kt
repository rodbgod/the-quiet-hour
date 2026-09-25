package br.com.rodsil.quiethour.ritual

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

private val TODAY = LocalDate.of(2026, 9, 25)

class StreakTest {

  @Test
  fun `has no streak before the first completed ritual`() {
    assertEquals(Streak.None, streakOn(TODAY, emptySet()))
  }

  @Test
  fun `counts consecutive completed days ending today`() {
    val completed = daysBefore(TODAY, count = 5)

    assertEquals(Streak.Active(5, TODAY), streakOn(TODAY, completed))
  }

  @Test
  fun `stops counting at the first gap`() {
    val completed = daysBefore(TODAY, count = 3) + TODAY.minusDays(10)

    assertEquals(Streak.Active(3, TODAY), streakOn(TODAY, completed))
  }

  @Test
  fun `is at risk when yesterday was completed but today is not yet`() {
    val yesterday = TODAY.minusDays(1)

    assertEquals(Streak.AtRisk(4, yesterday), streakOn(TODAY, daysBefore(yesterday, count = 4)))
  }

  @Test
  fun `breaks after one missed calendar day`() {
    val twoDaysAgo = TODAY.minusDays(2)

    assertEquals(Streak.Broken(7, TODAY.minusDays(1)), streakOn(TODAY, daysBefore(twoDaysAgo, count = 7)))
  }

  @Test
  fun `stays active when the device clock moves back a day after completing`() {
    val clockMovedBack = TODAY.minusDays(1)

    assertEquals(Streak.Active(2, TODAY), streakOn(clockMovedBack, daysBefore(TODAY, count = 2)))
  }

  @Test
  fun `survives a DST change between two late-night rituals`() {
    val newYork = ZoneId.of("America/New_York")
    val beforeSpringForward = LocalDateTime.of(2026, 3, 7, 23, 50).atZone(newYork).toLocalDate()
    val afterSpringForward = LocalDateTime.of(2026, 3, 8, 23, 50).atZone(newYork).toLocalDate()

    val streak = streakOn(afterSpringForward, setOf(beforeSpringForward, afterSpringForward))

    assertEquals(Streak.Active(2, afterSpringForward), streak)
  }

  @Test
  fun `keeps the streak alive when travel moves the local date back`() {
    val completedInTokyo = LocalDateTime.of(2026, 9, 25, 8, 0).atZone(ZoneId.of("Asia/Tokyo"))
    val nowInLosAngeles = completedInTokyo.plusHours(3).withZoneSameInstant(ZoneId.of("America/Los_Angeles"))

    val streak = streakOn(nowInLosAngeles.toLocalDate(), setOf(completedInTokyo.toLocalDate()))

    assertEquals(Streak.Active(1, completedInTokyo.toLocalDate()), streak)
  }
}

private fun daysBefore(last: LocalDate, count: Int) = (0 until count).map { last.minusDays(it.toLong()) }.toSet()
