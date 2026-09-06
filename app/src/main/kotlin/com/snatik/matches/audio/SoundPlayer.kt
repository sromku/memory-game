package com.snatik.matches.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.snatik.matches.R

/** Short game sound effects, decoded once and played through a [SoundPool]. */
class SoundPlayer(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val correctId = pool.load(context, R.raw.correct_answer, 1)
    private val starId = pool.load(context, R.raw.star, 1)

    fun playCorrect() = play(correctId)

    fun playStar() = play(starId)

    private fun play(soundId: Int) {
        pool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() = pool.release()
}
