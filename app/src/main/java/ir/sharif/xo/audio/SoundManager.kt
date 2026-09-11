package ir.sharif.xo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import ir.sharif.xo.R

class SoundManager(private val context: Context) {

    private companion object {
        const val TAG = "SoundManager"
    }

    private var soundPool: SoundPool? = null
    private var moveSoundId: Int = 0
    private var winSoundId: Int = 0
    private var drawSoundId: Int = 0
    private var isLoaded: Boolean = false

    var isSoundEnabled: Boolean = true

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    setOnLoadCompleteListener { _, _, status ->
                        if (status == 0) {
                            isLoaded = true
                        }
                    }
                    moveSoundId = load(context, R.raw.sound_move, 1)
                    winSoundId = load(context, R.raw.sound_win, 1)
                    drawSoundId = load(context, R.raw.sound_draw, 1)
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize SoundPool: ${e.message}")
        }
    }

    fun playMove() {
        if (!isSoundEnabled || soundPool == null) return
        soundPool?.play(moveSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
    }

    fun playWin() {
        if (!isSoundEnabled || soundPool == null) return
        soundPool?.play(winSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
    }

    fun playDraw() {
        if (!isSoundEnabled || soundPool == null) return
        soundPool?.play(drawSoundId, 0.9f, 0.9f, 1, 0, 1.0f)
    }

    fun release() {
        try {
            soundPool?.release()
        } catch (_: Exception) {
        }
        soundPool = null
    }
}
