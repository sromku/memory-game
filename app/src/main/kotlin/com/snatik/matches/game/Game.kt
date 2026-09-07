package com.snatik.matches.game

import com.snatik.matches.game.progression.RoundSpec

/** One round in progress (or just finished, once [result] is set). */
class Game(
    val theme: GameTheme,
    val difficulty: Difficulty,
    /** The round on the difficulty's road this game counts for. */
    val round: RoundSpec,
    val board: Board,
    /** SystemClock.elapsedRealtime() when the round started, shifted forward by any time spent paused. */
    startedAtMillis: Long,
) {
    var startedAtMillis: Long = startedAtMillis
        internal set

    val engine = GameEngine(board)

    var result: GameResult? = null
        internal set
}
