package br.com.rodsil.quiethour.journal

import br.com.rodsil.quiethour.content.Passage
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val DAY = LocalDate.of(2026, 9, 25)

class JournalTest {

  @Test
  fun `returns every entry for a blank query`() {
    val entries = listOf(entry(DAY, reflection = "calm"), entry(DAY.minusDays(1), reflection = null))

    assertEquals(entries, entries.matching("  ", favoritesOnly = false))
  }

  @Test
  fun `finds entries by reflection text regardless of case`() {
    val match = entry(DAY, reflection = "I blamed my brother for the delay")
    val entries = listOf(match, entry(DAY.minusDays(1), reflection = "quiet morning"))

    assertEquals(listOf(match), entries.matching("BROTHER", favoritesOnly = false))
  }

  @Test
  fun `finds entries by author and question when no reflection was written`() {
    val seneca = entry(DAY, author = "Seneca", question = "Where did your time go?", reflection = null)
    val entries = listOf(seneca, entry(DAY.minusDays(1), author = "Epictetus"))

    assertEquals(listOf(seneca), entries.matching("seneca", favoritesOnly = false))
    assertEquals(listOf(seneca), entries.matching("time go", favoritesOnly = false))
  }

  @Test
  fun `limits results to favorites when asked`() {
    val favorite = entry(DAY, favorite = true)
    val entries = listOf(favorite, entry(DAY.minusDays(1), favorite = false))

    assertEquals(listOf(favorite), entries.matching("", favoritesOnly = true))
  }

  @Test
  fun `exports completed days oldest first with passage, question and reflection`() {
    val older = entry(DAY.minusDays(1), question = "First question?", reflection = "First answer")
    val newer = entry(DAY, question = "Second question?", reflection = null)

    val markdown = journalMarkdown(listOf(newer, older))

    assertTrue(markdown.indexOf("## ${older.date}") < markdown.indexOf("## ${newer.date}"))
    assertTrue(markdown.contains("**First question?**\n\nFirst answer"))
    assertTrue(markdown.contains("_No reflection written._"))
    assertTrue(markdown.contains("> Marcus Aurelius, Meditations, Book IV.7"))
  }

  @Test
  fun `leaves a missed yesterday out of the export`() {
    val missed = entry(DAY.minusDays(1), completed = false)

    assertFalse(journalMarkdown(listOf(missed)).contains("## ${missed.date}"))
  }
}

private fun entry(
  date: LocalDate,
  author: String = "Marcus Aurelius",
  question: String = "What harm are you carrying?",
  reflection: String? = "A reflection",
  favorite: Boolean = false,
  completed: Boolean = true,
) =
  JournalEntry(
    date = date,
    passage =
      Passage(
        id = "passage-$date",
        text = "Take away thy opinion.",
        author = author,
        work = "Meditations",
        reference = "Book IV.7",
        translation = "George Long (1862)",
        question = question,
        context = "Context.",
      ),
    reflection = reflection,
    favorite = favorite,
    completed = completed,
  )
