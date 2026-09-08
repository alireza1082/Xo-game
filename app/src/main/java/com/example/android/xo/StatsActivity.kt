package com.example.android.xo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.android.xo.data.GamePreferences
import com.example.android.xo.engine.GameMode
import com.example.android.xo.util.WindowInsetsUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class StatsActivity : AppCompatActivity() {

    private lateinit var preferences: GamePreferences

    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvBestStreak: TextView
    private lateinit var tvAiEasyStats: TextView
    private lateinit var tvAiMediumStats: TextView
    private lateinit var tvAiImpStats: TextView
    private lateinit var tvTwoPlayerStats: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        WindowInsetsUtil.applySystemBarInsets(findViewById(R.id.root))

        preferences = GamePreferences(this)

        initViews()
        setupToolbar()
        displayStats()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun initViews() {
        tvCurrentStreak = findViewById(R.id.tv_current_streak)
        tvBestStreak = findViewById(R.id.tv_best_streak)
        tvAiEasyStats = findViewById(R.id.tv_ai_easy_stats)
        tvAiMediumStats = findViewById(R.id.tv_ai_medium_stats)
        tvAiImpStats = findViewById(R.id.tv_ai_imp_stats)
        tvTwoPlayerStats = findViewById(R.id.tv_two_player_stats)

        findViewById<MaterialButton>(R.id.btn_reset_stats).setOnClickListener {
            confirmResetStats()
        }
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayStats() {
        tvCurrentStreak.text = preferences.currentStreak.toString()
        tvBestStreak.text = preferences.bestStreak.toString()

        val easy = preferences.getStats(GameMode.VS_AI_EASY)
        tvAiEasyStats.text = formatModeStats(easy)

        val med = preferences.getStats(GameMode.VS_AI_MEDIUM)
        tvAiMediumStats.text = formatModeStats(med)

        val imp = preferences.getStats(GameMode.VS_AI_IMPOSSIBLE)
        tvAiImpStats.text = formatModeStats(imp)

        val tp = preferences.getTwoPlayerStats()
        tvTwoPlayerStats.text = "${getString(R.string.stats_wins, tp.xWins)} (X)  |  " +
                "${getString(R.string.stats_wins, tp.oWins)} (O)  |  " +
                getString(R.string.stats_draws, tp.draws)
    }

    private fun formatModeStats(stats: GamePreferences.ModeStats): String {
        return "${getString(R.string.stats_wins, stats.wins)}  |  " +
                "${getString(R.string.stats_losses, stats.losses)}  |  " +
                "${getString(R.string.stats_draws, stats.draws)}  " +
                "(${getString(R.string.stats_win_rate, stats.winRatePercent)})"
    }

    private fun confirmResetStats() {
        AlertDialog.Builder(this)
            .setTitle(R.string.stats_reset_confirm_title)
            .setMessage(R.string.stats_reset_confirm_msg)
            .setPositiveButton(R.string.stats_reset_button) { _, _ ->
                preferences.resetAllStats()
                displayStats()
                Toast.makeText(this, R.string.stats_reset_success, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
