package com.example.dangerousdave.game

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.dangerousdave.MainActivity
import com.example.dangerousdave.core.GameObject
import com.example.dangerousdave.entities.Player
import com.example.dangerousdave.entities.Projectile
import com.example.dangerousdave.ui.Controls
import com.example.dangerousdave.ui.HUD
import com.example.dangerousdave.utils.TextureManager

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    companion object {
        private const val TAG = "GameView"
        const val DESIGN_WIDTH = 1920f
        const val DESIGN_HEIGHT = 1080f
    }

    // Game components
    private lateinit var gameState: GameState
    private lateinit var gameLoop: GameLoop
    private val controls: Controls = Controls()
    private val hud: HUD = HUD()
    private var surfaceReadyListener: MainActivity? = null


    // Screen dimensions and scaling
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f

    // State flags
    private var isInitialized: Boolean = false
    private var isGameStarted: Boolean = false
    private var surfaceCreated: Boolean = false

    init {
        try {
            setupView()
        } catch (e: Exception) {
            Log.e(TAG, "Error in GameView initialization: ${e.message}")
        }
    }
    fun setSurfaceReadyListener(listener: MainActivity) {
        surfaceReadyListener = listener
    }

    // Call the listener when the surface is created
    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            if (!isInitialized) {
                Log.e(TAG, "Cannot create surface - GameView not initialized")
                return
            }

            // Initialize game loop
            gameLoop = GameLoop(holder, gameState)
            surfaceCreated = true
            Log.d(TAG, "Surface created successfully")

            // Notify the listener that the surface is ready
            surfaceReadyListener?.onSurfaceReady()
        } catch (e: Exception) {
            Log.e(TAG, "Error in surfaceCreated: ${e.message}")
            surfaceCreated = false
        }
    }

    private fun setupView() {
        try {
            // Add surface callback
            holder.addCallback(this)
            isFocusable = true

            // Initialize components
            initializeGameState()
            initializeTextures()
            initializeControls()

            isInitialized = true
            Log.d(TAG, "GameView setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during view setup: ${e.message}")
            isInitialized = false
        }
    }

    private fun initializeGameState() {
        gameState = GameState()
        gameState.initialize()
        Log.d(TAG, "Game state initialized")
    }

    private fun initializeTextures() {
        try {
            TextureManager.loadTextures(context.assets)
            Log.d(TAG, "Textures loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading textures: ${e.message}")
            throw e
        }
    }




    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        try {
            Log.d(TAG, "Surface changed: width=$width, height=$height")
            updateScreenDimensions(width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Error in surfaceChanged: ${e.message}")
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        try {
            stopGameLoop()
            Log.d(TAG, "Surface destroyed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in surfaceDestroyed: ${e.message}")
        }
    }

    private fun updateScreenDimensions(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        scaleX = width / DESIGN_WIDTH
        scaleY = height / DESIGN_HEIGHT

        // Update UI components with new dimensions
        controls.updateDimensions(width, height)
        hud.updateDimensions(width, height)

        Log.d(TAG, "Screen dimensions updated - Width: $width, Height: $height")
    }

    private fun stopGameLoop() {
        try {
            if (::gameLoop.isInitialized) {
                gameLoop.stopLoop()
                var retry = true
                while (retry) {
                    try {
                        gameLoop.join()
                        retry = false
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "Error joining game loop thread: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping game loop: ${e.message}")
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isGameStarted) return true

        val scaledX = event.x / scaleX
        val scaledY = event.y / scaleY

        try {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> handleTouchDown(scaledX, scaledY)
                MotionEvent.ACTION_MOVE -> handleTouchMove(scaledX, scaledY)
                MotionEvent.ACTION_UP -> handleTouchUp(scaledX, scaledY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling touch event: ${e.message}")
        }

        return true
    }

    private fun handleTouchDown(x: Float, y: Float) {
        when {
            controls.isLeftPressed(x, y) -> movePlayerLeft()
            controls.isRightPressed(x, y) -> movePlayerRight()
            controls.isJumpPressed(x, y) -> playerJump()
            controls.isShootPressed(x, y) -> playerShoot()
        }
    }

    private fun handleTouchMove(x: Float, y: Float) {
        when {
            controls.isLeftPressed(x, y) -> movePlayerLeft()
            controls.isRightPressed(x, y) -> movePlayerRight()
            else -> stopPlayerMovement()
        }
    }

    private fun handleTouchUp(x: Float, y: Float) {
        stopPlayerMovement()
    }

    // Player control methods
    private fun movePlayerLeft() {
        gameState.getPlayer()?.apply {
            moveLeft()
            Log.d(TAG, "Player moving left")
        }
    }

    private fun movePlayerRight() {
        gameState.getPlayer()?.apply {
            moveRight()
            Log.d(TAG, "Player moving right")
        }
    }

    private fun stopPlayerMovement() {
        gameState.getPlayer()?.apply {
            stopMoving()
            Log.d(TAG, "Player movement stopped")
        }
    }

    private fun playerJump() {
        gameState.getPlayer()?.apply {
            jump()
            Log.d(TAG, "Player jumped")
        }
    }

    private fun playerShoot() {
        gameState.getPlayer()?.let { player ->
            if (player.hasGun()) {
                try {
                    val projectile = createPlayerProjectile(player)
                    gameState.addProjectile(projectile)
                    Log.d(TAG, "Player shot projectile")
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating projectile: ${e.message}")
                }
            }
        }
    }

    private fun createPlayerProjectile(player: Player): Projectile {
        val projectileX = if (player.facing == GameObject.Direction.RIGHT) {
            player.x + player.width
        } else {
            player.x
        }

        return Projectile(
            x = projectileX,
            y = player.y + player.height / 2,
            width = 10f,
            height = 10f,
            fromPlayer = true
        ).apply {
            shoot(player.facing)
        }
    }

    // Public methods for game control
    fun isReady(): Boolean {
        val ready = isInitialized && ::gameState.isInitialized
        Log.d(TAG, "GameView ready check - initialized: $isInitialized, gameState initialized: ${::gameState.isInitialized}")
        return ready
    }

    fun getGameState(): GameState = gameState

    fun startGame() {
        try {
            if (!isInitialized) {
                Log.e(TAG, "Cannot start game - GameView not initialized")
                return
            }

            if (!surfaceCreated) {
                Log.e(TAG, "Cannot start game - Surface not created")
                return
            }

            gameLoop.startLoop()
            Log.d(TAG, "Game started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting game: ${e.message}")
        }
    }


    fun pauseGame() {
        try {
            if (::gameLoop.isInitialized) {
                gameLoop.pauseLoop()
                Log.d(TAG, "Game paused")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing game: ${e.message}")
        }
    }

    private fun initializeControls() {
        try {
            // Initialize the controls by setting their initial dimensions
            controls.updateDimensions(width, height)
            hud.updateDimensions(width, height)
            Log.d(TAG, "Controls and HUD initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing controls: ${e.message}")
            throw e
        }
    }

    fun resumeGame() {
        try {
            if (::gameLoop.isInitialized) {
                gameLoop.resumeLoop()
                Log.d(TAG, "Game resumed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming game: ${e.message}")
        }
    }

    fun stopGame() {
        try {
            stopGameLoop()
            isGameStarted = false
            Log.d(TAG, "Game stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping game: ${e.message}")
        }
    }


    // Debug info
    fun getDebugInfo(): Map<String, Any> {
        return try {
            mapOf(
                "FPS" to (if (::gameLoop.isInitialized) gameLoop.getCurrentFPS() else 0),
                "Scale" to "X: $scaleX, Y: $scaleY",
                "Screen" to "W: $screenWidth, H: $screenHeight",
                "GameStarted" to isGameStarted,
                "Initialized" to isInitialized
            ) + (if (::gameState.isInitialized) gameState.getDebugInfo() else emptyMap())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting debug info: ${e.message}")
            mapOf("Error" to "Failed to get debug info")
        }
    }
}