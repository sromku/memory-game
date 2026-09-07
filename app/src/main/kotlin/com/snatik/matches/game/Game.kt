package com.snatik.matches.game

import com.snatik.matches.game.progression.RoundSpec

/** One round in progress (or just finished, once [result] is set). */
class Game(
    /** The round on its road this game counts for; it sets the theme, the board and the time. */
    val round: RoundSpec,
    val board: Board,
    /** SystemClock.elapsedRealtime() when the round started, shifted forward by any time spent paused. */
    startedAtMillis: Long,
) {
    val theme: GameTheme get() = round.theme
    val difficulty: Difficulty get() = round.difficulty

    var startedAtMillis: Long = startedAtMillis
        internal set

    val engine = GameEngine(board)

    var result: GameResult? = null
        internal set
}
