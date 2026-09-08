package com.snatik.matches.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snatik.matches.audio.SoundPlayer
import com.snatik.matches.data.GamePreferences
import com.snatik.matches.data.ProgressStore
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.RoundResult
import com.snatik.matches.game.progression.RoundSpec
import com.snatik.matches.game.Board
import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.Game
import com.snatik.matches.game.GameEngine
import com.snatik.matches.game.GameResult
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.minigame.FollowTheSong
import com.snatik.matches.game.minigame.MiniGameRules
import com.snatik.matches.game.minigame.OddOneOut
import com.snatik.matches.game.minigame.PeekAndFind
import com.snatik.matches.game.minigame.ShoppingList
import com.snatik.matches.game.minigame.ShadowMatch
import com.snatik.matches.game.minigame.WhatChanged
import com.snatik.matches.game.minigame.WhoWasHere
import com.snatik.matches.game.progression.MiniGame as MiniGameKind
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
        data object OpenThemePicker : UiEvent
        data object OpenDifficultyPicker : UiEvent
        data object ClosePicker : UiEvent
        data object OpenRoadMap : UiEvent
        data object OpenGame : UiEvent
        data class OpenMiniGame(val kind: MiniGameKind) : UiEvent
        data object ReturnToRoadMap : UiEvent
        data object OpenAlbum : UiEvent
        data object ShowSettings : UiEvent
        data object ShowLanguages : UiEvent
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
    private val progressStore = ProgressStore(application, viewModelScope)
    private val sounds = SoundPlayer(application)

    /** The player's roads. */
    val progress: StateFlow<Progress> = progressStore.progress

    private val _selectedTheme = MutableStateFlow<GameTheme?>(null)
    val selectedTheme: StateFlow<GameTheme?> = _selectedTheme.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    /** The round finished most recently, waiting for the map to celebrate it. */
    private var finishedRound: RoundSpec? = null

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

    /** A special round in progress: a mini-game on a theme, with its rules and its clock. */
    class MiniGame(val round: RoundSpec, val rules: MiniGameRules, val startedAtMillis: Long) {
        val theme: GameTheme get() = round.theme

        var result: GameResult? = null
            internal set
    }

    var miniGame: MiniGame? = null
        private set

    /** The round being played, whichever kind of game it is. */
    private val currentRound: RoundSpec? get() = game?.round ?: miniGame?.round

    private var clockJob: Job? = null

    /** When the app left the screen mid-round, so the pause does not count against the player. */
    private var pausedAtMillis: Long? = null

    /** Parent of every delayed effect of the current round, cancelled when a new round starts. */
    private var roundJob: Job = Job()

    /** The theme the next warm-up and the theme cards are about. */
    val themeForArt: GameTheme get() = _selectedTheme.value ?: preferences.lastTheme ?: GameTheme.ANIMALS

    /** Hands over the round that just finished, once, so the map can celebrate it. */
    fun consumeFinishedRound(): RoundSpec? = finishedRound.also { finishedRound = null }

    /**
     * Play, from the menu: the map of the road the player was on last, with their theme, centred
     * on the next round. A first-time player lands on the road where play is.
     */
    fun play() {
        val theme = preferences.lastTheme ?: GameTheme.ANIMALS
        _selectedTheme.value = theme
        _selectedDifficulty.value = roadToShow(theme, preferences.lastDifficulty)
        uiEvents.trySend(UiEvent.OpenRoadMap)
    }

    /** [wanted] if it is open in [theme], else the theme's road where play is. */
    private fun roadToShow(theme: GameTheme, wanted: Difficulty?): Difficulty {
        val current = progress.value
        return wanted?.takeIf { current.isUnlocked(theme, it) }
            ?: current.quickPlayRound(theme)?.difficulty
            ?: Difficulty.entries.last { current.isUnlocked(theme, it) }
    }

    fun openAlbum() {
        uiEvents.trySend(UiEvent.OpenAlbum)
    }

    fun openThemePicker() {
        uiEvents.trySend(UiEvent.OpenThemePicker)
    }

    fun openDifficultyPicker() {
        uiEvents.trySend(UiEvent.OpenDifficultyPicker)
    }

    /** From the theme picker: the map re-skins and comes back. */
    fun selectTheme(theme: GameTheme) {
        _selectedTheme.value = theme
        preferences.lastTheme = theme
        // Each theme has its own roads: the one shown may not be open here yet.
        _selectedDifficulty.value = roadToShow(theme, _selectedDifficulty.value)
        uiEvents.trySend(UiEvent.ClosePicker)
    }

    /** Called when the theme screen is showing, so the background returns to the default art. */
    fun clearTheme() {
        _selectedTheme.value = null
        game = null
        miniGame = null
    }

    /** From the difficulty picker: the map shows that road and comes back. Roads not open yet are ignored. */
    fun selectDifficulty(difficulty: Difficulty) {
        val theme = _selectedTheme.value ?: return
        if (!progress.value.isUnlocked(theme, difficulty)) return
        _selectedDifficulty.value = difficulty
        preferences.lastDifficulty = difficulty
        uiEvents.trySend(UiEvent.ClosePicker)
    }

    /** Special rounds are mini-games; every other round is a board of cards. */
    fun selectRound(round: RoundSpec) {
        val kind = round.miniGame
        if (kind == null) {
            startRound(round)
            uiEvents.trySend(UiEvent.OpenGame)
            return
        }
        val characters = round.theme.characters.size
        val rules: MiniGameRules = when (kind) {
            MiniGameKind.WHO_WAS_HERE -> WhoWasHere.create(round, characters)
            MiniGameKind.FOLLOW_THE_SONG -> FollowTheSong.create(round)
            MiniGameKind.WHAT_CHANGED -> WhatChanged.create(round, characters)
            MiniGameKind.SHADOW_MATCH -> ShadowMatch.create(round, characters)
            MiniGameKind.ODD_ONE_OUT -> OddOneOut.create(round, characters)
            MiniGameKind.SHOPPING_LIST -> ShoppingList.create(round, characters)
            MiniGameKind.PEEK_AND_FIND -> PeekAndFind.create(round, characters)
        }
        startMiniGame(round, rules)
        uiEvents.trySend(UiEvent.OpenMiniGame(kind))
    }

    /** Answers the mini-game with a card or a character, with the right and wrong sounds. */
    fun answerMiniGame(choice: Int): MiniGameRules.Answer {
        val rules = miniGame?.rules ?: return MiniGameRules.Answer.IGNORED
        val answer = rules.answer(choice)
        if (_soundEnabled.value) when (answer) {
            MiniGameRules.Answer.RIGHT -> sounds.playCorrect()
            MiniGameRules.Answer.WRONG -> sounds.playWrong()
            MiniGameRules.Answer.IGNORED -> Unit
        }
        return answer
    }

    /** The note of a singer at [position] in a party of [partySize], spread over the scale. */
    fun noteOf(position: Int, partySize: Int): Int =
        if (partySize <= 1) 0 else position * (sounds.noteCount - 1) / (partySize - 1)

    /** A singer sings, during the song or when tapped. */
    fun sing(position: Int) {
        val rules = miniGame?.rules as? FollowTheSong ?: return
        if (_soundEnabled.value) sounds.playNote(noteOf(position, rules.partySize))
    }

    /** The player tapped a singer; the note or the "oops" plays here. */
    fun tapSinger(position: Int): FollowTheSong.Outcome {
        val rules = miniGame?.rules as? FollowTheSong ?: return FollowTheSong.Outcome.IGNORED
        val outcome = rules.tap(position)
        if (_soundEnabled.value) {
            if (outcome == FollowTheSong.Outcome.MISTAKE) sounds.playWrong() else if (outcome != FollowTheSong.Outcome.IGNORED) sounds.playNote(noteOf(position, rules.partySize))
        }
        return outcome
    }

    /** The mini-game's last turn is done: stars from its mistakes, recorded like any round. */
    fun finishMiniGame() {
        val mini = miniGame ?: return
        if (mini.result != null || !mini.rules.isOver) return
        val passed = ((SystemClock.elapsedRealtime() - mini.startedAtMillis) / 1000).toInt()
        val stars = mini.rules.stars
        val result = GameResult(stars = stars, score = stars * MINI_GAME_STAR_SCORE * mini.round.difficulty.level, remainingSeconds = 0, passedSeconds = passed)
        mini.result = result
        progressStore.record(mini.round, RoundResult(stars, passed))
        finishedRound = mini.round
        launchInRound {
            delay(WON_DELAY_MS)
            uiEvents.send(UiEvent.ShowWon(result))
        }
    }

    fun openSettings() {
        uiEvents.trySend(UiEvent.ShowSettings)
    }

    fun openLanguages() {
        uiEvents.trySend(UiEvent.ShowLanguages)
    }

    /** From the "won" popup: the next round of the road, or back to the map when the road is done. */
    fun nextGame() {
        val next = currentRound?.next
        if (next == null) backToRoadMap() else {
            uiEvents.trySend(UiEvent.ClosePopup)
            selectRound(next)
        }
    }

    fun backToRoadMap() {
        uiEvents.trySend(UiEvent.ClosePopup)
        uiEvents.trySend(UiEvent.ReturnToRoadMap)
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

    private fun startMiniGame(round: RoundSpec, rules: MiniGameRules) {
        roundJob.cancel()
        roundJob = Job()
        clockJob?.cancel()
        pausedAtMillis = null
        game = null
        miniGame = MiniGame(round, rules, SystemClock.elapsedRealtime())
    }

    private fun startRound(round: RoundSpec) {
        roundJob.cancel()
        roundJob = Job()
        pausedAtMillis = null
        miniGame = null
        while (boardEvents.tryReceive().isSuccess) Unit // drop effects of the previous round
        val started = Game(
            round = round,
            board = Board.create(round.difficulty.tileCount, round.theme.characters.indices.toList()),
            startedAtMillis = SystemClock.elapsedRealtime(),
        )
        game = started
        startClock(started)
    }

    private fun finishRound(game: Game) {
        clockJob?.cancel()
        val result = GameResult.compute(game.round, game.theme.id, passedSeconds(game))
        game.result = result
        preferences.recordResult(game.theme, game.difficulty, result.stars, result.passedSeconds)
        progressStore.record(game.round, RoundResult(result.stars, result.passedSeconds))
        finishedRound = game.round
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
                val remaining = game.round.timeSeconds - passedSeconds(game)
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

        /** Score per star in the mini-game, times the difficulty level. */
        const val MINI_GAME_STAR_SCORE = 100
    }
}
