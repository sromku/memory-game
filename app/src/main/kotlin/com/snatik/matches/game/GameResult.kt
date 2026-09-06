package com.snatik.matches.game

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
         * Stars and score for finishing a [difficulty] board in [passedSeconds].
         *
         * Star thresholds are the ones the game shipped with: three stars within half the time,
         * two within 80%, one before the clock runs out, none after. [themeMultiplier] keeps the
         * original score formula, where each theme multiplies the score by its id.
         */
        fun compute(difficulty: Difficulty, themeMultiplier: Int, passedSeconds: Int): GameResult {
            val total = difficulty.timeSeconds
            val remaining = (total - passedSeconds).coerceAtLeast(0)
            val stars = when {
                passedSeconds <= total / 2 -> 3
                passedSeconds <= total - total / 5 -> 2
                passedSeconds < total -> 1
                else -> 0
            }
            return GameResult(
                stars = stars,
                score = difficulty.level * remaining * themeMultiplier,
                remainingSeconds = remaining,
                passedSeconds = passedSeconds,
            )
        }
    }
}
