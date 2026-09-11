package ir.sharif.xo.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class TicTacToeAiTest {
    @Test
    fun testAiTakesWinningMove() {
        val game = TicTacToeGame(GameMode.VS_AI_MEDIUM)
        game.makeMove(3)
        game.makeMove(0)
        game.makeMove(4)
        game.makeMove(1)
        game.makeMove(6)

        val move = TicTacToeAi.getBestMove(game, GameMode.VS_AI_MEDIUM, Player.O)
        assertEquals(2, move)
    }

    @Test
    fun testAiBlocksOpponentWin() {
        val game = TicTacToeGame(GameMode.VS_AI_MEDIUM)
        game.makeMove(0)
        game.makeMove(4)
        game.makeMove(1)

        val move = TicTacToeAi.getBestMove(game, GameMode.VS_AI_MEDIUM, Player.O)
        assertEquals(2, move)
    }

    @Test
    fun mediumPrefersCreatingAForkWhenNoImmediateTacticsExist() {
        val game = TicTacToeGame(GameMode.VS_AI_MEDIUM)
        game.makeMove(1)
        game.makeMove(0)
        game.makeMove(8)
        game.makeMove(4)
        game.setActivePlayer(Player.O)

        val move = TicTacToeAi.getBestMove(game, GameMode.VS_AI_MEDIUM, Player.O, Random(7))
        assertEquals(6, move)
    }

    @Test
    fun mediumPrefersCenterBeforeCornersAndEdges() {
        val game = TicTacToeGame(GameMode.VS_AI_MEDIUM)

        val move = TicTacToeAi.getBestMove(game, GameMode.VS_AI_MEDIUM, Player.O, Random(7))
        assertEquals(4, move)
    }

    @Test
    fun testMinimaxAiNeverLoses() {
        val random = Random(42)
        for (i in 0 until 50) {
            val game = TicTacToeGame(GameMode.VS_AI_IMPOSSIBLE)
            val aiPlayer = if (i % 2 == 0) Player.O else Player.X
            val humanPlayer = aiPlayer.opponent()

            while (!game.gameResult.isGameOver) {
                if (game.activePlayer == aiPlayer) {
                    val move = TicTacToeAi.getBestMove(game, GameMode.VS_AI_IMPOSSIBLE, aiPlayer)
                    assertTrue("AI produced invalid move", game.makeMove(move))
                } else {
                    val moves = game.availableMoves
                    val move = moves[random.nextInt(moves.size)]
                    assertTrue("Human move failed", game.makeMove(move))
                }
            }

            assertNotEquals("Minimax AI lost to random opponent in game $i", humanPlayer, game.gameResult.winner)
        }
    }
}
