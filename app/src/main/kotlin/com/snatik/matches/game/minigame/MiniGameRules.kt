package com.snatik.matches.game.minigame

/** What every mini-game tells the game flow: whether it is done and what it earned. */
interface MiniGameRules {
    val isOver: Boolean
    val stars: Int
}
