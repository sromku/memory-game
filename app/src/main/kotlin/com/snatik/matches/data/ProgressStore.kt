package com.snatik.matches.data

import android.content.Context
import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.RoundResult
import com.snatik.matches.game.progression.RoundSpec
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the player's [Progress]: loads it once, exposes it as state, and writes every change to
 * disk atomically (temporary file, then rename) so a crash cannot leave a half-written file.
 *
 * On the first run with this store, results from the 1.x preference keys are carried over so
 * existing players keep their stars.
 */
class ProgressStore(
    private val file: File,
    /** Best 1.x result per difficulty, consulted only when no progress file exists yet. */
    private val legacy: (Difficulty) -> RoundResult?,
    private val scope: CoroutineScope,
    /** Where file writes run; tests pass their test dispatcher so writes finish deterministically. */
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    constructor(context: Context, scope: CoroutineScope) :
        this(File(context.filesDir, FILE_NAME), GamePreferences(context)::bestAcrossThemes, scope)

    private val writes = Mutex()
    private val _progress = MutableStateFlow(load())
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    /** Records a finished round; the new best is kept in memory at once and written in the background. */
    fun record(round: RoundSpec, result: RoundResult) {
        val updated = _progress.value.record(round, result)
        if (updated == _progress.value) return
        _progress.value = updated
        persist(updated)
    }

    private fun load(): Progress {
        if (file.exists()) {
            return runCatching { ProgressCodec.decode(file.readText()) }.getOrDefault(Progress.EMPTY)
        }
        val migrated = migrate()
        if (migrated != Progress.EMPTY) persist(migrated)
        return migrated
    }

    /** 1.x kept best stars and time per difficulty; that result becomes the difficulty's round 1. */
    private fun migrate(): Progress =
        Difficulty.entries.fold(Progress.EMPTY) { progress, difficulty ->
            legacy(difficulty)?.let { progress.record(RoundSpec(difficulty, 1), it) } ?: progress
        }

    private fun persist(progress: Progress) {
        scope.launch(ioDispatcher) { writes.withLock { write(progress) } }
    }

    private fun write(progress: Progress) {
        val temp = File(file.parentFile, "$FILE_NAME.tmp")
        temp.writeText(ProgressCodec.encode(progress))
        if (!temp.renameTo(file)) {
            file.delete()
            temp.renameTo(file)
        }
    }

    companion object {
        const val FILE_NAME = "progress.txt"
    }
}
