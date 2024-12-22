package com.example.dangerousdave.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.dangerousdave.levels.GameLevel
import com.example.dangerousdave.entities.*

class GameState {
    companion object {
        private const val TAG = "GameState"
        const val MAX_LEVELS = 10
        const val TIME_BONUS_THRESHOLD = 60f  // Seconds
        const val TIME_BONUS_POINTS = 1000    // Points awarded for quick completion
    }

    // Game entities
    private lateinit var player: Player
    private val enemies = mutableListOf<Enemy>()
    private val platforms = mutableListOf<Platform>()
    private val collectibles = mutableListOf<Collectible>()
    private val projectiles = mutableListOf<Projectile>()

    // Game state
    private var currentLevel: GameLevel? = null
    private var levelNumber: Int = 1
    private var gameOver: Boolean = false
    private var paused: Boolean = false
    private var levelCompleted: Boolean = false
    private var isInitialized: Boolean = false

    // Scoring and time
    private var totalScore: Int = 0
    private var levelTime: Float = 0f
    private var totalTime: Float = 0f

    // Initialize game state
    fun initialize(startLevel: Int = 1) {
        try {
            // Create and initialize player
            player = Player(
                x = 100f,    // Default starting position
                y = 100f,
                width = 64f, // Default size
                height = 64f
            )

            levelNumber = startLevel
            gameOver = false
            paused = false
            levelCompleted = false
            totalScore = 0
            totalTime = 0f
            levelTime = 0f

            // Add some test platforms
            platforms.add(Platform(100f, 500f, 300f, 20f, Platform.PlatformType.SOLID))
            platforms.add(Platform(400f, 400f, 300f, 20f, Platform.PlatformType.SOLID))

            // Add a test collectible
            collectibles.add(Collectible(500f, 300f, 30f, 30f, Collectible.Type.TROPHY))

            // Add a test enemy
            enemies.add(Enemy(600f, 200f, 50f, 50f, Enemy.EnemyType.WALKER))

            isInitialized = true
            Log.d(TAG, "GameState initialized with test entities")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing GameState: ${e.message}")
            isInitialized = false
            throw e
        }
    }

    // Update game state
    fun update(deltaTime: Float) {
        if (!isInitialized) {
            Log.e(TAG, "Attempting to update before initialization")
            return
        }

        if (gameOver || paused) return

        try {
            levelTime += deltaTime
            totalTime += deltaTime

            updateEntities(deltaTime)
            checkCollisions()
            cleanupEntities()
            checkLevelCompletion()
        } catch (e: Exception) {
            Log.e(TAG, "Error during update: ${e.message}")
        }
    }

    private fun updateEntities(deltaTime: Float) {
        try {
            if (::player.isInitialized) {
                player.update(deltaTime)
            }

            enemies.forEach { it.update(deltaTime) }
            platforms.forEach { it.update(deltaTime) }
            collectibles.forEach { it.update(deltaTime) }
            projectiles.forEach { it.update(deltaTime) }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating entities: ${e.message}")
        }
    }
    fun render(canvas: Canvas) {
        try {
            // Draw background first
            canvas.drawColor(Color.rgb(135, 206, 235))  // Sky blue background

            // Debug shapes to verify rendering
            val debugPaint = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            }
            canvas.drawRect(100f, 100f, 300f, 300f, debugPaint)

            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 50f
            }
            canvas.drawText("Score: $totalScore", 50f, 50f, textPaint)

            // Draw game elements
            platforms.forEach { it.render(canvas) }
            collectibles.forEach { it.render(canvas) }
            enemies.forEach { it.render(canvas) }
            projectiles.forEach { it.render(canvas) }

            // Draw player last (on top)
            if (::player.isInitialized) {
                player.render(canvas)
            }

            Log.d(TAG, "Game elements rendered - Player: ${::player.isInitialized}, " +
                    "Platforms: ${platforms.size}, " +
                    "Collectibles: ${collectibles.size}, " +
                    "Enemies: ${enemies.size}, " +
                    "Projectiles: ${projectiles.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering game state: ${e.message}")
            e.printStackTrace()
        }
    }
    private fun checkCollisions() {
        if (!::player.isInitialized) return

        try {
            // Player with platforms
            platforms.forEach { platform ->
                if (player.intersects(platform)) {
                    player.onCollision(platform)
                }
            }

            // Player with enemies
            enemies.forEach { enemy ->
                if (player.intersects(enemy)) {
                    player.onCollision(enemy)
                }
            }

            // Player with collectibles
            collectibles.forEach { collectible ->
                if (!collectible.isCollected() && player.intersects(collectible)) {
                    player.onCollision(collectible)
                    totalScore += collectible.getPoints()
                }
            }

            // Handle projectile collisions
            handleProjectileCollisions()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking collisions: ${e.message}")
        }
    }

    private fun handleProjectileCollisions() {
        projectiles.forEach { projectile ->
            // Projectile with platforms
            platforms.forEach { platform ->
                if (projectile.intersects(platform)) {
                    projectile.onCollision(platform)
                }
            }

            if (projectile.isFromPlayer()) {
                // Player projectiles with enemies
                enemies.forEach { enemy ->
                    if (projectile.intersects(enemy)) {
                        projectile.onCollision(enemy)
                        enemy.onCollision(projectile)
                        if (!enemy.isActive) {
                            totalScore += enemy.getPoints()
                        }
                    }
                }
            } else {
                // Enemy projectiles with player
                if (::player.isInitialized && projectile.intersects(player)) {
                    projectile.onCollision(player)
                    player.onCollision(projectile)
                }
            }
        }
    }

    private fun cleanupEntities() {
        try {
            enemies.removeAll { !it.isActive }
            projectiles.removeAll { !it.isActive }
            collectibles.removeAll { it.isCollected() }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up entities: ${e.message}")
        }
    }

    private fun checkLevelCompletion() {
        if (!::player.isInitialized) return

        try {
            if (player.isLevelComplete() || shouldCompleteLevel()) {
                levelCompleted = true
                calculateLevelBonus()
                Log.d(TAG, "Level completed! Score: $totalScore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking level completion: ${e.message}")
        }
    }

    private fun shouldCompleteLevel(): Boolean {
        return collectibles.none { !it.isCollected() && it.type == Collectible.Type.TROPHY }
    }

    private fun calculateLevelBonus() {
        if (levelTime < TIME_BONUS_THRESHOLD) {
            val timeBonus = TIME_BONUS_POINTS
            totalScore += timeBonus
            Log.d(TAG, "Time bonus awarded: $timeBonus")
        }
    }

    // Level management
    fun loadLevel(level: GameLevel) {
        try {
            currentLevel = level
            levelTime = 0f
            levelCompleted = false

            // Clear existing entities
            enemies.clear()
            platforms.clear()
            collectibles.clear()
            projectiles.clear()

            // Add new entities from level
            enemies.addAll(level.enemies)
            platforms.addAll(level.platforms)
            collectibles.addAll(level.collectibles)

            // Set player position if initialized
            if (::player.isInitialized) {
                player.setPosition(level.playerStartX, level.playerStartY)
                player.reset()
            }

            Log.d(TAG, "Level $levelNumber loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading level: ${e.message}")
            throw e
        }
    }

    fun nextLevel() {
        if (levelNumber < MAX_LEVELS) {
            levelNumber++
            Log.d(TAG, "Advancing to level $levelNumber")
        } else {
            gameOver = true
            Log.d(TAG, "Game completed! Final score: $totalScore")
        }
    }

    // Entity management
    fun addProjectile(projectile: Projectile) {
        try {
            projectiles.add(projectile)
            Log.d(TAG, "Projectile added")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding projectile: ${e.message}")
        }
    }

    fun removeProjectile(projectile: Projectile) {
        projectiles.remove(projectile)
    }

    // State queries with null safety
    fun getPlayer(): Player? = if (::player.isInitialized) player else null
    fun isGameOver(): Boolean = gameOver
    fun isPaused(): Boolean = paused
    fun isLevelCompleted(): Boolean = levelCompleted
    fun getCurrentLevel(): Int = levelNumber
    fun getTotalScore(): Int = totalScore
    fun getLevelTime(): Float = levelTime
    fun getTotalTime(): Float = totalTime
    fun isInitialized(): Boolean = isInitialized

    // State controls
    fun pauseGame() {
        paused = true
        Log.d(TAG, "Game paused")
    }

    fun resumeGame() {
        paused = false
        Log.d(TAG, "Game resumed")
    }

    fun endGame() {
        gameOver = true
        Log.d(TAG, "Game ended. Final score: $totalScore")
    }

    fun setPlayer(newPlayer: Player) {
        player = newPlayer
        Log.d(TAG, "New player set")
    }



    // Debug information
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "Level" to levelNumber,
            "Score" to totalScore,
            "Time" to totalTime,
            "Enemies" to enemies.size,
            "Collectibles" to collectibles.size,
            "Projectiles" to projectiles.size,
            "PlayerInitialized" to ::player.isInitialized,
            "GameInitialized" to isInitialized
        )
    }
}