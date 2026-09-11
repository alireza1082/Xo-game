package ir.sharif.xo

import ir.sharif.xo.engine.CellState
import ir.sharif.xo.engine.GameMode
import ir.sharif.xo.engine.Player
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

    @Test
    fun defaultsToHumanPlayerXAndAiPlayerO() {
        val viewModel = GameBoardViewModel(GameMode.VS_AI_EASY)

        assertEquals(Player.X, viewModel.humanPlayer)
        assertEquals(Player.O, viewModel.aiPlayer)
    }

    @Test
    fun supportsHumanPlayerOWithAiPlayerX() {
        val viewModel = GameBoardViewModel(GameMode.VS_AI_MEDIUM, Player.O)

        assertEquals(Player.O, viewModel.humanPlayer)
        assertEquals(Player.X, viewModel.aiPlayer)
        assertEquals(Player.X, viewModel.game.activePlayer)
    }
}