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
    private val wrongId = pool.load(context, R.raw.wrong, 1)
    private val noteIds = listOf(R.raw.note_1, R.raw.note_2, R.raw.note_3, R.raw.note_4, R.raw.note_5, R.raw.note_6)
        .map { pool.load(context, it, 1) }

    /** How many different notes the party can sing. */
    val noteCount: Int get() = noteIds.size

    fun playCorrect() = play(correctId)

    fun playStar() = play(starId)

    fun playWrong() = play(wrongId)

    /** One of the pentatonic notes, 0 being the lowest. */
    fun playNote(index: Int) = play(noteIds[index.coerceIn(noteIds.indices)])

    private fun play(soundId: Int) {
        pool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() = pool.release()
}
