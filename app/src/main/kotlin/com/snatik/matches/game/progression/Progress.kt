package com.snatik.matches.game.progression

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
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
 * Roads are per theme: what is done in one theme opens nothing in another.
 */
class Progress private constructor(private val results: Map<RoundSpec, RoundResult>) {

    fun resultOf(round: RoundSpec): RoundResult? = results[round]

    private fun onRoad(theme: GameTheme, difficulty: Difficulty) =
        results.entries.filter { it.key.theme == theme && it.key.difficulty == difficulty }

    /** Rounds with a result, in road order. */
    fun completedRounds(theme: GameTheme, difficulty: Difficulty): List<RoundSpec> =
        onRoad(theme, difficulty).map { it.key }.sortedBy { it.index }

    fun completedCount(theme: GameTheme, difficulty: Difficulty): Int = onRoad(theme, difficulty).size

    fun starsOn(theme: GameTheme, difficulty: Difficulty): Int = onRoad(theme, difficulty).sumOf { it.value.stars }

    val totalStars: Int get() = results.values.sumOf { it.stars }

    /** How the road is going at a glance: the rounded mean stars of its played rounds, 0 when none. */
    fun averageStars(theme: GameTheme, difficulty: Difficulty): Int = roundedMean(onRoad(theme, difficulty).map { it.value.stars })

    /** How the theme is going at a glance, over all its roads. */
    fun themeStars(theme: GameTheme): Int = roundedMean(results.entries.filter { it.key.theme == theme }.map { it.value.stars })

    private fun roundedMean(stars: List<Int>): Int = if (stars.isEmpty()) 0 else (stars.sum() * 2 + stars.size) / (2 * stars.size)

    /**
     * The first difficulty of every theme is open; each next one opens after enough rounds of the
     * previous one in that theme. A road that was ever played (results carried over from 1.x, say)
     * stays open.
     */
    fun isUnlocked(theme: GameTheme, difficulty: Difficulty): Boolean {
        val previous = Difficulty.entries.getOrNull(difficulty.ordinal - 1) ?: return true
        return completedCount(theme, difficulty) > 0 || completedCount(theme, previous) >= Road.ROUNDS_TO_UNLOCK_NEXT
    }

    fun isCompleted(round: RoundSpec): Boolean = round in results

    /** The first round on the road without a result, or null when the road is finished. */
    fun nextRound(theme: GameTheme, difficulty: Difficulty): RoundSpec? =
        Road.rounds(theme, difficulty).firstOrNull { it !in results }

    /** Where play is in a theme: the next round of its highest open road that still has rounds left. */
    fun quickPlayRound(theme: GameTheme): RoundSpec? =
        Difficulty.entries.reversed().filter { isUnlocked(theme, it) }.firstNotNullOfOrNull { nextRound(theme, it) }

    /**
     * The theme's friends: one character joins the album for every [Friends.ROUNDS_PER_FRIEND]
     * rounds done on any of the theme's roads, in the theme's fixed order.
     */
    fun friendsOf(theme: GameTheme): List<Int> = Friends.order(theme).take(friendsEarned(theme))

    private fun friendsEarned(theme: GameTheme): Int = Difficulty.entries.sumOf { completedCount(theme, it) / Friends.ROUNDS_PER_FRIEND }

    /**
     * The fewest rounds that still have to be played for the character [image] of [theme] to join
     * the album: the rounds to the next friend on the road closest to giving one, plus a full
     * batch for every friend queued ahead of it. Null when it is already a friend.
     */
    fun roundsToFriend(theme: GameTheme, image: Int): Int? {
        val earned = friendsEarned(theme)
        val position = Friends.order(theme).indexOf(image)
        if (position < earned) return null
        val toNext = Difficulty.entries.filter { isUnlocked(theme, it) }
            .minOf { Friends.ROUNDS_PER_FRIEND - completedCount(theme, it) % Friends.ROUNDS_PER_FRIEND }
        return toNext + (position - earned) * Friends.ROUNDS_PER_FRIEND
    }

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
