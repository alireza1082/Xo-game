package ir.sharif.xo.engine

import java.util.Arrays

class GameResult private constructor(
    val status: Status,
    indices: IntArray?,
) {
    enum class Status {
        IN_PROGRESS,
        X_WON,
        O_WON,
        DRAW
    }

    private val storedWinningIndices: IntArray? = indices?.clone()

    val winningIndices: IntArray?
        get() = storedWinningIndices?.clone()

    val isGameOver: Boolean
        get() = status != Status.IN_PROGRESS

    val winner: Player?
        get() = when (status) {
            Status.X_WON -> Player.X
            Status.O_WON -> Player.O
            else -> null
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameResult) return false
        return status == other.status && Arrays.equals(storedWinningIndices, other.storedWinningIndices)
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + Arrays.hashCode(storedWinningIndices)
        return result
    }

    companion object {
        fun inProgress() = GameResult(Status.IN_PROGRESS, null)

        fun draw() = GameResult(Status.DRAW, null)

        fun win(winner: Player, winningIndices: IntArray) =
            GameResult(if (winner == Player.X) Status.X_WON else Status.O_WON, winningIndices)
    }
}