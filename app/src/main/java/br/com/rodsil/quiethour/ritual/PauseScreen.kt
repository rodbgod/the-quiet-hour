package br.com.rodsil.quiethour.ritual

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.rodsil.quiethour.R

// ponytail: fixed until the settings screen (M3) makes it 30 / 60 / 120 / 180 s.
const val PAUSE_DURATION_MS = 60_000L

private const val BREATHE_IN_MS = 4_000
private const val BREATH_CYCLE_MS = 10_000
private const val EXHALED_SCALE = 0.55f
private const val BREATH_ALPHA = 0.18f
private const val RING_ALPHA = 0.35f
private val PAUSE_CIRCLE_SIZE = 240.dp
private val RING_STROKE = 2.dp
private val SCREEN_PADDING = 32.dp

/**
 * Timed on the monotonic clock from a start instant that survives configuration changes and process death. Frames
 * stop while the app is in the background, so the pause only completes in the foreground.
 */
@Composable
fun PauseScreen(question: String, onFinished: () -> Unit, onSkip: () -> Unit, onAbandon: () -> Unit) {
  val startedAt = rememberSaveable { SystemClock.elapsedRealtime() }
  val currentOnFinished by rememberUpdatedState(onFinished)
  val progress by
    produceState(0f, startedAt) {
      while (value < 1f) {
        withFrameMillis { value = ((SystemClock.elapsedRealtime() - startedAt).toFloat() / PAUSE_DURATION_MS).coerceAtMost(1f) }
      }
      currentOnFinished()
    }
  var confirmingExit by rememberSaveable { mutableStateOf(false) }

  KeepScreenOn()
  BackHandler { confirmingExit = true }

  Column(Modifier.fillMaxSize().safeDrawingPadding().padding(SCREEN_PADDING)) {
    Text(question, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Box(Modifier.weight(1f).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
      BreathingCircle()
      CircularProgressIndicator(
        progress = { progress },
        modifier = Modifier.size(PAUSE_CIRCLE_SIZE),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = RING_ALPHA),
        strokeWidth = RING_STROKE,
        trackColor = Color.Transparent,
      )
    }
    TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.End)) { Text(stringResource(R.string.pause_skip)) }
  }

  if (confirmingExit) {
    AlertDialog(
      onDismissRequest = { confirmingExit = false },
      title = { Text(stringResource(R.string.pause_exit_title)) },
      text = { Text(stringResource(R.string.pause_exit_message)) },
      confirmButton = { TextButton(onClick = onAbandon) { Text(stringResource(R.string.pause_exit_confirm)) } },
      dismissButton = { TextButton(onClick = { confirmingExit = false }) { Text(stringResource(R.string.pause_exit_stay)) } },
    )
  }
}

@Composable
private fun BreathingCircle() {
  val scale by
    rememberInfiniteTransition(label = "breath")
      .animateFloat(
        initialValue = EXHALED_SCALE,
        targetValue = EXHALED_SCALE,
        animationSpec =
          infiniteRepeatable(
            keyframes {
              durationMillis = BREATH_CYCLE_MS
              EXHALED_SCALE at 0 using FastOutSlowInEasing
              1f at BREATHE_IN_MS using FastOutSlowInEasing
            }
          ),
        label = "breath scale",
      )
  val color = MaterialTheme.colorScheme.onSurface.copy(alpha = BREATH_ALPHA)
  val description = stringResource(R.string.pause_breathing_description)
  Canvas(Modifier.size(PAUSE_CIRCLE_SIZE).semantics { contentDescription = description }) {
    drawCircle(color, radius = size.minDimension / 2 * scale)
  }
}

@Composable
private fun KeepScreenOn() {
  val view = LocalView.current
  DisposableEffect(view) {
    view.keepScreenOn = true
    onDispose { view.keepScreenOn = false }
  }
}
