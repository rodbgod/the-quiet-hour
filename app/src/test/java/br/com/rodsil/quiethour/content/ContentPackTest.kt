package br.com.rodsil.quiethour.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentPackTest {
  private val passages = parsePassages(File("src/main/assets/$CONTENT_PACK_FILE").readText())

  @Test
  fun `bundles at least one passage`() {
    assertTrue(passages.isNotEmpty())
  }

  @Test
  fun `gives every passage a unique id`() {
    assertEquals(passages.size, passages.map { it.id }.toSet().size)
  }

  @Test
  fun `ships every passage with its text, attribution, question and context`() {
    passages.forEach { passage ->
      listOf(
          passage.text,
          passage.author,
          passage.work,
          passage.reference,
          passage.translation,
          passage.question,
          passage.context,
        )
        .forEach { field -> assertTrue("blank field in ${passage.id}", field.isNotBlank()) }
    }
  }
}
