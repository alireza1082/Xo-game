package ir.sharif.xo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import ir.sharif.xo.engine.GameMode
import ir.sharif.xo.engine.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val PREFS_NAME = "xo_game_preferences"
private const val DATASTORE_NAME = "xo_settings"

private val Context.xoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATASTORE_NAME,
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, PREFS_NAME)) }
)

/** Persistent settings and match statistics for the game. */
class GamePreferences(context: Context) {
    private val dataStore = context.applicationContext.xoDataStore

    val settings: Flow<Settings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences ->
            Settings(
                isSoundEnabled = preferences[Keys.soundEnabled] ?: true,
                isHapticEnabled = preferences[Keys.hapticEnabled] ?: true,
                preferredSymbol = if (preferences[Keys.preferredSymbol] == Player.O.name) Player.O else Player.X
            )
        }

    val statistics: Flow<Statistics> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences ->
            Statistics(
                currentStreak = preferences[Keys.currentStreak] ?: 0,
                bestStreak = preferences[Keys.bestStreak] ?: 0,
                easy = modeStats(preferences, Keys.aiEasyWins, Keys.aiEasyLosses, Keys.aiEasyDraws),
                medium = modeStats(preferences, Keys.aiMediumWins, Keys.aiMediumLosses, Keys.aiMediumDraws),
                impossible = modeStats(preferences, Keys.aiImpossibleWins, Keys.aiImpossibleLosses, Keys.aiImpossibleDraws),
                twoPlayer = TwoPlayerStats(
                    xWins = preferences[Keys.twoPlayerXWins] ?: 0,
                    oWins = preferences[Keys.twoPlayerOWins] ?: 0,
                    draws = preferences[Keys.twoPlayerDraws] ?: 0
                )
            )
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.soundEnabled] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.hapticEnabled] = enabled }
    }

    suspend fun setPreferredSymbol(symbol: Player) {
        dataStore.edit { it[Keys.preferredSymbol] = symbol.name }
    }

    suspend fun recordGameResult(mode: GameMode, humanPlayer: Player, winner: Player?) {
        dataStore.edit { preferences ->
            if (mode.isAiMode) {
                val keys = when (mode) {
                    GameMode.VS_AI_EASY -> Triple(Keys.aiEasyWins, Keys.aiEasyLosses, Keys.aiEasyDraws)
                    GameMode.VS_AI_MEDIUM -> Triple(Keys.aiMediumWins, Keys.aiMediumLosses, Keys.aiMediumDraws)
                    GameMode.VS_AI_IMPOSSIBLE -> Triple(Keys.aiImpossibleWins, Keys.aiImpossibleLosses, Keys.aiImpossibleDraws)
                    GameMode.TWO_PLAYERS -> return@edit
                }
                when {
                    winner == humanPlayer -> {
                        preferences[keys.first] = (preferences[keys.first] ?: 0) + 1
                        val streak = (preferences[Keys.currentStreak] ?: 0) + 1
                        preferences[Keys.currentStreak] = streak
                        if (streak > (preferences[Keys.bestStreak] ?: 0)) preferences[Keys.bestStreak] = streak
                    }
                    winner == humanPlayer.opponent() -> {
                        preferences[keys.second] = (preferences[keys.second] ?: 0) + 1
                        preferences[Keys.currentStreak] = 0
                    }
                    else -> preferences[keys.third] = (preferences[keys.third] ?: 0) + 1
                }
            } else {
                when (winner) {
                    Player.X -> preferences[Keys.twoPlayerXWins] = (preferences[Keys.twoPlayerXWins] ?: 0) + 1
                    Player.O -> preferences[Keys.twoPlayerOWins] = (preferences[Keys.twoPlayerOWins] ?: 0) + 1
                    null -> preferences[Keys.twoPlayerDraws] = (preferences[Keys.twoPlayerDraws] ?: 0) + 1
                }
            }
        }
    }

    suspend fun resetStatistics() {
        dataStore.edit { preferences ->
            listOf(
                Keys.currentStreak, Keys.bestStreak,
                Keys.aiEasyWins, Keys.aiEasyLosses, Keys.aiEasyDraws,
                Keys.aiMediumWins, Keys.aiMediumLosses, Keys.aiMediumDraws,
                Keys.aiImpossibleWins, Keys.aiImpossibleLosses, Keys.aiImpossibleDraws,
                Keys.twoPlayerXWins, Keys.twoPlayerOWins, Keys.twoPlayerDraws
            ).forEach(preferences::remove)
        }
    }

    private fun modeStats(
        preferences: Preferences,
        winsKey: Preferences.Key<Int>,
        lossesKey: Preferences.Key<Int>,
        drawsKey: Preferences.Key<Int>
    ) = ModeStats(
        wins = preferences[winsKey] ?: 0,
        losses = preferences[lossesKey] ?: 0,
        draws = preferences[drawsKey] ?: 0
    )

    data class Settings(
        val isSoundEnabled: Boolean = true,
        val isHapticEnabled: Boolean = true,
        val preferredSymbol: Player = Player.X
    )

    data class Statistics(
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val easy: ModeStats = ModeStats(),
        val medium: ModeStats = ModeStats(),
        val impossible: ModeStats = ModeStats(),
        val twoPlayer: TwoPlayerStats = TwoPlayerStats()
    )

    data class ModeStats(val wins: Int = 0, val losses: Int = 0, val draws: Int = 0) {
        val totalGames: Int get() = wins + losses + draws
        val winRatePercent: Int get() = if (totalGames == 0) 0 else wins * 100 / totalGames
    }

    data class TwoPlayerStats(val xWins: Int = 0, val oWins: Int = 0, val draws: Int = 0)

    private object Keys {
        val soundEnabled = booleanPreferencesKey("key_sound_enabled")
        val hapticEnabled = booleanPreferencesKey("key_haptic_enabled")
        val preferredSymbol = stringPreferencesKey("key_preferred_symbol")
        val currentStreak = intPreferencesKey("key_current_streak")
        val bestStreak = intPreferencesKey("key_best_streak")
        val aiEasyWins = intPreferencesKey("key_ai_easy_wins")
        val aiEasyLosses = intPreferencesKey("key_ai_easy_losses")
        val aiEasyDraws = intPreferencesKey("key_ai_easy_draws")
        val aiMediumWins = intPreferencesKey("key_ai_med_wins")
        val aiMediumLosses = intPreferencesKey("key_ai_med_losses")
        val aiMediumDraws = intPreferencesKey("key_ai_med_draws")
        val aiImpossibleWins = intPreferencesKey("key_ai_imp_wins")
        val aiImpossibleLosses = intPreferencesKey("key_ai_imp_losses")
        val aiImpossibleDraws = intPreferencesKey("key_ai_imp_draws")
        val twoPlayerXWins = intPreferencesKey("key_2p_x_wins")
        val twoPlayerOWins = intPreferencesKey("key_2p_o_wins")
        val twoPlayerDraws = intPreferencesKey("key_2p_draws")
    }
}
