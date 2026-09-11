package ir.sharif.xo.engine

enum class GameMode {
    TWO_PLAYERS,
    VS_AI_EASY,
    VS_AI_MEDIUM,
    VS_AI_IMPOSSIBLE;

    val isAiMode: Boolean
        get() = this != TWO_PLAYERS
}