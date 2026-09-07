package com.snatik.matches.game

import com.snatik.matches.game.progression.RoundSpec

/** Outcome of a completed round. */
data class GameResult(
    val stars: Int,
    val score: Int,
    val remainingSeconds: Int,
    val passedSeconds: Int,
) {
    companion object {
        const val MAX_STARS = 3

        /**
         * Stars and score for finishing [round] in [passedSeconds].
         *
         * Star thresholds are the ones the game shipped with, relative to the round's time: three
         * stars within half of it, two within 80%, one before the clock runs out, none after.
         * [themeMultiplier] keeps the original score formula, where each theme multiplies the
         * score by its id.
         */
        fun compute(round: RoundSpec, themeMultiplier: Int, passedSeconds: Int): GameResult {
            val total = round.timeSeconds
            val remaining = (total - passedSeconds).coerceAtLeast(0)
            val stars = when {
                passedSeconds <= total / 2 -> 3
                passedSeconds <= total - total / 5 -> 2
                passedSeconds < total -> 1
                else -> 0
            }
            return GameResult(
                stars = stars,
                score = round.difficulty.level * remaining * themeMultiplier,
                remainingSeconds = remaining,
                passedSeconds = passedSeconds,
            )
        }
    }
}
