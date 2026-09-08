package com.snatik.matches.ui.minigame

import com.snatik.matches.R
import com.snatik.matches.game.minigame.FollowTheSong
import com.snatik.matches.game.minigame.FollowTheSong.Outcome
import com.snatik.matches.game.minigame.MiniGameRules
import kotlinx.coroutines.delay

/** "Follow the song": the party sings, the child taps the singers in the same order, the song grows. */
class FollowTheSongFragment : PartyGameFragment() {

    override fun plays(rules: MiniGameRules) = rules is FollowTheSong

    override suspend fun play() {
        val rules = mini.rules as FollowTheSong
        // The singers: a few of the theme's characters, the same ones for the whole round.
        val singers = theme.characters.indices.shuffled(kotlin.random.Random(mini.startedAtMillis)).take(rules.partySize)
        val cast = cast(singers)
        scene.setParty(singers.map(cast::forScene))
        scene.enter()
        progressDots.set(rules.song.size, rules.length - 1)
        if (rules.isOver) return // back after a configuration change with the round already finished
        delay(ENTRANCE_MS)

        var listenAgain = false
        while (!rules.isOver) {
            // Listen!
            prompt(if (listenAgain) R.string.song_again else R.string.song_listen)
            progressDots.set(rules.song.size, rules.length - 1)
            delay(PROMPT_MS)
            for (singer in rules.currentSequence) {
                scene.spotlight(singer, NOTE_MS)
                viewModel.sing(singer)
                delay(NOTE_MS + NOTE_GAP_MS)
            }

            // Your turn!
            prompt(R.string.song_your_turn)
            listenAgain = false
            answering@ while (true) {
                val singer = awaitSceneTap()
                val outcome = viewModel.tapSinger(singer)
                if (outcome != Outcome.MISTAKE && outcome != Outcome.IGNORED) scene.spotlight(singer, NOTE_MS)
                when (outcome) {
                    Outcome.CONTINUE, Outcome.IGNORED -> Unit
                    Outcome.TURN_COMPLETE, Outcome.SONG_COMPLETE -> {
                        progressDots.set(rules.song.size, rules.length - 1)
                        delay(JOY_DELAY_MS)
                        for (position in 0 until rules.partySize) {
                            scene.spotlight(position, NOTE_MS)
                            delay(JOY_STAGGER_MS)
                        }
                        delay(JOY_SHORT_MS)
                        break@answering
                    }
                    Outcome.MISTAKE -> {
                        listenAgain = true
                        delay(MISTAKE_MS)
                        break@answering
                    }
                }
            }
        }
        finish()
    }

    private companion object {
        const val ENTRANCE_MS = 1100L
        const val PROMPT_MS = 700L
        const val NOTE_MS = 550L
        const val NOTE_GAP_MS = 200L
        const val JOY_DELAY_MS = 400L
        const val JOY_STAGGER_MS = 120L
        const val JOY_SHORT_MS = 900L
        const val MISTAKE_MS = 1100L
    }
}
