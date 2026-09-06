package com.snatik.matches.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snatik.matches.audio.SoundPlayer
import com.snatik.matches.data.GamePreferences
import com.snatik.matches.game.Board
import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.Game
import com.snatik.matches.game.GameEngine
import com.snatik.matches.game.GameResult
import com.snatik.matches.game.GameTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Owns the game flow: theme and difficulty selection, the round in progress, its clock, and the
 * timing of everything that happens after a flip. Survives configuration changes.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    /** Screen-level effects, consumed by the activity. */
    sealed interface UiEvent {
        data object OpenThemeSelect : UiEvent
        data object OpenDifficultySelect : UiEvent
        data object OpenGame : UiEvent
        data object ReturnToDifficultySelect : UiEvent
        data object ShowSettings : UiEvent
        data class ShowWon(val result: GameResult) : UiEvent
        data object ClosePopup : UiEvent
    }

    /** Effects on the board of the current round, consumed by the game screen. */
    sealed interface BoardEvent {
        /** The second card of a pair just matched; the pair is still shown for a moment. */
        data class Matched(val first: Int, val second: Int) : BoardEvent
        data class HidePair(val first: Int, val second: Int) : BoardEvent
        data class FlipDown(val first: Int, val second: Int) : BoardEvent
        data class Won(val result: GameResult) : BoardEvent
    }

    private val preferences = GamePreferences(application)
    private val sounds = SoundPlayer(application)

    private val _selectedTheme = MutableStateFlow<GameTheme?>(null)
    val selectedTheme: StateFlow<GameTheme?> = _selectedTheme.asStateFlow()

    private val _soundEnabled = MutableStateFlow(preferences.soundEnabled)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val uiEvents = Channel<UiEvent>(Channel.BUFFERED)
    val uiEventFlow = uiEvents.receiveAsFlow()

    private val boardEvents = Channel<BoardEvent>(Channel.BUFFERED)
    val boardEventFlow = boardEvents.receiveAsFlow()

    var game: Game? = null
        private set

    private var clockJob: Job? = null

    /** When the app left the screen mid-round, so the pause does not count against the player. */
    private var pausedAtMillis: Long? = null

    /** Parent of every delayed effect of the current round, cancelled when a new round starts. */
    private var roundJob: Job = Job()

    fun highStars(theme: GameTheme, difficulty: Difficulty) = preferences.highStars(theme, difficulty)

    fun bestTimeSeconds(theme: GameTheme, difficulty: Difficulty) = preferences.bestTimeSeconds(theme, difficulty)

    fun averageStars(theme: GameTheme) = preferences.averageStars(theme)

    fun startPressed() {
        uiEvents.trySend(UiEvent.OpenThemeSelect)
    }

    fun selectTheme(theme: GameTheme) {
        _selectedTheme.value = theme
        uiEvents.trySend(UiEvent.OpenDifficultySelect)
    }

    /** Called when the theme screen is showing, so the background returns to the default art. */
    fun clearTheme() {
        _selectedTheme.value = null
        game = null
    }

    fun selectDifficulty(difficulty: Difficulty) {
        val theme = _selectedTheme.value ?: return
        startRound(theme, difficulty)
        uiEvents.trySend(UiEvent.OpenGame)
    }

    fun openSettings() {
        uiEvents.trySend(UiEvent.ShowSettings)
    }

    /** From the "won" popup: same level again, or the next one after a three-star round. */
    fun nextGame() {
        val finished = game ?: return
        uiEvents.trySend(UiEvent.ClosePopup)
        val difficulty = if (finished.result?.stars == GameResult.MAX_STARS) {
            finished.difficulty.next ?: finished.difficulty
        } else {
            finished.difficulty
        }
        selectDifficulty(difficulty)
    }

    fun backToDifficultySelect() {
        uiEvents.trySend(UiEvent.ClosePopup)
        uiEvents.trySend(UiEvent.ReturnToDifficultySelect)
    }

    /** The activity is no longer visible: stop the clock so an interruption does not cost stars. */
    fun onScreenHidden() {
        val game = game ?: return
        if (game.result != null || pausedAtMillis != null) return
        pausedAtMillis = SystemClock.elapsedRealtime()
        clockJob?.cancel()
    }

    fun onScreenShown() {
        val pausedAt = pausedAtMillis ?: return
        pausedAtMillis = null
        val game = game ?: return
        game.startedAtMillis += SystemClock.elapsedRealtime() - pausedAt
        startClock(game)
    }

    fun toggleSound(): Boolean {
        val enabled = !_soundEnabled.value
        _soundEnabled.value = enabled
        preferences.soundEnabled = enabled
        return enabled
    }

    fun playStarSound() {
        if (_soundEnabled.value) sounds.playStar()
    }

    /**
     * The player tapped [tile]. Returns true when the tile should flip face up; the consequences
     * (hiding a matched pair, flipping a mismatch back, winning) follow as [BoardEvent]s.
     */
    fun flipTile(tile: Int): Boolean {
        val game = game ?: return false
        if (game.result != null) return false
        when (val flip = game.engine.flip(tile)) {
            GameEngine.Flip.Ignored -> return false
            is GameEngine.Flip.First -> Unit
            is GameEngine.Flip.Match -> {
                boardEvents.trySend(BoardEvent.Matched(flip.first, flip.second))
                launchInRound {
                    delay(RESOLVE_DELAY_MS)
                    game.engine.resolve()
                    boardEvents.send(BoardEvent.HidePair(flip.first, flip.second))
                    if (_soundEnabled.value) sounds.playCorrect()
                }
                if (flip.complete) finishRound(game)
            }
            is GameEngine.Flip.Mismatch -> launchInRound {
                delay(RESOLVE_DELAY_MS)
                game.engine.resolve()
                boardEvents.send(BoardEvent.FlipDown(flip.first, flip.second))
            }
        }
        return true
    }

    private fun startRound(theme: GameTheme, difficulty: Difficulty) {
        roundJob.cancel()
        roundJob = Job()
        pausedAtMillis = null
        while (boardEvents.tryReceive().isSuccess) Unit // drop effects of the previous round
        val round = Game(
            theme = theme,
            difficulty = difficulty,
            board = Board.create(difficulty.tileCount, theme.characters.indices.toList()),
            startedAtMillis = SystemClock.elapsedRealtime(),
        )
        game = round
        startClock(round)
    }

    private fun finishRound(game: Game) {
        clockJob?.cancel()
        val result = GameResult.compute(game.difficulty, game.theme.id, passedSeconds(game))
        game.result = result
        preferences.recordResult(game.theme, game.difficulty, result.stars, result.passedSeconds)
        launchInRound {
            delay(WON_DELAY_MS)
            boardEvents.send(BoardEvent.Won(result))
            uiEvents.send(UiEvent.ShowWon(result))
        }
    }

    private fun startClock(game: Game) {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                val remaining = game.difficulty.timeSeconds - passedSeconds(game)
                _remainingSeconds.value = remaining.coerceAtLeast(0)
                if (remaining <= 0) break
                val elapsed = SystemClock.elapsedRealtime() - game.startedAtMillis
                delay(1000 - elapsed % 1000)
            }
        }
    }

    private fun passedSeconds(game: Game): Int =
        ((SystemClock.elapsedRealtime() - game.startedAtMillis) / 1000).toInt()

    private fun launchInRound(block: suspend () -> Unit) {
        viewModelScope.launch(roundJob) { block() }
    }

    override fun onCleared() {
        sounds.release()
    }

    private companion object {
        /** Time the second card stays visible before a pair is hidden or a mismatch flipped back. */
        const val RESOLVE_DELAY_MS = 1000L

        /** Delay between the last match and the "won" popup. */
        const val WON_DELAY_MS = 1200L
    }
}
