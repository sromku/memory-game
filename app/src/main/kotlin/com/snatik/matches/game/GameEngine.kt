package com.snatik.matches.game

/**
 * The rules of a single round: which tiles are face up, which are already matched, and what
 * happens when the player flips a tile. Purely synchronous; the timing of the flip-back and
 * hide animations is the caller's business (see [resolve]).
 */
class GameEngine(val board: Board) {

    sealed interface Flip {
        /** The tap was ignored: the tile is already matched or up, or two tiles are waiting to be resolved. */
        data object Ignored : Flip

        /** First tile of an attempt is now face up. */
        data class First(val tile: Int) : Flip

        /** Second tile matched the first. [complete] is true when this was the last pair. */
        data class Match(val first: Int, val second: Int, val complete: Boolean) : Flip

        /** Second tile did not match the first; both should flip back down after [resolve]. */
        data class Mismatch(val first: Int, val second: Int) : Flip
    }

    private val matched = BooleanArray(board.tileCount)
    private var awaitingResolve = false

    /** The single face-up, not yet matched tile, if any. */
    var faceUpTile: Int? = null
        private set

    val matchedPairCount: Int get() = matched.count { it } / 2

    val isComplete: Boolean get() = matchedPairCount == board.tileCount / 2

    fun isMatched(tile: Int): Boolean = matched[tile]

    fun flip(tile: Int): Flip {
        require(tile in 0 until board.tileCount) { "No tile $tile on a ${board.tileCount}-tile board" }
        if (awaitingResolve || matched[tile] || faceUpTile == tile) return Flip.Ignored

        val first = faceUpTile ?: run {
            faceUpTile = tile
            return Flip.First(tile)
        }
        faceUpTile = null
        awaitingResolve = true
        return if (board.isPair(first, tile)) {
            matched[first] = true
            matched[tile] = true
            Flip.Match(first, tile, complete = isComplete)
        } else {
            Flip.Mismatch(first, tile)
        }
    }

    /** Accept flips again after a [Flip.Match] or [Flip.Mismatch] has been shown to the player. */
    fun resolve() {
        awaitingResolve = false
    }
}
