package com.example.android.xo.engine

enum class CellState {
    EMPTY,
    X,
    O;

    fun toPlayer(): Player? = when (this) {
        X -> Player.X
        O -> Player.O
        EMPTY -> null
    }

    companion object {
        fun fromPlayer(player: Player?): CellState =
            if (player == null) EMPTY else if (player == Player.X) X else O
    }
}