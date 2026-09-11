package ir.sharif.xo.engine

import java.util.Random

object TicTacToeAi {

    private const val EASY_TACTICAL_AWARENESS = 0.20
    private const val MEDIUM_BLOCK_CHANCE = 0.80

    private val random = Random()

    fun getBestMove(game: TicTacToeGame, mode: GameMode, aiPlayer: Player): Int {
        val availableMoves = game.availableMoves
        if (availableMoves.isEmpty()) return -1

        return when (mode) {
            GameMode.VS_AI_EASY -> getEasyMove(game, availableMoves, aiPlayer)
            GameMode.VS_AI_MEDIUM -> getMediumMove(game, availableMoves, aiPlayer)
            GameMode.VS_AI_IMPOSSIBLE, GameMode.TWO_PLAYERS ->
                getMinimaxMove(game, availableMoves, aiPlayer)
        }
    }

    private fun getEasyMove(
        game: TicTacToeGame,
        availableMoves: List<Int>,
        aiPlayer: Player
    ): Int {
        if (random.nextDouble() < EASY_TACTICAL_AWARENESS) {
            val winningMove = findWinningMoves(game, aiPlayer, availableMoves)
                .let(::randomMoveOrNull)
            if (winningMove != null) return winningMove

            val blockingMove = findWinningMoves(game, aiPlayer.opponent(), availableMoves)
                .let(::randomMoveOrNull)
            if (blockingMove != null) return blockingMove
        }

        return randomMove(availableMoves)
    }

    private fun getMediumMove(
        game: TicTacToeGame,
        availableMoves: List<Int>,
        aiPlayer: Player
    ): Int {
        val winningMove = findWinningMoves(game, aiPlayer, availableMoves)
            .let(::randomMoveOrNull)
        if (winningMove != null) return winningMove

        val blockingMoves = findWinningMoves(game, aiPlayer.opponent(), availableMoves)
        if (blockingMoves.isNotEmpty() && random.nextDouble() < MEDIUM_BLOCK_CHANCE) {
            return randomMove(blockingMoves)
        }

        if (4 in availableMoves) return 4

        val corners = availableMoves.filter { it in CORNERS }
        if (corners.isNotEmpty()) return randomMove(corners)

        return randomMove(availableMoves)
    }

    fun getMinimaxMove(game: TicTacToeGame, availableMoves: List<Int>, aiPlayer: Player): Int {
        var bestScore = Int.MIN_VALUE
        val bestMoves = ArrayList<Int>(availableMoves.size)

        for (move in availableMoves) {
            val simulation = game.copy()
            simulation.makeMove(move)
            val score = minimax(simulation, 0, false, aiPlayer)

            when {
                score > bestScore -> {
                    bestScore = score
                    bestMoves.clear()
                    bestMoves += move
                }
                score == bestScore -> bestMoves += move
            }
        }

        return randomMove(bestMoves)
    }

    private fun findWinningMoves(
        game: TicTacToeGame,
        player: Player,
        availableMoves: List<Int>
    ): List<Int> = availableMoves.filter { move ->
        val simulation = game.copy()
        simulation.setActivePlayer(player)
        simulation.makeMove(move)
        simulation.gameResult.winner == player
    }

    private fun randomMove(moves: List<Int>): Int = moves[random.nextInt(moves.size)]

    private fun randomMoveOrNull(moves: List<Int>): Int? =
        if (moves.isEmpty()) null else randomMove(moves)

    private fun minimax(game: TicTacToeGame, depth: Int, isMaximizing: Boolean, aiPlayer: Player): Int {
        val result = game.gameResult
        if (result.isGameOver) {
            if (result.status == GameResult.Status.DRAW) return 0
            return if (result.winner == aiPlayer) 10 - depth else depth - 10
        }

        val availableMoves = game.availableMoves
        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in availableMoves) {
                val simulation = game.copy()
                simulation.makeMove(move)
                maxEval = maxOf(maxEval, minimax(simulation, depth + 1, false, aiPlayer))
            }
            return maxEval
        }

        var minEval = Int.MAX_VALUE
        for (move in availableMoves) {
            val simulation = game.copy()
            simulation.makeMove(move)
            minEval = minOf(minEval, minimax(simulation, depth + 1, true, aiPlayer))
        }
        return minEval
    }

    private val CORNERS = intArrayOf(0, 2, 6, 8)
}