package com.example.android.xo

import com.example.android.xo.engine.CellState
import com.example.android.xo.engine.GameMode
import com.example.android.xo.engine.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GameBoardViewModelTest {

    @Test
    fun startsFreshRoundWithXToMove() {
        val viewModel = GameBoardViewModel(GameMode.TWO_PLAYERS)

        assertEquals(GameMode.TWO_PLAYERS, viewModel.game.gameMode)
        assertEquals(Player.X, viewModel.game.activePlayer)
        assertFalse(viewModel.game.gameResult.isGameOver)
        for (i in 0 until 9) {
            assertEquals(CellState.EMPTY, viewModel.game.cellAt(i))
        }
    }

    @Test
    fun retainsConfiguredAiMode() {
        val viewModel = GameBoardViewModel(GameMode.VS_AI_IMPOSSIBLE)

        assertEquals(GameMode.VS_AI_IMPOSSIBLE, viewModel.game.gameMode)
        assertFalse(viewModel.game.gameResult.isGameOver)
    }
}