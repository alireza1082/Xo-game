package com.example.android.xo

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.android.xo.audio.HapticManager
import com.example.android.xo.audio.SoundManager
import com.example.android.xo.data.GamePreferences
import com.example.android.xo.engine.CellState
import com.example.android.xo.engine.GameMode
import com.example.android.xo.engine.GameResult
import com.example.android.xo.engine.Player
import com.example.android.xo.engine.TicTacToeAi
import com.example.android.xo.engine.TicTacToeGame
import com.example.android.xo.ui.WinningLineView
import com.example.android.xo.util.WindowInsetsUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class GameBoard : AppCompatActivity() {

    private companion object {
        const val AI_MOVE_DELAY_MS = 400L
        const val GAME_OVER_DIALOG_DELAY_MS = 700L
    }

    private lateinit var viewModel: GameBoardViewModel
    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager

    /** Game state, owned by the [viewModel] so it survives configuration changes. */
    private val game: TicTacToeGame
        get() = viewModel.game

    private lateinit var cellViews: Array<ImageView>
    private lateinit var currentPlayerText: TextView
    private lateinit var xPointText: TextView
    private lateinit var oPointText: TextView
    private lateinit var drawPointText: TextView
    private lateinit var playerXTitle: TextView
    private lateinit var playerOTitle: TextView
    private lateinit var winningLineView: WinningLineView
    private lateinit var btnSoundToggle: ImageButton

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingAiRunnable: Runnable? = null
    private var pendingDialogRunnable: Runnable? = null
    private var activeDialog: AlertDialog? = null

    private var isInputLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.board)
        WindowInsetsUtil.applySystemBarInsets(findViewById(R.id.root))

        preferences = GamePreferences(this)
        soundManager = SoundManager(this).apply {
            isSoundEnabled = preferences.isSoundEnabled
        }
        hapticManager = HapticManager(this).apply {
            isHapticEnabled = preferences.isHapticEnabled
        }

        initViews()
        setupBackNavigation()

        var mode = GameMode.TWO_PLAYERS
        intent?.getStringExtra(MainActivity.EXTRA_GAME_MODE)?.let { modeName ->
            runCatching { GameMode.valueOf(modeName) }.getOrNull()?.let { mode = it }
        }

        var humanPlayer = preferences.preferredSymbol
        intent?.getStringExtra(MainActivity.EXTRA_HUMAN_PLAYER)?.let { pName ->
            runCatching { Player.valueOf(pName) }.getOrNull()?.let { humanPlayer = it }
        }

        viewModel = ViewModelProvider(
            this,
            GameBoardViewModel.factory(mode, humanPlayer)
        )[GameBoardViewModel::class.java]

        setupToolbar()
        updateLabelsForGameMode()
        updateFullUi()

        // If it starts or was restored during an AI turn, trigger AI move
        if (game.gameMode.isAiMode && game.activePlayer == viewModel.aiPlayer && !game.gameResult.isGameOver) {
            triggerAiMove()
        }
    }

    private fun initViews() {
        cellViews = arrayOf(
            findViewById(R.id.c0), findViewById(R.id.c1), findViewById(R.id.c2),
            findViewById(R.id.c3), findViewById(R.id.c4), findViewById(R.id.c5),
            findViewById(R.id.c6), findViewById(R.id.c7), findViewById(R.id.c8)
        )

        cellViews.forEachIndexed { index, cellView ->
            cellView.setOnClickListener { onCellClicked(index) }
        }

        currentPlayerText = findViewById(R.id.current_player)
        xPointText = findViewById(R.id.x_point)
        oPointText = findViewById(R.id.o_point)
        drawPointText = findViewById(R.id.draw_point)
        playerXTitle = findViewById(R.id.player_x_title)
        playerOTitle = findViewById(R.id.player_o_title)
        winningLineView = findViewById(R.id.winning_line_view)

        findViewById<MaterialButton>(R.id.btn_play_again).setOnClickListener { startNewGameRound() }
        findViewById<MaterialButton>(R.id.btn_reset_score).setOnClickListener { confirmResetScore() }
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener { finish() }
            title = getModeTitle()
        }

        btnSoundToggle = findViewById(R.id.btn_sound_toggle)
        updateSoundIcon()
        btnSoundToggle.setOnClickListener {
            val newState = !soundManager.isSoundEnabled
            soundManager.isSoundEnabled = newState
            preferences.isSoundEnabled = newState
            updateSoundIcon()
            hapticManager.performTap(it)
            val msg = if (newState) R.string.sound_enabled else R.string.sound_disabled
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSoundIcon() {
        btnSoundToggle.setImageResource(
            if (soundManager.isSoundEnabled) R.drawable.ic_sound_on else R.drawable.ic_sound_off
        )
    }

    private fun getModeTitle(): String = when (game.gameMode) {
        GameMode.TWO_PLAYERS -> getString(R.string.mode_two_player)
        GameMode.VS_AI_EASY ->
            "${getString(R.string.mode_single_player)} (${getString(R.string.difficulty_easy)})"
        GameMode.VS_AI_MEDIUM ->
            "${getString(R.string.mode_single_player)} (${getString(R.string.difficulty_medium)})"
        GameMode.VS_AI_IMPOSSIBLE ->
            "${getString(R.string.mode_single_player)} (${getString(R.string.difficulty_impossible)})"
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun updateLabelsForGameMode() {
        if (game.gameMode.isAiMode) {
            if (viewModel.humanPlayer == Player.X) {
                playerXTitle.setText(R.string.score_you)
                playerOTitle.setText(R.string.score_ai)
            } else {
                playerXTitle.setText(R.string.score_ai)
                playerOTitle.setText(R.string.score_you)
            }
        } else {
            playerXTitle.setText(R.string.cell_x)
            playerOTitle.setText(R.string.cell_o)
        }
    }

    private fun onCellClicked(index: Int) {
        if (isInputLocked) return
        if (game.gameResult.isGameOver) return
        if (game.gameMode.isAiMode && game.activePlayer != viewModel.humanPlayer) return
        if (game.cellAt(index) != CellState.EMPTY) return

        val movingPlayer = game.activePlayer
        if (!game.makeMove(index)) return

        soundManager.playMove()
        hapticManager.performTap(cellViews[index])

        animateCellPlacement(index, movingPlayer)
        handlePostMove()
    }

    private fun handlePostMove() {
        updateScores()
        updateTurnIndicator()

        val result = game.gameResult
        if (result.isGameOver) {
            isInputLocked = true
            handleGameOver(result)
            return
        }

        // If Single Player mode and now AI's turn
        if (game.gameMode.isAiMode && game.activePlayer == viewModel.aiPlayer) {
            triggerAiMove()
        }
    }

    private fun handleGameOver(result: GameResult) {
        // Persist game result in statistics
        preferences.recordGameResult(game.gameMode, viewModel.humanPlayer, result.winner)

        if (result.winner != null) {
            highlightWinningCellsIfAny()
            val lineColor = ContextCompat.getColor(
                this,
                if (result.winner == Player.X) R.color.colorX else R.color.colorO
            )
            winningLineView.startWinAnimation(result.winningIndices, lineColor)

            if (game.gameMode.isAiMode) {
                if (result.winner == viewModel.humanPlayer) {
                    soundManager.playWin()
                    hapticManager.performWin()
                } else {
                    soundManager.playDraw()
                    hapticManager.performTap()
                }
            } else {
                soundManager.playWin()
                hapticManager.performWin()
            }
        } else {
            // Draw
            soundManager.playDraw()
            hapticManager.performTap()
        }

        scheduleGameOverDialog()
    }

    private fun triggerAiMove() {
        isInputLocked = true
        updateTurnIndicator()

        cancelPendingAi()
        val runnable = Runnable {
            if (isFinishing || isDestroyed) return@Runnable
            if (game.gameResult.isGameOver) return@Runnable

            val bestMove = TicTacToeAi.getBestMove(game, game.gameMode, viewModel.aiPlayer)
            if (bestMove >= 0) {
                game.makeMove(bestMove)
                soundManager.playMove()
                hapticManager.performTap(cellViews[bestMove])
                animateCellPlacement(bestMove, viewModel.aiPlayer)
                isInputLocked = false
                handlePostMove()
            } else {
                isInputLocked = false
            }
        }
        pendingAiRunnable = runnable
        mainHandler.postDelayed(runnable, AI_MOVE_DELAY_MS)
    }

    private fun animateCellPlacement(index: Int, player: Player) {
        val cellView = cellViews[index]
        cellView.setImageResource(if (player == Player.X) R.drawable.x else R.drawable.o)
        cellView.contentDescription = getString(
            R.string.cell_description,
            index + 1,
            if (player == Player.X) getString(R.string.cell_x) else getString(R.string.cell_o)
        )

        cellView.scaleX = 0.4f
        cellView.scaleY = 0.4f
        cellView.alpha = 0f
        cellView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(220)
            .start()
    }

    private fun highlightWinningCellsIfAny() {
        game.gameResult.winningIndices?.forEach { idx ->
            val cell = cellViews[idx]
            cell.setBackgroundResource(R.drawable.cell_win_background)
            cell.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(300)
                .withEndAction { cell.animate().scaleX(1f).scaleY(1f).setDuration(200).start() }
                .start()
        }
    }

    private fun scheduleGameOverDialog() {
        cancelPendingDialog()
        val runnable = Runnable {
            if (isFinishing || isDestroyed) return@Runnable
            showGameOverDialog()
        }
        pendingDialogRunnable = runnable
        mainHandler.postDelayed(runnable, GAME_OVER_DIALOG_DELAY_MS)
    }

    private fun showGameOverDialog() {
        dismissActiveDialog()

        val result = game.gameResult
        val message = when (result.status) {
            GameResult.Status.X_WON ->
                if (game.gameMode.isAiMode) {
                    if (viewModel.humanPlayer == Player.X) getString(R.string.winner_you) else getString(R.string.winner_ai)
                } else {
                    getString(R.string.winner_x)
                }
            GameResult.Status.O_WON ->
                if (game.gameMode.isAiMode) {
                    if (viewModel.humanPlayer == Player.O) getString(R.string.winner_you) else getString(R.string.winner_ai)
                } else {
                    getString(R.string.winner_o)
                }
            else -> getString(R.string.game_draw)
        }

        activeDialog = AlertDialog.Builder(this)
            .setTitle(R.string.dialog_game_over_title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.play_again) { _, _ -> startNewGameRound() }
            .setNegativeButton(R.string.exit_to_menu) { _, _ -> finish() }
            .create()
            .apply { show() }
    }

    private fun startNewGameRound() {
        dismissActiveDialog()
        cancelPendingAi()
        cancelPendingDialog()

        winningLineView.clear()

        // X always makes the first move in standard Tic Tac Toe
        val nextStarter = Player.X

        game.startNewRound(nextStarter)
        isInputLocked = false

        resetCellViewsUi()
        updateFullUi()

        if (game.gameMode.isAiMode && game.activePlayer == viewModel.aiPlayer) {
            triggerAiMove()
        }
    }

    private fun resetCellViewsUi() {
        cellViews.forEachIndexed { i, cell ->
            cell.animate().cancel()
            cell.scaleX = 1f
            cell.scaleY = 1f
            cell.alpha = 1f
            cell.setImageDrawable(null)
            cell.setBackgroundResource(R.drawable.cell_background)
            cell.contentDescription = getString(R.string.cell_description, i + 1, getString(R.string.cell_empty))
        }
    }

    private fun confirmResetScore() {
        AlertDialog.Builder(this)
            .setTitle(R.string.reset_scores)
            .setMessage(R.string.reset_scores)
            .setPositiveButton(R.string.play_again) { _, _ ->
                game.resetAll(Player.X)
                startNewGameRound()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateFullUi() {
        updateScores()
        updateBoardUi()
        updateTurnIndicator()
    }

    private fun updateBoardUi() {
        val boardState = game.board
        for (i in 0 until TicTacToeGame.BOARD_SIZE) {
            val cell = cellViews[i]
            when (boardState[i]) {
                CellState.X -> {
                    cell.setImageResource(R.drawable.x)
                    cell.contentDescription = getString(R.string.cell_description, i + 1, getString(R.string.cell_x))
                }
                CellState.O -> {
                    cell.setImageResource(R.drawable.o)
                    cell.contentDescription = getString(R.string.cell_description, i + 1, getString(R.string.cell_o))
                }
                CellState.EMPTY -> {
                    cell.setImageDrawable(null)
                    cell.contentDescription = getString(R.string.cell_description, i + 1, getString(R.string.cell_empty))
                }
            }
            cell.setBackgroundResource(R.drawable.cell_background)
        }

        if (game.gameResult.isGameOver) {
            highlightWinningCellsIfAny()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateScores() {
        xPointText.text = game.xScore.toString()
        oPointText.text = game.oScore.toString()
        drawPointText.text = game.drawScore.toString()
    }

    private fun updateTurnIndicator() {
        val result = game.gameResult
        if (result.isGameOver) {
            currentPlayerText.setText(
                when (result.status) {
                    GameResult.Status.X_WON ->
                        if (game.gameMode.isAiMode) {
                            if (viewModel.humanPlayer == Player.X) R.string.winner_you else R.string.winner_ai
                        } else {
                            R.string.winner_x
                        }
                    GameResult.Status.O_WON ->
                        if (game.gameMode.isAiMode) {
                            if (viewModel.humanPlayer == Player.O) R.string.winner_you else R.string.winner_ai
                        } else {
                            R.string.winner_o
                        }
                    else -> R.string.game_draw
                }
            )
            return
        }

        currentPlayerText.setText(
            if (game.gameMode.isAiMode) {
                if (game.activePlayer == viewModel.aiPlayer) R.string.turn_ai else R.string.turn_you
            } else {
                if (game.activePlayer == Player.X) R.string.turn_x else R.string.turn_o
            }
        )
    }

    private fun cancelPendingAi() {
        pendingAiRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingAiRunnable = null
    }

    private fun cancelPendingDialog() {
        pendingDialogRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingDialogRunnable = null
    }

    private fun dismissActiveDialog() {
        activeDialog?.takeIf { it.isShowing }?.dismiss()
        activeDialog = null
    }

    override fun onDestroy() {
        cancelPendingAi()
        cancelPendingDialog()
        dismissActiveDialog()
        winningLineView.clear()
        soundManager.release()
        super.onDestroy()
    }
}