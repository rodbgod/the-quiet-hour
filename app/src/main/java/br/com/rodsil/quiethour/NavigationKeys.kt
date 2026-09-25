package br.com.rodsil.quiethour

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object PassageStep : NavKey

@Serializable data object QuestionStep : NavKey

@Serializable data object PauseStep : NavKey

@Serializable data object ReflectionStep : NavKey

@Serializable data object CloseStep : NavKey
