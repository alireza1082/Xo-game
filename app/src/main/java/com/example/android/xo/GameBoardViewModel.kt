package com.example.android.xo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android.xo.engine.GameMode
import com.example.android.xo.engine.Player
import com.example.android.xo.engine.TicTacToeGame

/**
 * Owns the [TicTacToeGame] state so it survives configuration changes
 * (rotation, resizing) without needing to serialize it into a Bundle.
 *
 * Note: a ViewModel does not survive process death. The activity starts a
 * fresh round after the process is recreated, which is acceptable for this
 * casual offline game.
 */
class GameBoardViewModel(initialGameMode: GameMode) : ViewModel() {

    val game: TicTacToeGame = TicTacToeGame(initialGameMode).apply {
        startNewRound(Player.X)
    }

    companion object {
        fun factory(initialGameMode: GameMode): ViewModelProvider.Factory = viewModelFactory {
            initializer { GameBoardViewModel(initialGameMode) }
        }
    }
}