package br.com.rodsil.quiethour.journal

import br.com.rodsil.quiethour.content.Passage
import java.time.LocalDate

/** [completed] is false only for a missed yesterday, which stays readable but never counts toward the streak. */
data class JournalEntry(
  val date: LocalDate,
  val passage: Passage,
  val reflection: String?,
  val favorite: Boolean,
  val completed: Boolean,
)

fun List<JournalEntry>.matching(query: String, favoritesOnly: Boolean): List<JournalEntry> {
  val needle = query.trim()
  return filter { entry ->
    (!favoritesOnly || entry.favorite) &&
      (needle.isEmpty() ||
        listOfNotNull(entry.reflection, entry.passage.question, entry.passage.text, entry.passage.author).any {
          it.contains(needle, ignoreCase = true)
        })
  }
}

fun journalMarkdown(entries: List<JournalEntry>): String = buildString {
  appendLine("# The Quiet Hour: Journal")
  entries
    .filter { it.completed }
    .sortedBy { it.date }
    .forEach { entry ->
      val passage = entry.passage
      appendLine()
      appendLine("## ${entry.date}")
      appendLine()
      passage.text.lines().forEach { appendLine("> $it") }
      appendLine(">")
      appendLine("> ${passage.author}, ${passage.work}, ${passage.reference}")
      appendLine()
      appendLine("**${passage.question}**")
      appendLine()
      appendLine(entry.reflection ?: "_No reflection written._")
    }
}
