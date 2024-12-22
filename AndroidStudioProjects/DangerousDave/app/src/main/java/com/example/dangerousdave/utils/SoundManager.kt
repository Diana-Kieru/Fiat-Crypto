package com.example.dangerousdave.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.*

class SoundManager(private val context: Context) {
    // Sound pools for effects and music
    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null

    // Sound storage
    private val soundIds = mutableMapOf<String, Int>()
    private val soundStreams = mutableMapOf<Int, Int>()
    private val soundCache = LruCache<String, Int>(20)

    // State management
    private var isInitialized = false
    private var loadingJob: Job? = null

    // Volume control
    private var sfxVolume: Float = 1.0f
    private var musicVolume: Float = 0.8f
    private var isMuted: Boolean = false

    companion object {
        const val CATEGORY_PLAYER = "player"
        const val CATEGORY_ENEMY = "enemy"
        const val CATEGORY_COLLECTIBLE = "collectible"
        const val CATEGORY_UI = "ui"

        const val MAX_STREAMS = 10

        const val PRIORITY_LOW = 1
        const val PRIORITY_MEDIUM = 5
        const val PRIORITY_HIGH = 10

        private const val TAG = "SoundManager"
    }

    init {
        initializeSoundPool()
        loadSounds()
    }

    private fun initializeSoundPool() {
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attributes)
                .build()

            isInitialized = true
            Log.d(TAG, "SoundPool initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundPool: ${e.message}")
            isInitialized = false
        }
    }

    private fun loadSounds() {
        try {
            // Player sounds
            loadSound("jump", CATEGORY_PLAYER)
            loadSound("land", CATEGORY_PLAYER)
            loadSound("hurt", CATEGORY_PLAYER)
            loadSound("shoot", CATEGORY_PLAYER)

            // Enemy sounds
            loadSound("hit", CATEGORY_ENEMY)
            loadSound("die", CATEGORY_ENEMY)

            // Collectible sounds
            loadSound("trophy", CATEGORY_COLLECTIBLE)
            loadSound("powerup", CATEGORY_COLLECTIBLE)

            // UI sounds
            loadSound("click", CATEGORY_UI)
            loadSound("level_complete", CATEGORY_UI)

            Log.d(TAG, "All sounds loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in loadSounds: ${e.message}")
            isInitialized = false
        }
    }

    private fun loadSound(name: String, category: String) {
        try {
            val assetPath = "sounds/$category/$name.wav"  // Note the "sounds/" prefix
            Log.d(TAG, "Checking sound file: $assetPath")

            // Check if file exists first
            context.assets.list("sounds/$category")?.find { it == "$name.wav" } ?: run {
                Log.e(TAG, "Sound file not found: $assetPath")
                return
            }

            context.assets.openFd(assetPath).use { descriptor ->
                soundPool?.load(descriptor, 1)?.let { soundId ->
                    val key = "${category}_$name"
                    soundIds[key] = soundId
                    soundCache.put(key, soundId)
                    Log.d(TAG, "Successfully loaded sound: $assetPath")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound $name in category $category: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getCachedSound(name: String, category: String): Int? {
        val key = "${category}_$name"
        return soundCache.get(key) ?: soundIds[key]?.also {
            soundCache.put(key, it)
        }
    }

    fun playSound(name: String, category: String, loop: Boolean = false, priority: Int = PRIORITY_MEDIUM) {
        if (!isInitialized) {
            Log.w(TAG, "Cannot play sound - SoundManager not initialized")
            return
        }

        if (isMuted) {
            Log.d(TAG, "Sound muted - skipping playback")
            return
        }

        val soundId = getCachedSound(name, category) ?: run {
            Log.w(TAG, "Sound not found: $category/$name")
            return
        }

        try {
            val loopCount = if (loop) -1 else 0
            soundPool?.play(soundId, sfxVolume, sfxVolume, priority, loopCount, 1.0f)?.let { streamId ->
                soundStreams[streamId] = soundId
                Log.d(TAG, "Playing sound: $category/$name (StreamID: $streamId)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound $name: ${e.message}")
        }
    }

    fun stopSound(streamId: Int) {
        try {
            soundPool?.stop(streamId)
            soundStreams.remove(streamId)
            Log.d(TAG, "Stopped sound stream: $streamId")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sound stream $streamId: ${e.message}")
        }
    }

    fun playMusic(musicFile: String, loop: Boolean = true, fadeInMs: Long = 0) {
        if (!isInitialized || isMuted) {
            Log.w(TAG, "Cannot play music - system not initialized or muted")
            return
        }

        try {
            stopMusic()

            val assetPath = "music/$musicFile"
            Log.d(TAG, "Loading music: $assetPath")

            mediaPlayer = MediaPlayer().apply {
                context.assets.openFd(assetPath).use { descriptor ->
                    setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                }
                if (fadeInMs > 0) {
                    setVolume(0f, 0f)
                } else {
                    setVolume(musicVolume, musicVolume)
                }
                isLooping = loop
                prepare()
                start()
            }

            if (fadeInMs > 0) {
                fadeInMusic(fadeInMs)
            }
            Log.d(TAG, "Music started successfully: $musicFile")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing music $musicFile: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun fadeInMusic(durationMs: Long) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val fadeSteps = 10
                val stepDuration = durationMs / fadeSteps
                val volumeStep = musicVolume / fadeSteps

                var currentVolume = 0f
                repeat(fadeSteps) {
                    currentVolume += volumeStep
                    withContext(Dispatchers.Main) {
                        mediaPlayer?.setVolume(currentVolume, currentVolume)
                    }
                    delay(stepDuration)
                }
                Log.d(TAG, "Music fade-in completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during music fade-in: ${e.message}")
            }
        }
    }

    fun fadeOutMusic(durationMs: Long = 1000) {
        if (mediaPlayer?.isPlaying != true) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val fadeSteps = 10
                val stepDuration = durationMs / fadeSteps
                val volumeStep = musicVolume / fadeSteps

                var currentVolume = musicVolume
                repeat(fadeSteps) {
                    currentVolume -= volumeStep
                    withContext(Dispatchers.Main) {
                        mediaPlayer?.setVolume(currentVolume, currentVolume)
                    }
                    delay(stepDuration)
                }
                stopMusic()
                Log.d(TAG, "Music fade-out completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during music fade-out: ${e.message}")
            }
        }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
            Log.d(TAG, "Music stopped and released")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping music: ${e.message}")
        }
    }

    fun pauseMusic() {
        try {
            mediaPlayer?.pause()
            Log.d(TAG, "Music paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing music: ${e.message}")
        }
    }

    fun resumeMusic() {
        if (isMuted) {
            Log.d(TAG, "Cannot resume music - system is muted")
            return
        }

        try {
            mediaPlayer?.start()
            Log.d(TAG, "Music resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming music: ${e.message}")
        }
    }

    fun setSFXVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
        soundStreams.forEach { (streamId, _) ->
            soundPool?.setVolume(streamId, sfxVolume, sfxVolume)
        }
        Log.d(TAG, "SFX volume set to: $sfxVolume")
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(musicVolume, musicVolume)
        Log.d(TAG, "Music volume set to: $musicVolume")
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            stopAllSounds()
            pauseMusic()
            Log.d(TAG, "Sound system muted")
        } else {
            resumeMusic()
            Log.d(TAG, "Sound system unmuted")
        }
    }

    fun stopAllSounds() {
        try {
            soundStreams.keys.forEach { streamId ->
                soundPool?.stop(streamId)
            }
            soundStreams.clear()
            Log.d(TAG, "All sounds stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping all sounds: ${e.message}")
        }
    }

    fun release() {
        try {
            loadingJob?.cancel()
            stopAllSounds()
            stopMusic()
            soundPool?.release()
            soundPool = null
            soundCache.evictAll()
            isInitialized = false
            Log.d(TAG, "SoundManager released and cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during release: ${e.message}")
        }
    }

    // Utility methods
    fun isMusicPlaying(): Boolean = mediaPlayer?.isPlaying == true
    fun getSFXVolume(): Float = sfxVolume
    fun getMusicVolume(): Float = musicVolume
    fun isMuted(): Boolean = isMuted
    fun isReady(): Boolean = isInitialized
}