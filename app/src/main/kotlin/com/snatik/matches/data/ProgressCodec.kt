package com.snatik.matches.data

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.RoundResult
import com.snatik.matches.game.progression.RoundSpec

/**
 * The on-disk form of [Progress]: a header line, then one line per round with a result.
 *
 *     memory-game-progress 1
 *     <difficulty level> <round index> <stars> <best time seconds>
 *
 * Plain text keeps it readable in a bug report and needs no library. Unknown lines are skipped and
 * a damaged file decodes to whatever could be read, so progress degrades instead of disappearing.
 */
object ProgressCodec {
    private const val HEADER = "memory-game-progress"
    const val VERSION = 1

    fun encode(progress: Progress): String = buildString {
        appendLine("$HEADER $VERSION")
        progress.entries.entries
            .sortedWith(compareBy({ it.key.difficulty.level }, { it.key.index }))
            .forEach { (round, result) -> appendLine("${round.difficulty.level} ${round.index} ${result.stars} ${result.bestTimeSeconds}") }
    }

    fun decode(text: String): Progress {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (lines.isEmpty() || !lines.first().startsWith(HEADER)) return Progress.EMPTY
        val results = lines.drop(1).mapNotNull(::decodeLine).toMap()
        return Progress.of(results)
    }

    private fun decodeLine(line: String): Pair<RoundSpec, RoundResult>? {
        val parts = line.split(' ')
        if (parts.size != 4) return null
        return runCatching {
            val difficulty = Difficulty.fromLevel(parts[0].toInt())
            RoundSpec(difficulty, parts[1].toInt()) to RoundResult(parts[2].toInt(), parts[3].toInt())
        }.getOrNull()
    }
}
