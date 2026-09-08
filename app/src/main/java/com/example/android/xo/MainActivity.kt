package com.example.android.xo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.android.xo.engine.GameMode
import com.example.android.xo.util.WindowInsetsUtil
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GAME_MODE = "extra_game_mode"
        private const val BACK_PRESS_INTERVAL_MS = 2000L
    }

    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowInsetsUtil.applySystemBarInsets(findViewById(R.id.root))

        val btnVsAi: MaterialButton = findViewById(R.id.btn_vs_ai)
        val btnTwoPlayer: MaterialButton = findViewById(R.id.btn_two_player)
        val btnAbout: MaterialButton = findViewById(R.id.btn_about)
        val btnShare: MaterialButton = findViewById(R.id.btn_share)

        btnVsAi.setOnClickListener { showDifficultyDialog() }

        btnTwoPlayer.setOnClickListener {
            startActivity(
                Intent(this, GameBoard::class.java)
                    .putExtra(EXTRA_GAME_MODE, GameMode.TWO_PLAYERS.name)
            )
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

    private fun showDifficultyDialog() {
        val difficulties = arrayOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_medium),
            getString(R.string.difficulty_impossible)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.select_difficulty)
            .setItems(difficulties) { _, which ->
                val selectedMode = when (which) {
                    0 -> GameMode.VS_AI_EASY
                    1 -> GameMode.VS_AI_MEDIUM
                    else -> GameMode.VS_AI_IMPOSSIBLE
                }
                startActivity(
                    Intent(this, GameBoard::class.java)
                        .putExtra(EXTRA_GAME_MODE, selectedMode.name)
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}