package ir.sharif.xo.engine

import java.util.Random

object TicTacToeAi {
    private const val EASY_TACTICAL_AWARENESS = 0.20
    private const val MEDIUM_BLOCK_CHANCE = 0.80
    private val defaultRandom = Random()
    private val corners = intArrayOf(0, 2, 6, 8)
    private val edges = intArrayOf(1, 3, 5, 7)

    fun getBestMove(
        game: TicTacToeGame,
        mode: GameMode,
        aiPlayer: Player,
        random: Random = defaultRandom
    ): Int {
        val availableMoves = game.availableMoves
        if (availableMoves.isEmpty()) return -1

        return when (mode) {
            GameMode.VS_AI_EASY -> getEasyMove(game, availableMoves, aiPlayer, random)
            GameMode.VS_AI_MEDIUM -> getMediumMove(game, availableMoves, aiPlayer, random)
            GameMode.VS_AI_IMPOSSIBLE, GameMode.TWO_PLAYERS ->
                getMinimaxMove(game, availableMoves, aiPlayer, random)
        }
    }

    private fun getEasyMove(
        game: TicTacToeGame,
        availableMoves: List<Int>,
        aiPlayer: Player,
        random: Random
    ): Int {
        if (random.nextDouble() < EASY_TACTICAL_AWARENESS) {
            randomMoveOrNull(findWinningMoves(game, aiPlayer, availableMoves), random)
                ?.let { return it }
            randomMoveOrNull(findWinningMoves(game, aiPlayer.opponent(), availableMoves), random)
                ?.let { return it }
        }
        return randomMove(availableMoves, random)
    }

    private fun getMediumMove(
        game: TicTacToeGame,
        availableMoves: List<Int>,
        aiPlayer: Player,
        random: Random
    ): Int {
        randomMoveOrNull(findWinningMoves(game, aiPlayer, availableMoves), random)
            ?.let { return it }

        val blockingMoves = findWinningMoves(game, aiPlayer.opponent(), availableMoves)
        if (blockingMoves.isNotEmpty() && random.nextDouble() < MEDIUM_BLOCK_CHANCE) {
            return randomMove(blockingMoves, random)
        }

        randomMoveOrNull(findForkMoves(game, aiPlayer, availableMoves), random)
            ?.let { return it }
        randomMoveOrNull(findForkMoves(game, aiPlayer.opponent(), availableMoves), random)
            ?.let { return it }

        if (4 in availableMoves) return 4
        randomMoveOrNull(availableMoves.filter { it in corners }, random)
            ?.let { return it }

        val availableEdges = availableMoves.filter { it in edges }
        return if (availableEdges.isNotEmpty()) {
            randomMove(availableEdges, random)
        } else {
            randomMove(availableMoves, random)
        }
    }

    fun getMinimaxMove(
        game: TicTacToeGame,
        availableMoves: List<Int>,
        aiPlayer: Player,
        random: Random = defaultRandom
    ): Int {
        var bestScore = Int.MIN_VALUE
        val bestMoves = ArrayList<Int>(availableMoves.size)
        val cache = HashMap<Long, Int>()

        for (move in availableMoves) {
            val simulation = game.copy()
            simulation.makeMove(move)
            val score = minimax(
                simulation,
                depth = 0,
                isMaximizing = false,
                aiPlayer = aiPlayer,
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE,
                cache = cache
            )
            when {
                score > bestScore -> {
                    bestScore = score
                    bestMoves.clear()
                    bestMoves += move
                }
                score == bestScore -> bestMoves += move
            }
        }

        return randomMove(bestMoves, random)
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

    private fun findForkMoves(
        game: TicTacToeGame,
        player: Player,
        availableMoves: List<Int>
    ): List<Int> = availableMoves.filter { move ->
        val simulation = game.copy()
        simulation.setActivePlayer(player)
        if (!simulation.makeMove(move) || simulation.gameResult.isGameOver) return@filter false
        findWinningMoves(simulation, player, simulation.availableMoves).size >= 2
    }

    private fun minimax(
        game: TicTacToeGame,
        depth: Int,
        isMaximizing: Boolean,
        aiPlayer: Player,
        alpha: Int,
        beta: Int,
        cache: MutableMap<Long, Int>
    ): Int {
        val key = stateKey(game, depth, isMaximizing)
        cache[key]?.let { return it }

        val result = game.gameResult
        if (result.isGameOver) {
            val score = when {
                result.status == GameResult.Status.DRAW -> 0
                result.winner == aiPlayer -> 10 - depth
                else -> depth - 10
            }
            cache[key] = score
            return score
        }

        var currentAlpha = alpha
        var currentBeta = beta
        var exact = true
        val score = if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (move in game.availableMoves) {
                val simulation = game.copy()
                simulation.makeMove(move)
                best = maxOf(
                    best,
                    minimax(simulation, depth + 1, false, aiPlayer, currentAlpha, currentBeta, cache)
                )
                currentAlpha = maxOf(currentAlpha, best)
                if (currentAlpha >= currentBeta) {
                    exact = false
                    break
                }
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (move in game.availableMoves) {
                val simulation = game.copy()
                simulation.makeMove(move)
                best = minOf(
                    best,
                    minimax(simulation, depth + 1, true, aiPlayer, currentAlpha, currentBeta, cache)
                )
                currentBeta = minOf(currentBeta, best)
                if (currentAlpha >= currentBeta) {
                    exact = false
                    break
                }
            }
            best
        }

        if (exact) cache[key] = score
        return score
    }

    private fun stateKey(game: TicTacToeGame, depth: Int, isMaximizing: Boolean): Long {
        var key = 0L
        for (cell in game.board) key = key * 3 + cell.ordinal
        key = key * 2 + game.activePlayer.ordinal
        key = key * 2 + if (isMaximizing) 1 else 0
        return key * 10 + depth
    }

    private fun randomMove(moves: List<Int>, random: Random): Int = moves[random.nextInt(moves.size)]

    private fun randomMoveOrNull(moves: List<Int>, random: Random): Int? =
        if (moves.isEmpty()) null else randomMove(moves, random)
}
