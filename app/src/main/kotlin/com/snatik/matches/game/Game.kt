package com.snatik.matches.game

/** One round in progress (or just finished, once [result] is set). */
class Game(
    val theme: GameTheme,
    val difficulty: Difficulty,
    val board: Board,
    /** SystemClock.elapsedRealtime() when the round started. */
    val startedAtMillis: Long,
) {
    val engine = GameEngine(board)

    var result: GameResult? = null
        internal set
}
