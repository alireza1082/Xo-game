package com.example.android.xo.engine

enum class Player {
    X,
    O;

    fun opponent(): Player = if (this == X) O else X
}