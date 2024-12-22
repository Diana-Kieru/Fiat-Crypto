package com.example.dangerousdave.game

import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.SurfaceHolder
import kotlin.math.abs

class GameLoop(
    private val surfaceHolder: SurfaceHolder,
    private val gameState: GameState
) : Thread() {

    companion object {
        private const val TAG = "GameLoop"
        const val MAX_FPS = 60
        const val FRAME_PERIOD = 1000.0f / MAX_FPS  // milliseconds
        const val MAX_FRAME_SKIPS = 5
        const val MAX_FRAME_TIME = 1000.0f / 30  // Don't allow updates slower than 30 FPS
    }

    // Game loop state
    private var isRunning: Boolean = false
    private var isPaused: Boolean = false
    private var isInitialized: Boolean = false

    // Timing variables
    private var lastUpdateTime: Long = 0
    private var lastFPSTime: Long = 0
    private var framesThisSecond: Int = 0
    private var currentFPS: Int = 0

    init {
        try {
            // Verify game state is properly initialized
            if (gameState.isInitialized()) {
                isInitialized = true
                Log.d(TAG, "GameLoop initialized successfully")
            } else {
                Log.e(TAG, "GameState not initialized during GameLoop creation")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing GameLoop: ${e.message}")
            isInitialized = false
        }
    }

    fun startLoop() {
        if (!isInitialized) {
            Log.e(TAG, "Cannot start loop - GameLoop not initialized")
            return
        }

        try {
            isRunning = true
            lastUpdateTime = System.currentTimeMillis()
            start()
            Log.d(TAG, "Game loop started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting game loop: ${e.message}")
            isRunning = false
        }
    }

    fun stopLoop() {
        try {
            isRunning = false
            join(1000) // Wait up to 1 second for the loop to stop
            Log.d(TAG, "Game loop stopped")
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error stopping game loop: ${e.message}")
            // Restore interrupted status
            Thread.currentThread().interrupt()
        }
    }

    fun pauseLoop() {
        try {
            isPaused = true
            gameState.pauseGame()
            Log.d(TAG, "Game loop paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing game loop: ${e.message}")
        }
    }

    fun resumeLoop() {
        try {
            isPaused = false
            lastUpdateTime = System.currentTimeMillis()
            gameState.resumeGame()
            Log.d(TAG, "Game loop resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming game loop: ${e.message}")
        }
    }

    override fun run() {
        if (!isInitialized) {
            Log.e(TAG, "Cannot run game loop - not initialized")
            return
        }

        var frameSkipCount: Int
        var afterTime: Long
        var beforeTime: Long
        var timeDiff: Long
        var sleepTime: Long
        var excessTime: Float

        while (isRunning) {
            if (!isPaused) {
                try {
                    frameSkipCount = 0
                    beforeTime = System.currentTimeMillis()

                    // Calculate and limit delta time
                    val deltaTime = calculateDeltaTime(beforeTime)

                    // Update and render
                    updateGame(deltaTime)
                    renderGame()

                    // Handle timing
                    afterTime = System.currentTimeMillis()
                    timeDiff = afterTime - beforeTime
                    sleepTime = (FRAME_PERIOD - timeDiff).toLong()

                    if (sleepTime > 0) {
                        handleExtraTime(sleepTime)
                    } else {
                        // Handle frame skipping when running slow
                        excessTime = abs(sleepTime).toFloat()
                        frameSkipCount = (excessTime / FRAME_PERIOD).toInt().coerceAtMost(MAX_FRAME_SKIPS)
                        handleFrameSkip(frameSkipCount)
                    }

                    updateFPSCounter(afterTime)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in game loop: ${e.message}")
                }
            } else {
                handlePausedState()
            }
        }
    }

    private fun calculateDeltaTime(currentTime: Long): Float {
        val deltaTime = (currentTime - lastUpdateTime) / 1000f
        lastUpdateTime = currentTime
        return deltaTime.coerceAtMost(MAX_FRAME_TIME / 1000f)
    }

    private fun handleExtraTime(sleepTime: Long) {
        try {
            sleep(sleepTime)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Sleep interrupted: ${e.message}")
            Thread.currentThread().interrupt()
        }
    }

    private fun handleFrameSkip(frameSkipCount: Int) {
        try {
            repeat(frameSkipCount) {
                updateGame(FRAME_PERIOD / 1000f)
            }
            Log.d(TAG, "Skipped $frameSkipCount frames to catch up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during frame skip: ${e.message}")
        }
    }

    private fun handlePausedState() {
        try {
            sleep(100) // Sleep while paused to avoid busy waiting
        } catch (e: InterruptedException) {
            Log.e(TAG, "Pause sleep interrupted: ${e.message}")
            Thread.currentThread().interrupt()
        }
    }

    private fun updateGame(deltaTime: Float) {
        try {
            gameState.update(deltaTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating game state: ${e.message}")
        }
    }

    private fun renderGame() {
        if (!isInitialized) {
            Log.e(TAG, "Cannot render - GameLoop not initialized")
            return
        }

        var canvas: Canvas? = null
        try {
            canvas = surfaceHolder.lockCanvas()
            synchronized(surfaceHolder) {
                canvas?.let {
                    // Clear the canvas with a background color (not just black)
                    it.drawColor(Color.rgb(135, 206, 235))  // Sky blue background

                    // Draw game state
                    gameState.render(it)

                    // Log successful render
                    Log.d(TAG, "Frame rendered successfully")
                } ?: run {
                    Log.e(TAG, "Failed to get canvas for rendering")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during render: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                canvas?.let { surfaceHolder.unlockCanvasAndPost(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error posting canvas: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun updateFPSCounter(currentTime: Long) {
        try {
            framesThisSecond++
            if (currentTime - lastFPSTime >= 1000) {
                currentFPS = framesThisSecond
                framesThisSecond = 0
                lastFPSTime = currentTime
                Log.v(TAG, "Current FPS: $currentFPS")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating FPS counter: ${e.message}")
        }
    }

    // Public getters
    fun getCurrentFPS(): Int = currentFPS
    fun isRunning(): Boolean = isRunning
    fun isPaused(): Boolean = isPaused
    fun isInitialized(): Boolean = isInitialized

    // Debug information
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "FPS" to currentFPS,
            "Running" to isRunning,
            "Paused" to isPaused,
            "Initialized" to isInitialized,
            "FramePeriod" to FRAME_PERIOD,
            "LastUpdateTime" to lastUpdateTime
        )
    }
}