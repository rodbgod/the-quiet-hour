package br.com.rodsil.quiethour.content

import kotlinx.serialization.Serializable

@Serializable
data class Passage(
  val id: String,
  val text: String,
  val author: String,
  val work: String,
  val reference: String,
  val translation: String,
  val question: String,
  val context: String,
)
