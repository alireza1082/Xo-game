package ir.sharif.xo

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.sharif.xo.audio.SoundManager
import ir.sharif.xo.data.GamePreferences
import ir.sharif.xo.engine.CellState
import ir.sharif.xo.engine.GameMode
import ir.sharif.xo.engine.GameResult
import ir.sharif.xo.engine.Player
import ir.sharif.xo.engine.TicTacToeAi
import ir.sharif.xo.engine.TicTacToeGame
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameBoardViewModel(
    val gameMode: GameMode,
    val humanPlayer: Player = Player.X,
    private val preferences: GamePreferences? = null,
    private val soundManager: SoundManager? = null
) : ViewModel() {
    internal val game = TicTacToeGame(gameMode).apply { startNewRound(Player.X) }
    val aiPlayer: Player = humanPlayer.opponent()

    private val _uiState = MutableStateFlow(game.toUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<GameEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private var aiJob: Job? = null
    private var resultJob: Job? = null
    private var aiGeneration = 0L
    private var roundCount = 0

    init {
        if (preferences != null) {
            viewModelScope.launch {
                preferences.settings.collect { settings ->
                    soundManager?.isSoundEnabled = settings.isSoundEnabled
                    _uiState.update {
                        it.copy(
                            isSoundEnabled = settings.isSoundEnabled,
                            isHapticEnabled = settings.isHapticEnabled
                        )
                    }
                }
            }
        }
        if (preferences != null && gameMode.isAiMode && game.activePlayer == aiPlayer) {
            scheduleAiMove()
        }
    }

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.CellClicked -> onCellClicked(event.index)
            GameEvent.NewRound -> startNewRound()
            GameEvent.ResetScore -> resetScore()
            GameEvent.ToggleSound -> toggleSound()
        }
    }

    private fun onCellClicked(index: Int) {
        if (uiState.value.isInputLocked || game.gameResult.isGameOver) return
        if (game.gameMode.isAiMode && game.activePlayer != humanPlayer) return
        makeMove(index)
    }

    private fun makeMove(index: Int, scheduleNextAi: Boolean = true): Boolean {
        if (!game.makeMove(index)) return false
        soundManager?.playMove()
        _effects.tryEmit(GameEffect.MoveMade)
        publishState()

        if (game.gameResult.isGameOver) {
            finishRound(game.gameResult)
        } else if (scheduleNextAi && game.gameMode.isAiMode && game.activePlayer == aiPlayer) {
            scheduleAiMove()
        }
        return true
    }

    private fun scheduleAiMove() {
        aiJob?.cancel()
        val generation = ++aiGeneration
        _uiState.update { it.copy(isInputLocked = true) }
        aiJob = viewModelScope.launch {
            delay(AI_MOVE_DELAY_MS)
            if (!isActive || generation != aiGeneration) return@launch
            if (!game.gameResult.isGameOver && game.activePlayer == aiPlayer) {
                val move = TicTacToeAi.getBestMove(game, gameMode, aiPlayer)
                if (isActive && generation == aiGeneration && move >= 0) {
                    makeMove(move, scheduleNextAi = false)
                }
            }
            if (isActive && generation == aiGeneration && !game.gameResult.isGameOver) {
                _uiState.update { it.copy(isInputLocked = false) }
            }
        }
    }

    private fun finishRound(result: GameResult) {
        roundCount++
        val winner = result.winner
        _uiState.update { it.copy(isInputLocked = true, result = result.toUiResult(humanPlayer, gameMode)) }
        if (winner != null) {
            _effects.tryEmit(GameEffect.WinLine(winner, result.winningIndices ?: intArrayOf()))
            if (gameMode.isAiMode && winner != humanPlayer) soundManager?.playDraw() else soundManager?.playWin()
        } else {
            soundManager?.playDraw()
        }
        viewModelScope.launch {
            preferences?.recordGameResult(gameMode, humanPlayer, result.winner)
        }
        if (roundCount % 5 == 0) {
            _effects.tryEmit(GameEffect.ShowInterstitialAd)
        }
        resultJob?.cancel()
        resultJob = viewModelScope.launch {
            delay(GAME_OVER_DIALOG_DELAY_MS)
            _effects.emit(GameEffect.ShowResult)
            delay(GAME_OVER_RESULT_VISIBLE_MS)
            _effects.emit(GameEffect.HideResult)
            startNewRound()
        }
    }

    private fun startNewRound() {
        cancelAiMove()
        resultJob?.cancel()
        game.startNewRound(Player.X)
        _uiState.update {
            game.toUiState().copy(
                isInputLocked = false,
                isSoundEnabled = it.isSoundEnabled,
                isHapticEnabled = it.isHapticEnabled
            )
        }
        if (gameMode.isAiMode && game.activePlayer == aiPlayer) scheduleAiMove()
    }

    private fun resetScore() {
        cancelAiMove()
        resultJob?.cancel()
        game.resetAll(Player.X)
        _uiState.update {
            game.toUiState().copy(
                isInputLocked = false,
                isSoundEnabled = it.isSoundEnabled,
                isHapticEnabled = it.isHapticEnabled
            )
        }
        if (gameMode.isAiMode && game.activePlayer == aiPlayer) scheduleAiMove()
    }

    private fun cancelAiMove() {
        aiGeneration++
        aiJob?.cancel()
        aiJob = null
    }

    private fun toggleSound() {
        val enabled = !(_uiState.value.isSoundEnabled)
        soundManager?.isSoundEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
        viewModelScope.launch { preferences?.setSoundEnabled(enabled) }
        _effects.tryEmit(GameEffect.SoundChanged(enabled))
    }

    private fun publishState() {
        _uiState.value = game.toUiState().copy(
            isInputLocked = _uiState.value.isInputLocked,
            isSoundEnabled = _uiState.value.isSoundEnabled,
            isHapticEnabled = _uiState.value.isHapticEnabled
        )
    }

    private fun TicTacToeGame.toUiState(): GameUiState = GameUiState(
        board = board.map { it.toUiCell() },
        activePlayer = activePlayer,
        result = gameResult.toUiResult(humanPlayer, gameMode),
        xScore = xScore,
        oScore = oScore,
        drawScore = drawScore,
        isInputLocked = false,
        isSoundEnabled = true,
        isHapticEnabled = true
    )

    override fun onCleared() {
        cancelAiMove()
        resultJob?.cancel()
    }

    companion object {
        private const val AI_MOVE_DELAY_MS = 400L
        private const val GAME_OVER_DIALOG_DELAY_MS = 700L
        private const val GAME_OVER_RESULT_VISIBLE_MS = 2_000L

        fun factory(
            gameMode: GameMode,
            humanPlayer: Player,
            preferences: GamePreferences,
            soundManager: SoundManager? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GameBoardViewModel(gameMode, humanPlayer, preferences, soundManager) as T
        }
    }
}

sealed interface GameEvent {
    data object NewRound : GameEvent
    data object ResetScore : GameEvent
    data object ToggleSound : GameEvent
    data class CellClicked(val index: Int) : GameEvent
}

@Immutable
data class GameUiState(
    val board: List<UiCell> = List(9) { UiCell.Empty },
    val activePlayer: Player = Player.X,
    val result: UiResult = UiResult.InProgress,
    val xScore: Int = 0,
    val oScore: Int = 0,
    val drawScore: Int = 0,
    val isInputLocked: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true
)

@Immutable
data class UiCell(val player: Player?) {
    val isEmpty: Boolean get() = player == null
    companion object { val Empty = UiCell(null) }
}

sealed interface UiResult {
    data object InProgress : UiResult
    data object Draw : UiResult
    data class Winner(val player: Player, val isHumanWinner: Boolean) : UiResult
}

sealed interface GameEffect {
    data object MoveMade : GameEffect
    data object ShowResult : GameEffect
    data object HideResult : GameEffect
    data object ShowVideoAd : GameEffect
    data object ShowInterstitialAd : GameEffect
    data class WinLine(val player: Player, val indices: IntArray) : GameEffect
    data class SoundChanged(val enabled: Boolean) : GameEffect
}

private fun CellState.toUiCell(): UiCell = when (this) {
    CellState.X -> UiCell(Player.X)
    CellState.O -> UiCell(Player.O)
    CellState.EMPTY -> UiCell.Empty
}

private fun GameResult.toUiResult(humanPlayer: Player, gameMode: GameMode): UiResult {
    if (status == GameResult.Status.DRAW) return UiResult.Draw
    val roundWinner = winner ?: return UiResult.InProgress
    return UiResult.Winner(roundWinner, !gameMode.isAiMode || roundWinner == humanPlayer)
}
