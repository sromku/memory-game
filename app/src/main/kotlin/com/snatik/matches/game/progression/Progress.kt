package com.snatik.matches.game.progression

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameResult

/** The best result ever achieved on one round. */
data class RoundResult(val stars: Int, val bestTimeSeconds: Int) {
    init {
        require(stars in 0..GameResult.MAX_STARS) { "stars must be 0..${GameResult.MAX_STARS}, was $stars" }
        require(bestTimeSeconds >= 0) { "bestTimeSeconds must not be negative" }
    }

    /** The better of two results: more stars wins; equal stars keep the faster time. */
    fun improvedBy(other: RoundResult): RoundResult = when {
        other.stars > stars -> other
        other.stars < stars -> this
        else -> copy(bestTimeSeconds = minOf(bestTimeSeconds, other.bestTimeSeconds))
    }
}

/**
 * Everything the player has achieved on the roads, as an immutable value. Recording a result
 * returns a new [Progress]; all questions about unlocking and what to play next are answered here.
 */
class Progress private constructor(private val results: Map<RoundSpec, RoundResult>) {

    fun resultOf(round: RoundSpec): RoundResult? = results[round]

    /** Rounds with a result, in road order. */
    fun completedRounds(difficulty: Difficulty): List<RoundSpec> =
        results.keys.filter { it.difficulty == difficulty }.sortedBy { it.index }

    fun completedCount(difficulty: Difficulty): Int = results.keys.count { it.difficulty == difficulty }

    fun starsOn(difficulty: Difficulty): Int = results.entries.filter { it.key.difficulty == difficulty }.sumOf { it.value.stars }

    val totalStars: Int get() = results.values.sumOf { it.stars }

    /** The first difficulty is always open; each next one opens after enough rounds of the previous. */
    fun isUnlocked(difficulty: Difficulty): Boolean {
        val previous = Difficulty.entries.getOrNull(difficulty.ordinal - 1) ?: return true
        return completedCount(previous) >= Road.ROUNDS_TO_UNLOCK_NEXT
    }

    fun isCompleted(round: RoundSpec): Boolean = round in results

    /** The first round on the road without a result, or null when the road is finished. */
    fun nextRound(difficulty: Difficulty): RoundSpec? = Road.rounds(difficulty).firstOrNull { it !in results }

    /** What "just play" picks: the next round of the highest unlocked difficulty that still has rounds left. */
    fun quickPlayRound(): RoundSpec? =
        Difficulty.entries.reversed().filter(::isUnlocked).firstNotNullOfOrNull(::nextRound)

    fun record(round: RoundSpec, result: RoundResult): Progress {
        val best = results[round]?.improvedBy(result) ?: result
        return Progress(results + (round to best))
    }

    /** Every result, for storage. */
    val entries: Map<RoundSpec, RoundResult> get() = results

    override fun equals(other: Any?) = other is Progress && other.results == results
    override fun hashCode() = results.hashCode()

    companion object {
        val EMPTY = Progress(emptyMap())

        fun of(results: Map<RoundSpec, RoundResult>) = Progress(results.toMap())
    }
}
