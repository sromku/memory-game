package com.snatik.matches.game.minigame

/** What every mini-game tells the game flow: how an answer went, whether it is done, what it earned. */
interface MiniGameRules {

    enum class Answer {
        /** The right choice; the rules moved on (a step, a turn or the whole game). */
        RIGHT,
        /** A wrong choice; the rules count a mistake and the turn goes on or starts over. */
        WRONG,
        /** Nothing to answer. */
        IGNORED,
    }

    /** The player chose something: a card, a character, a position; what it means is the game's. */
    fun answer(choice: Int): Answer

    val isOver: Boolean
    val stars: Int
}
