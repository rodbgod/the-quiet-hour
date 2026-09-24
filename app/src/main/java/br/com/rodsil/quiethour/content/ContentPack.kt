package br.com.rodsil.quiethour.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

const val CONTENT_PACK_FILE = "passages.json"

private val PACK_JSON = Json { ignoreUnknownKeys = true }

fun parsePassages(json: String): List<Passage> = PACK_JSON.decodeFromString(json)

/** Loads whichever pack the APK was built with; the ritual engine never depends on what the passages are about. */
@Singleton
class ContentPack @Inject constructor(@ApplicationContext private val context: Context) {
  val passages: List<Passage> by lazy {
    parsePassages(context.assets.open(CONTENT_PACK_FILE).bufferedReader().use { it.readText() })
  }
}
