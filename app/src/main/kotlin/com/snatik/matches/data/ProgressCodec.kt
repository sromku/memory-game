package com.snatik.matches.data

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.fromId
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.RoundResult
import com.snatik.matches.game.progression.RoundSpec

/**
 * The on-disk form of [Progress]: a header line, then one line per round with a result.
 *
 *     memory-game-progress 2
 *     <theme id> <difficulty level> <round index> <stars> <best time seconds>
 *
 * Version 1 had no theme (roads were per difficulty only); its lines are read as the animals
 * theme. Plain text keeps it readable in a bug report and needs no library. Unknown lines are
 * skipped and a damaged file decodes to whatever could be read, so progress degrades instead of
 * disappearing.
 */
object ProgressCodec {
    private const val HEADER = "memory-game-progress"
    const val VERSION = 2

    fun encode(progress: Progress): String = buildString {
        appendLine("$HEADER $VERSION")
        progress.entries.entries
            .sortedWith(compareBy({ it.key.theme.id }, { it.key.difficulty.level }, { it.key.index }))
            .forEach { (round, result) ->
                appendLine("${round.theme.id} ${round.difficulty.level} ${round.index} ${result.stars} ${result.bestTimeSeconds}")
            }
    }

    fun decode(text: String): Progress {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (lines.isEmpty() || !lines.first().startsWith(HEADER)) return Progress.EMPTY
        val results = lines.drop(1).mapNotNull(::decodeLine).toMap()
        return Progress.of(results)
    }

    private fun decodeLine(line: String): Pair<RoundSpec, RoundResult>? {
        val parts = line.split(' ')
        return runCatching {
            when (parts.size) {
                5 -> RoundSpec(GameTheme.fromId(parts[0].toInt())!!, Difficulty.fromLevel(parts[1].toInt()), parts[2].toInt()) to
                    RoundResult(parts[3].toInt(), parts[4].toInt())
                4 -> RoundSpec(GameTheme.ANIMALS, Difficulty.fromLevel(parts[0].toInt()), parts[1].toInt()) to
                    RoundResult(parts[2].toInt(), parts[3].toInt())
                else -> null
            }
        }.getOrNull()
    }
}
