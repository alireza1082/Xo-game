package com.example.android.xo.engine

import java.util.Random

object TicTacToeAi {

    private val random = Random()

    fun getBestMove(game: TicTacToeGame, mode: GameMode, aiPlayer: Player): Int {
        val availableMoves = game.availableMoves
        if (availableMoves.isEmpty()) {
            return -1
        }

        return when (mode) {
            GameMode.VS_AI_EASY -> availableMoves[random.nextInt(availableMoves.size)]
            GameMode.VS_AI_MEDIUM -> getMediumMove(game, availableMoves, aiPlayer)
            // Same default as the original implementation: anything else uses Minimax.
            GameMode.VS_AI_IMPOSSIBLE, GameMode.TWO_PLAYERS -> getMinimaxMove(game, availableMoves, aiPlayer)
        }
    }

    private fun getMediumMove(game: TicTacToeGame, availableMoves: List<Int>, aiPlayer: Player): Int {
        val opponent = aiPlayer.opponent()

        // 1. Can AI win immediately?
        for (move in availableMoves) {
            val sim = game.copy()
            sim.makeMove(move)
            if (sim.gameResult.winner == aiPlayer) {
                return move
            }
        }

        // 2. Can opponent win immediately? Block it!
        for (move in availableMoves) {
            val sim = game.copy()
            sim.setActivePlayer(opponent)
            sim.makeMove(move)
            if (sim.gameResult.winner == opponent) {
                return move
            }
        }

        // 3. Take center if available
        if (4 in availableMoves && random.nextFloat() < 0.7f) {
            return 4
        }

        // 4. Random move
        return availableMoves[random.nextInt(availableMoves.size)]
    }

    fun getMinimaxMove(game: TicTacToeGame, availableMoves: List<Int>, aiPlayer: Player): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove = availableMoves.first()

        for (move in availableMoves) {
            val sim = game.copy()
            sim.makeMove(move)
            val score = minimax(sim, 0, false, aiPlayer)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
    }

    private fun minimax(game: TicTacToeGame, depth: Int, isMaximizing: Boolean, aiPlayer: Player): Int {
        val result = game.gameResult
        if (result.isGameOver) {
            if (result.status == GameResult.Status.DRAW) {
                return 0
            }
            return if (result.winner == aiPlayer) 10 - depth else depth - 10
        }

        val availableMoves = game.availableMoves
        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in availableMoves) {
                val sim = game.copy()
                sim.makeMove(move)
                maxEval = maxOf(maxEval, minimax(sim, depth + 1, false, aiPlayer))
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in availableMoves) {
                val sim = game.copy()
                sim.makeMove(move)
                minEval = minOf(minEval, minimax(sim, depth + 1, true, aiPlayer))
            }
            return minEval
        }
    }
}