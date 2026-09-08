package com.example.android.xo

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.android.xo.data.GamePreferences
import com.example.android.xo.engine.GameMode
import com.example.android.xo.engine.Player
import com.example.android.xo.util.WindowInsetsUtil
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GAME_MODE = "extra_game_mode"
        const val EXTRA_HUMAN_PLAYER = "extra_human_player"
        private const val BACK_PRESS_INTERVAL_MS = 2000L
    }

    private var backPressedTime = 0L
    private lateinit var preferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowInsetsUtil.applySystemBarInsets(findViewById(R.id.root))

        preferences = GamePreferences(this)

        val btnVsAi: MaterialButton = findViewById(R.id.btn_vs_ai)
        val btnTwoPlayer: MaterialButton = findViewById(R.id.btn_two_player)
        val btnStats: MaterialButton = findViewById(R.id.btn_stats)
        val btnAbout: MaterialButton = findViewById(R.id.btn_about)
        val btnShare: MaterialButton = findViewById(R.id.btn_share)

        btnVsAi.setOnClickListener { showAiSetupDialog() }

        btnTwoPlayer.setOnClickListener {
            startActivity(
                Intent(this, GameBoard::class.java)
                    .putExtra(EXTRA_GAME_MODE, GameMode.TWO_PLAYERS.name)
            )
        }

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutUs::class.java))
        }

        btnShare.setOnClickListener {
            val shareUrl = "https://myket.ir/app/" + BuildConfig.APPLICATION_ID
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, shareUrl))
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val now = System.currentTimeMillis()
                if (backPressedTime + BACK_PRESS_INTERVAL_MS > now) {
                    finish()
                } else {
                    backPressedTime = now
                    Toast.makeText(this@MainActivity, R.string.press_back_again, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showAiSetupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ai_setup, null)
        val rgDifficulty: RadioGroup = dialogView.findViewById(R.id.rg_difficulty)
        val rbEasy: RadioButton = dialogView.findViewById(R.id.rb_easy)
        val rbMedium: RadioButton = dialogView.findViewById(R.id.rb_medium)
        val rbSymbolX: RadioButton = dialogView.findViewById(R.id.rb_symbol_x)
        val rbSymbolO: RadioButton = dialogView.findViewById(R.id.rb_symbol_o)

        if (preferences.preferredSymbol == Player.O) {
            rbSymbolO.isChecked = true
        } else {
            rbSymbolX.isChecked = true
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_ai_title)
            .setView(dialogView)
            .setPositiveButton(R.string.start_game) { _, _ ->
                val selectedMode = when {
                    rbEasy.isChecked -> GameMode.VS_AI_EASY
                    rbMedium.isChecked -> GameMode.VS_AI_MEDIUM
                    else -> GameMode.VS_AI_IMPOSSIBLE
                }

                val selectedSymbol = if (rbSymbolO.isChecked) Player.O else Player.X
                preferences.preferredSymbol = selectedSymbol

                startActivity(
                    Intent(this, GameBoard::class.java)
                        .putExtra(EXTRA_GAME_MODE, selectedMode.name)
                        .putExtra(EXTRA_HUMAN_PLAYER, selectedSymbol.name)
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}