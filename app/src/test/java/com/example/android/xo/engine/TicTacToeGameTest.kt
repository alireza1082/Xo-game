package com.example.android.xo.engine

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TicTacToeGameTest {

    private lateinit var game: TicTacToeGame

    @Before
    fun setUp() {
        game = TicTacToeGame()
    }

    @Test
    fun testInitialState() {
        for (i in 0 until 9) {
            assertEquals(CellState.EMPTY, game.cellAt(i))
        }
        assertEquals(Player.X, game.activePlayer)
        assertFalse(game.gameResult.isGameOver)
        assertEquals(GameResult.Status.IN_PROGRESS, game.gameResult.status)
        assertEquals(0, game.xScore)
        assertEquals(0, game.oScore)
        assertEquals(0, game.drawScore)
        assertEquals(9, game.availableMoves.size)
    }

    @Test
    fun testMakeMoveAndTurnSwitching() {
        assertTrue(game.makeMove(0))
        assertEquals(CellState.X, game.cellAt(0))
        assertEquals(Player.O, game.activePlayer)
        assertEquals(8, game.availableMoves.size)

        assertTrue(game.makeMove(1))
        assertEquals(CellState.O, game.cellAt(1))
        assertEquals(Player.X, game.activePlayer)
        assertEquals(7, game.availableMoves.size)
    }

    @Test
    fun testIllegalMoves() {
        // Out of bounds
        assertFalse(game.makeMove(-1))
        assertFalse(game.makeMove(9))

        // Occupied cell
        assertTrue(game.makeMove(4))
        assertEquals(CellState.X, game.cellAt(4))
        assertFalse(game.makeMove(4)) // Cannot overwrite
        assertEquals(Player.O, game.activePlayer) // Turn did not change
    }

    @Test
    fun testHorizontalWinRow0() {
        // X: 0, 1, 2
        // O: 3, 4
        assertTrue(game.makeMove(0)) // X
        assertTrue(game.makeMove(3)) // O
        assertTrue(game.makeMove(1)) // X
        assertTrue(game.makeMove(4)) // O
        assertTrue(game.makeMove(2)) // X wins

        assertTrue(game.gameResult.isGameOver)
        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertEquals(Player.X, game.gameResult.winner)
        assertArrayEquals(intArrayOf(0, 1, 2), game.gameResult.winningIndices!!)
        assertEquals(1, game.xScore)
        assertEquals(0, game.oScore)

        // Move after game over should fail
        assertFalse(game.makeMove(5))
    }

    @Test
    fun testHorizontalWinRow1() {
        // X: 3, 4, 5
        game.makeMove(3) // X
        game.makeMove(0) // O
        game.makeMove(4) // X
        game.makeMove(1) // O
        game.makeMove(5) // X wins

        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertArrayEquals(intArrayOf(3, 4, 5), game.gameResult.winningIndices!!)
    }

    @Test
    fun testHorizontalWinRow2() {
        // X: 6, 7, 8
        game.makeMove(6) // X
        game.makeMove(0) // O
        game.makeMove(7) // X
        game.makeMove(1) // O
        game.makeMove(8) // X wins

        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertArrayEquals(intArrayOf(6, 7, 8), game.gameResult.winningIndices!!)
    }

    @Test
    fun testVerticalWinCol0() {
        // X: 0, 3, 6
        game.makeMove(0) // X
        game.makeMove(1) // O
        game.makeMove(3) // X
        game.makeMove(2) // O
        game.makeMove(6) // X wins

        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertArrayEquals(intArrayOf(0, 3, 6), game.gameResult.winningIndices!!)
    }

    @Test
    fun testVerticalWinCol1() {
        // X: 1, 4, 7
        game.makeMove(1) // X
        game.makeMove(0) // O
        game.makeMove(4) // X
        game.makeMove(2) // O
        game.makeMove(7) // X wins

        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertArrayEquals(intArrayOf(1, 4, 7), game.gameResult.winningIndices!!)
    }

    @Test
    fun testVerticalWinCol2() {
        // X: 2, 5, 8
        game.makeMove(2) // X
        game.makeMove(0) // O
        game.makeMove(5) // X
        game.makeMove(1) // O
        game.makeMove(8) // X wins

        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertArrayEquals(intArrayOf(2, 5, 8), game.gameResult.winningIndices!!)
    }

    @Test
    fun testDiagonalWinMain() {
        // X: 0, 4, 8
        game.makeMove(0) // X
        game.makeMove(1) // O
        game.makeMove(4) // X
        game.makeMove(2) // O
        game.makeMove(8) // X wins

        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertArrayEquals(intArrayOf(0, 4, 8), game.gameResult.winningIndices!!)
    }

    @Test
    fun testDiagonalWinAnti() {
        // X: 2, 4, 6
        game.makeMove(2) // X
        game.makeMove(0) // O
        game.makeMove(4) // X
        game.makeMove(1) // O
        game.makeMove(6) // X wins

        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertArrayEquals(intArrayOf(2, 4, 6), game.gameResult.winningIndices!!)
    }

    @Test
    fun testOWins() {
        // O: 0, 1, 2
        game.makeMove(4) // X
        game.makeMove(0) // O
        game.makeMove(8) // X
        game.makeMove(1) // O
        game.makeMove(5) // X
        game.makeMove(2) // O wins

        assertTrue(game.gameResult.isGameOver)
        assertEquals(GameResult.Status.O_WON, game.gameResult.status)
        assertEquals(Player.O, game.gameResult.winner)
        assertEquals(0, game.xScore)
        assertEquals(1, game.oScore)
    }

    @Test
    fun testDrawGame() {
        // X O X
        // X X O
        // O X O
        game.makeMove(0) // X
        game.makeMove(1) // O
        game.makeMove(2) // X
        game.makeMove(4) // O
        game.makeMove(3) // X
        game.makeMove(5) // O
        game.makeMove(7) // X
        game.makeMove(6) // O
        game.makeMove(8) // X

        assertTrue(game.gameResult.isGameOver)
        assertEquals(GameResult.Status.DRAW, game.gameResult.status)
        assertNull(game.gameResult.winner)
        assertNull(game.gameResult.winningIndices)
        assertEquals(1, game.drawScore)
        assertEquals(0, game.xScore)
        assertEquals(0, game.oScore)
    }

    @Test
    fun testWinningOnMove9IsWinNotDraw() {
        // Board where X wins on the 9th move:
        // 0:X, 1:O, 2:X, 3:O, 4:X, 5:O, 6:O, 7:X, 8:X -> Diagonal 0,4,8 is X!
        game.makeMove(0) // X
        game.makeMove(1) // O
        game.makeMove(2) // X
        game.makeMove(3) // O
        game.makeMove(4) // X
        game.makeMove(5) // O
        game.makeMove(7) // X
        game.makeMove(6) // O
        game.makeMove(8) // X wins via diag 0,4,8 on the final move!

        assertTrue(game.gameResult.isGameOver)
        assertEquals(GameResult.Status.X_WON, game.gameResult.status)
        assertEquals(Player.X, game.gameResult.winner)
        assertEquals(1, game.xScore)
        assertEquals(0, game.drawScore)
    }

    @Test
    fun testRoundResetPreservesScore() {
        game.makeMove(0) // X
        game.makeMove(3) // O
        game.makeMove(1) // X
        game.makeMove(4) // O
        game.makeMove(2) // X wins

        assertEquals(1, game.xScore)

        game.startNewRound(Player.O)
        assertEquals(Player.O, game.activePlayer)
        assertFalse(game.gameResult.isGameOver)
        assertEquals(1, game.xScore)
        assertEquals(0, game.oScore)
        for (i in 0 until 9) {
            assertEquals(CellState.EMPTY, game.cellAt(i))
        }
    }

    @Test
    fun testResetAllClearsScores() {
        game.makeMove(0)
        game.makeMove(3)
        game.makeMove(1)
        game.makeMove(4)
        game.makeMove(2)
        assertEquals(1, game.xScore)

        game.resetAll(Player.X)
        assertEquals(0, game.xScore)
        assertEquals(0, game.oScore)
        assertEquals(0, game.drawScore)
        assertEquals(Player.X, game.activePlayer)
        assertFalse(game.gameResult.isGameOver)
    }
}