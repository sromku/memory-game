package com.snatik.matches.game.minigame

import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random

/**
 * Rules of "Shadow match": a character stands as a black shadow, three cards show characters, and
 * the player picks whose shadow it is. Characters are the theme's image indices.
 */
class ShadowMatch private constructor(val turns: List<Turn>) : MiniGameRules {

    /** [choices] holds [target] once; the others are look-alikes only in that they are not it. */
    class Turn(val target: Int, val choices: List<Int>) {
        init {
            require(choices.count { it == target } == 1) { "the target is offered once" }
        }
    }

    var turnIndex: Int = 0
        private set

    var mistakes: Int = 0
        private set

    val currentTurn: Turn? get() = turns.getOrNull(turnIndex)

    override val isOver: Boolean get() = turnIndex >= turns.size

    override val stars: Int get() = (3 - mistakes).coerceAtLeast(1)

    override fun answer(choice: Int): MiniGameRules.Answer {
        val turn = currentTurn ?: return MiniGameRules.Answer.IGNORED
        if (choice != turn.target) {
            mistakes++
            return MiniGameRules.Answer.WRONG
        }
        turnIndex++
        return MiniGameRules.Answer.RIGHT
    }

    companion object {
        const val CHOICES = 3

        /** Three shadows at first, four from round 50. */
        fun turns(round: RoundSpec): Int = if (round.index < 50) 3 else 4

        fun create(round: RoundSpec, characterCount: Int, random: Random = Random.Default): ShadowMatch {
            require(characterCount >= CHOICES) { "three characters are needed for the cards" }
            val turns = List(turns(round)) {
                val order = (0 until characterCount).shuffled(random)
                val target = order[0]
                Turn(target, order.take(CHOICES).shuffled(random))
            }
            return ShadowMatch(turns)
        }
    }
}
