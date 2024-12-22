package com.example.dangerousdave

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.dangerousdave.game.GameLoop
import com.example.dangerousdave.game.GameView
import com.example.dangerousdave.utils.SoundManager
import com.example.dangerousdave.levels.LevelManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Game components
    private lateinit var gameView: GameView
//    private lateinit var soundManager: SoundManager
    private lateinit var levelManager: LevelManager
    private lateinit var gameLoop: GameLoop

    // UI elements
    private lateinit var loadingScreen: FrameLayout
    private lateinit var pauseMenu: FrameLayout
    private lateinit var gameOverScreen: FrameLayout
    private lateinit var finalScoreText: TextView

    // Game state
    private var isPaused: Boolean = false
    private var isGameOver: Boolean = false
    private var isInitialized: Boolean = false
    private var pendingGameStart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setupWindow()
            setContentView(R.layout.activity_main)

            if (initializeGame()) {
                // Now GameLoop is already initialized when we get here
                pendingGameStart = true
            } else {
                handleInitializationError()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during onCreate: ${e.message}")
            handleFatalError(e)
        }
    }


    private fun initializeGameComponents(): Boolean {
        try {
            // Wait a short moment for GameView to complete initialization
            if (!gameView.isReady()) {
                Log.e(TAG, "GameView failed to initialize")
                return false
            }

            // Initialize GameLoop first
            gameLoop = GameLoop(gameView.holder, gameView.getGameState())
            if (!gameLoop.isInitialized()) {
                Log.e(TAG, "GameLoop failed to initialize")
                return false
            }

            // Initialize level manager
            levelManager = LevelManager(this, gameView.getGameState())

            Log.d(TAG, "Game components initialized successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing game components: ${e.message}")
            return false
        }
    }


    fun onSurfaceReady() {
        if (pendingGameStart) {
            startGame()
            pendingGameStart = false
        }
    }
    private fun setupWindow() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }




        private fun pauseGame() {
            if (!isGameOver) {
                try {
                    isPaused = true
                    gameView.pauseGame()
                    if (::gameLoop.isInitialized) {
                        gameLoop.pauseLoop()
                    }
                    showPauseMenu()
                    Log.d(TAG, "Game paused")
                } catch (e: Exception) {
                    Log.e(TAG, "Error pausing game: ${e.message}")
                }
            }
        }

        private fun resumeGame() {
            try {
                isPaused = false
                hidePauseMenu()
                gameView.resumeGame()
                if (::gameLoop.isInitialized) {
                    gameLoop.resumeLoop()
                }
                Log.d(TAG, "Game resumed")
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming game: ${e.message}")
            }
        }




    private fun initializeViews() {
        try {
            gameView = findViewById(R.id.gameView)
            loadingScreen = findViewById(R.id.loadingScreen)
            pauseMenu = findViewById(R.id.pauseMenu)
            gameOverScreen = findViewById(R.id.gameOverScreen)
            finalScoreText = findViewById(R.id.finalScoreText)

            // Initially hide all UI elements
            loadingScreen.visibility = View.GONE
            pauseMenu.visibility = View.GONE
            gameOverScreen.visibility = View.GONE

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            throw e
        }
    }


    private fun initializeGame(): Boolean {
        try {
            // Initialize views first
            initializeViews()

            // Wait for GameView to be ready before continuing
            var retryCount = 0
            while (!gameView.isReady() && retryCount < 3) {
                Thread.sleep(100) // Wait 100ms
                retryCount++
            }

            if (!gameView.isReady()) {
                Log.e(TAG, "GameView failed to initialize after retries")
                return false
            }

            if (!initializeGameComponents()) {
                return false
            }

            setupClickListeners()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error during game initialization: ${e.message}")
            return false
        }
    }


    private fun setupClickListeners() {
        try {
            // Pause menu buttons
            findViewById<Button>(R.id.resumeButton).setOnClickListener { resumeGame() }
            findViewById<Button>(R.id.restartButton).setOnClickListener { restartLevel() }
            findViewById<Button>(R.id.settingsButton).setOnClickListener { showSettings() }
            findViewById<Button>(R.id.quitButton).setOnClickListener { quitToMenu() }

            // Game over screen buttons
            findViewById<Button>(R.id.tryAgainButton).setOnClickListener { restartLevel() }
            findViewById<Button>(R.id.mainMenuButton).setOnClickListener { quitToMenu() }

            Log.d(TAG, "Click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}")
            throw e
        }
    }

//    private fun startGame() {
//        if (!isInitialized) {
//            Log.e(TAG, "Cannot start game - not initialized")
//            return
//        }
//
//        try {
//            isGameOver = false
//            showLoadingScreen()
//
//            // Start background music
////            if (soundManager.isReady()) {
////                soundManager.playMusic("background_music.mp3", true)
////            }
//
//            // Initialize level and start game components
//            levelManager.startGame()
//            gameView.startGame()
//            gameLoop.startLoop()
//
//            hideLoadingScreen()
//            Log.d(TAG, "Game started successfully")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error starting game: ${e.message}")
//            handleGameError(e)
//        }
//    }
private fun startGame() {
    try {
        showLoadingScreen()

        // Initialize level and start game
        levelManager.startGame()
        gameView.startGame()

        hideLoadingScreen()
        Log.d(TAG, "Game started successfully")
    } catch (e: Exception) {
        Log.e(TAG, "Error starting game: ${e.message}")
        handleGameError(e)
    }
}


    private fun restartLevel() {
        try {
            hidePauseMenu()
            hideGameOverScreen()
            showLoadingScreen()

            levelManager.restartLevel()
            gameView.startGame()

            hideLoadingScreen()
            Log.d(TAG, "Level restarted")
        } catch (e: Exception) {
            Log.e(TAG, "Error restarting level: ${e.message}")
            handleGameError(e)
        }
    }

    private fun gameOver(finalScore: Int) {
        try {
            isGameOver = true
            gameView.stopGame()
            gameLoop.stopLoop()

//            if (soundManager.isReady()) {
//                soundManager.playSound("game_over", SoundManager.CATEGORY_UI)
//            }

            finalScoreText.text = "Final Score: $finalScore"
            showGameOverScreen()
            Log.d(TAG, "Game over - Final score: $finalScore")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling game over: ${e.message}")
        }
    }

    // UI visibility controls with error handling
    private fun showLoadingScreen() {
        try {
            loadingScreen.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading screen: ${e.message}")
        }
    }

    private fun hideLoadingScreen() {
        try {
            loadingScreen.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding loading screen: ${e.message}")
        }
    }

    private fun showPauseMenu() {
        try {
            pauseMenu.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing pause menu: ${e.message}")
        }
    }

    private fun hidePauseMenu() {
        try {
            pauseMenu.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding pause menu: ${e.message}")
        }
    }

    private fun showGameOverScreen() {
        try {
            gameOverScreen.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing game over screen: ${e.message}")
        }
    }

    private fun hideGameOverScreen() {
        try {
            gameOverScreen.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding game over screen: ${e.message}")
        }
    }

    private fun showSettings() {
        // TODO: Implement settings menu
        Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun quitToMenu() {
        try {
            cleanupGame()
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error quitting to menu: ${e.message}")
            finish()
        }
    }

    // Error handling
    private fun handleInitializationError() {
        Log.e(TAG, "Game initialization failed")
        Toast.makeText(this, "Failed to initialize game", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun handleGameError(e: Exception) {
        Log.e(TAG, "Game error occurred: ${e.message}")
        Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show()
    }

    private fun handleFatalError(e: Exception) {
        Log.e(TAG, "Fatal error occurred: ${e.message}")
        Toast.makeText(this, "A fatal error occurred", Toast.LENGTH_LONG).show()
        finish()
    }

    // Activity lifecycle
    override fun onResume() {
        super.onResume()
        if (isInitialized && !isGameOver && !isPaused) {
            try {
                gameView.resumeGame()
//                soundManager.resumeMusic()
                gameLoop.resumeLoop()
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming game state: ${e.message}")
            }
        }
    }


    override fun onPause() {
        super.onPause()
        if (isInitialized && !isGameOver && !isPaused) {
            pauseGame()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupGame()
    }

    private fun cleanupGame() {
        try {
//            if (::soundManager.isInitialized) soundManager.release()
            if (::gameView.isInitialized) gameView.stopGame()
            if (::gameLoop.isInitialized) gameLoop.stopLoop()
            Log.d(TAG, "Game cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
    }

    // Back button handling
    override fun onBackPressed() {
        when {
            isGameOver -> super.onBackPressed()
            isPaused -> resumeGame()
            else -> pauseGame()
        }
    }
}