package com.example.android.xo.data

import android.content.Context
import android.content.SharedPreferences
import com.example.android.xo.engine.GameMode
import com.example.android.xo.engine.Player

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "xo_game_preferences"

        const val KEY_SOUND_ENABLED = "key_sound_enabled"
        const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"
        const val KEY_PREFERRED_SYMBOL = "key_preferred_symbol"

        // Streaks
        const val KEY_CURRENT_STREAK = "key_current_streak"
        const val KEY_BEST_STREAK = "key_best_streak"

        // AI Easy
        const val KEY_AI_EASY_WINS = "key_ai_easy_wins"
        const val KEY_AI_EASY_LOSSES = "key_ai_easy_losses"
        const val KEY_AI_EASY_DRAWS = "key_ai_easy_draws"

        // AI Medium
        const val KEY_AI_MED_WINS = "key_ai_med_wins"
        const val KEY_AI_MED_LOSSES = "key_ai_med_losses"
        const val KEY_AI_MED_DRAWS = "key_ai_med_draws"

        // AI Impossible
        const val KEY_AI_IMP_WINS = "key_ai_imp_wins"
        const val KEY_AI_IMP_LOSSES = "key_ai_imp_losses"
        const val KEY_AI_IMP_DRAWS = "key_ai_imp_draws"

        // Two Players
        const val KEY_2P_X_WINS = "key_2p_x_wins"
        const val KEY_2P_O_WINS = "key_2p_o_wins"
        const val KEY_2P_DRAWS = "key_2p_draws"
    }

    data class ModeStats(val wins: Int, val losses: Int, val draws: Int) {
        val totalGames: Int get() = wins + losses + draws
        val winRatePercent: Int
            get() = if (totalGames > 0) ((wins.toDouble() / totalGames) * 100).toInt() else 0
    }

    data class TwoPlayerStats(val xWins: Int, val oWins: Int, val draws: Int) {
        val totalGames: Int get() = xWins + oWins + draws
    }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var preferredSymbol: Player
        get() = if (prefs.getString(KEY_PREFERRED_SYMBOL, "X") == "O") Player.O else Player.X
        set(value) = prefs.edit().putString(KEY_PREFERRED_SYMBOL, value.name).apply()

    val currentStreak: Int
        get() = prefs.getInt(KEY_CURRENT_STREAK, 0)

    val bestStreak: Int
        get() = prefs.getInt(KEY_BEST_STREAK, 0)

    fun recordGameResult(mode: GameMode, humanPlayer: Player, winner: Player?) {
        val editor = prefs.edit()

        if (mode.isAiMode) {
            val (winKey, lossKey, drawKey) = when (mode) {
                GameMode.VS_AI_EASY -> Triple(KEY_AI_EASY_WINS, KEY_AI_EASY_LOSSES, KEY_AI_EASY_DRAWS)
                GameMode.VS_AI_MEDIUM -> Triple(KEY_AI_MED_WINS, KEY_AI_MED_LOSSES, KEY_AI_MED_DRAWS)
                GameMode.VS_AI_IMPOSSIBLE -> Triple(KEY_AI_IMP_WINS, KEY_AI_IMP_LOSSES, KEY_AI_IMP_DRAWS)
                GameMode.TWO_PLAYERS -> return
            }

            when (winner) {
                humanPlayer -> {
                    editor.putInt(winKey, prefs.getInt(winKey, 0) + 1)
                    val newStreak = currentStreak + 1
                    editor.putInt(KEY_CURRENT_STREAK, newStreak)
                    if (newStreak > bestStreak) {
                        editor.putInt(KEY_BEST_STREAK, newStreak)
                    }
                }
                humanPlayer.opponent() -> {
                    editor.putInt(lossKey, prefs.getInt(lossKey, 0) + 1)
                    editor.putInt(KEY_CURRENT_STREAK, 0)
                }
                else -> {
                    editor.putInt(drawKey, prefs.getInt(drawKey, 0) + 1)
                }
            }
        } else {
            // Two player mode
            when (winner) {
                Player.X -> editor.putInt(KEY_2P_X_WINS, prefs.getInt(KEY_2P_X_WINS, 0) + 1)
                Player.O -> editor.putInt(KEY_2P_O_WINS, prefs.getInt(KEY_2P_O_WINS, 0) + 1)
                null -> editor.putInt(KEY_2P_DRAWS, prefs.getInt(KEY_2P_DRAWS, 0) + 1)
            }
        }

        editor.apply()
    }

    fun getStats(mode: GameMode): ModeStats = when (mode) {
        GameMode.VS_AI_EASY -> ModeStats(
            prefs.getInt(KEY_AI_EASY_WINS, 0),
            prefs.getInt(KEY_AI_EASY_LOSSES, 0),
            prefs.getInt(KEY_AI_EASY_DRAWS, 0)
        )
        GameMode.VS_AI_MEDIUM -> ModeStats(
            prefs.getInt(KEY_AI_MED_WINS, 0),
            prefs.getInt(KEY_AI_MED_LOSSES, 0),
            prefs.getInt(KEY_AI_MED_DRAWS, 0)
        )
        GameMode.VS_AI_IMPOSSIBLE -> ModeStats(
            prefs.getInt(KEY_AI_IMP_WINS, 0),
            prefs.getInt(KEY_AI_IMP_LOSSES, 0),
            prefs.getInt(KEY_AI_IMP_DRAWS, 0)
        )
        GameMode.TWO_PLAYERS -> ModeStats(0, 0, 0)
    }

    fun getTwoPlayerStats(): TwoPlayerStats = TwoPlayerStats(
        prefs.getInt(KEY_2P_X_WINS, 0),
        prefs.getInt(KEY_2P_O_WINS, 0),
        prefs.getInt(KEY_2P_DRAWS, 0)
    )

    fun resetAllStats() {
        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, 0)
            .putInt(KEY_BEST_STREAK, 0)
            .putInt(KEY_AI_EASY_WINS, 0)
            .putInt(KEY_AI_EASY_LOSSES, 0)
            .putInt(KEY_AI_EASY_DRAWS, 0)
            .putInt(KEY_AI_MED_WINS, 0)
            .putInt(KEY_AI_MED_LOSSES, 0)
            .putInt(KEY_AI_MED_DRAWS, 0)
            .putInt(KEY_AI_IMP_WINS, 0)
            .putInt(KEY_AI_IMP_LOSSES, 0)
            .putInt(KEY_AI_IMP_DRAWS, 0)
            .putInt(KEY_2P_X_WINS, 0)
            .putInt(KEY_2P_O_WINS, 0)
            .putInt(KEY_2P_DRAWS, 0)
            .apply()
    }
}
