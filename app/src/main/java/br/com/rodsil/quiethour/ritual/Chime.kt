package br.com.rodsil.quiethour.ritual

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import br.com.rodsil.quiethour.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private val CHIME_ATTRIBUTES: AudioAttributes =
  AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    .build()

class Chime @Inject constructor(@ApplicationContext private val context: Context) {
  fun play() {
    MediaPlayer.create(context, R.raw.chime, CHIME_ATTRIBUTES, AudioManager.AUDIO_SESSION_ID_GENERATE)?.run {
      setOnCompletionListener { it.release() }
      start()
    }
  }
}
