package com.example.android.xo.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class TicTacToeAiTest {

    @Test
    fun testAiTakesWinningMove() {
        val game = TicTacToeGame(GameMode.VS_AI_MEDIUM)
        // Set up board where AI (O) can win on cell 2:
        // O: 0, 1, ?
        // X: 3, 4
        game.makeMove(3) // X
        game.makeMove(0) // O
        game.makeMove(4) // X
        game.makeMove(1) // O
        game.makeMove(6) // X

        // Now AI (O) should pick cell 2 to win
        val move = TicTacToeAi.getBestMove(game, GameMode.VS_AI_MEDIUM, Player.O)
        assertEquals(2, move)
    }

    @Test
    fun testAiBlocksOpponentWin() {
        val game = TicTacToeGame(GameMode.VS_AI_MEDIUM)
        // X is threatening to win at cell 2:
        // X: 0, 1
        // O: 4
        game.makeMove(0) // X
        game.makeMove(4) // O
        game.makeMove(1) // X

        // AI (O)'s turn: must block at 2
        val move = TicTacToeAi.getBestMove(game, GameMode.VS_AI_MEDIUM, Player.O)
        assertEquals(2, move)
    }

    @Test
    fun testMinimaxAiNeverLoses() {
        // Run 50 simulated games of random/heuristic player vs Minimax AI
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
                    // Human makes a random move
                    val moves = game.availableMoves
                    val move = moves[random.nextInt(moves.size)]
                    assertTrue("Human move failed", game.makeMove(move))
                }
            }

            // Minimax must NEVER lose
            assertNotEquals(
                "Minimax AI lost to random opponent in game $i",
                humanPlayer,
                game.gameResult.winner
            )
        }
    }
}