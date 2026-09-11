package ir.sharif.xo.engine

class TicTacToeGame(initialGameMode: GameMode = GameMode.TWO_PLAYERS) {

    companion object {
        const val BOARD_SIZE = 9

        private val WINNING_LINES = arrayOf(
            intArrayOf(0, 1, 2), // Row 0
            intArrayOf(3, 4, 5), // Row 1
            intArrayOf(6, 7, 8), // Row 2
            intArrayOf(0, 3, 6), // Col 0
            intArrayOf(1, 4, 7), // Col 1
            intArrayOf(2, 5, 8), // Col 2
            intArrayOf(0, 4, 8), // Diagonal top-left to bottom-right
            intArrayOf(2, 4, 6)  // Diagonal top-right to bottom-left
        )
    }

    private val cells = Array(BOARD_SIZE) { CellState.EMPTY }

    var gameMode: GameMode = initialGameMode
        private set

    var activePlayer: Player = Player.X
        private set

    var gameResult: GameResult = GameResult.inProgress()
        private set

    var xScore: Int = 0
        private set

    var oScore: Int = 0
        private set

    var drawScore: Int = 0
        private set

    private fun resetBoard() {
        cells.fill(CellState.EMPTY)
        gameResult = GameResult.inProgress()
    }

    fun startNewRound(startingPlayer: Player) {
        resetBoard()
        activePlayer = startingPlayer
    }

    fun resetAll(startingPlayer: Player) {
        startNewRound(startingPlayer)
        xScore = 0
        oScore = 0
        drawScore = 0
    }

    fun makeMove(index: Int): Boolean {
        if (index !in 0 until BOARD_SIZE) return false
        if (gameResult.isGameOver) return false
        if (cells[index] != CellState.EMPTY) return false

        cells[index] = CellState.fromPlayer(activePlayer)
        gameResult = evaluateBoard()

        when (gameResult.status) {
            GameResult.Status.X_WON -> xScore++
            GameResult.Status.O_WON -> oScore++
            GameResult.Status.DRAW -> drawScore++
            GameResult.Status.IN_PROGRESS -> activePlayer = activePlayer.opponent()
        }

        return true
    }

    fun evaluateBoard(): GameResult {
        for (line in WINNING_LINES) {
            val c0 = cells[line[0]]
            val c1 = cells[line[1]]
            val c2 = cells[line[2]]

            if (c0 != CellState.EMPTY && c0 == c1 && c1 == c2) {
                return GameResult.win(c0.toPlayer()!!, line)
            }
        }

        if (cells.any { it == CellState.EMPTY }) {
            return GameResult.inProgress()
        }

        return GameResult.draw()
    }

    val availableMoves: List<Int>
        get() = (0 until BOARD_SIZE).filter { cells[it] == CellState.EMPTY }

    val board: Array<CellState>
        get() = cells.clone()

    fun cellAt(index: Int): CellState =
        if (index in 0 until BOARD_SIZE) cells[index] else CellState.EMPTY

    fun setActivePlayer(player: Player) {
        activePlayer = player
    }

    // Deep copy for simulation
    fun copy(): TicTacToeGame {
        val clone = TicTacToeGame(gameMode)
        System.arraycopy(cells, 0, clone.cells, 0, BOARD_SIZE)
        clone.activePlayer = activePlayer
        clone.gameResult = gameResult
        clone.xScore = xScore
        clone.oScore = oScore
        clone.drawScore = drawScore
        return clone
    }

}