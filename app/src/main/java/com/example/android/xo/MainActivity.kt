package com.example.android.xo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.android.xo.audio.SoundManager
import com.example.android.xo.data.GamePreferences
import com.example.android.xo.ui.XoApp
import com.example.android.xo.ui.theme.XoTheme

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_GAME_MODE = "extra_game_mode"
        const val EXTRA_HUMAN_PLAYER = "extra_human_player"
    }

    private val preferences by lazy { GamePreferences(applicationContext) }
    private val soundManager by lazy { SoundManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XoTheme {
                XoApp(preferences = preferences, soundManager = soundManager)
            }
        }
    }

    override fun onDestroy() {
        soundManager.release()
        super.onDestroy()
    }
}
