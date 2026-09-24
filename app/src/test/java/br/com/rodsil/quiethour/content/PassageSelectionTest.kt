package br.com.rodsil.quiethour.content

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val INSTALL_ID = "3f1c2a9e-install"
private val START = LocalDate.of(2026, 1, 1)

class PassageSelectionTest {

  @Test
  fun `returns the same passage for the same install and date`() {
    val ids = passageIds(400)

    assertEquals(
      selectPassageId(INSTALL_ID, START, ids, emptyMap()),
      selectPassageId(INSTALL_ID, START, ids.shuffled(), emptyMap()),
    )
  }

  @Test
  fun `varies the passage across installs on the same date`() {
    val ids = passageIds(400)

    val picks = (1..20).map { selectPassageId("install-$it", START, ids, emptyMap()) }.toSet()

    assertTrue("expected variety, got $picks", picks.size > 1)
  }

  @Test
  fun `never repeats a passage within 180 days when the pack is large enough`() {
    val deliveries = deliverDaily(passageIds(400), days = 365)

    deliveries.values.toList().windowed(EXCLUSION_WINDOW_DAYS.toInt() + 1).forEach { window ->
      assertEquals(window.size, window.toSet().size)
    }
  }

  @Test
  fun `never repeats a passage within the pack size when the pack is smaller than the window`() {
    val packSize = 30
    val deliveries = deliverDaily(passageIds(packSize), days = 120)

    deliveries.values.toList().windowed(packSize).forEach { window -> assertEquals(window.size, window.toSet().size) }
  }

  @Test
  fun `ignores deliveries on or after the requested date`() {
    val ids = passageIds(2)
    val today = selectPassageId(INSTALL_ID, START, ids, emptyMap())

    val yesterday = selectPassageId(INSTALL_ID, START.minusDays(1), ids, mapOf(START to today))
    val yesterdayWithoutToday = selectPassageId(INSTALL_ID, START.minusDays(1), ids, emptyMap())

    assertEquals(yesterdayWithoutToday, yesterday)
  }

  @Test
  fun `excludes the passage delivered yesterday even in a two passage pack`() {
    val ids = passageIds(2)
    val yesterday = selectPassageId(INSTALL_ID, START, ids, emptyMap())

    val today = selectPassageId(INSTALL_ID, START.plusDays(1), ids, mapOf(START to yesterday))

    assertNotEquals(yesterday, today)
  }
}

private fun passageIds(count: Int) = (1..count).map { "passage-$it" }

private fun deliverDaily(ids: List<String>, days: Int): Map<LocalDate, String> {
  val deliveries = linkedMapOf<LocalDate, String>()
  repeat(days) { offset ->
    val date = START.plusDays(offset.toLong())
    deliveries[date] = selectPassageId(INSTALL_ID, date, ids, deliveries)
  }
  return deliveries
}
