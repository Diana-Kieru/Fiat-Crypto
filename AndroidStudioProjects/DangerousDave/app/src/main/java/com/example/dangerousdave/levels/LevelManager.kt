package com.example.dangerousdave.levels


import android.content.Context
import com.example.dangerousdave.game.GameState

class LevelManager(
    private val context: Context,
    private val gameState: GameState
) {
    private val levelLoader: LevelLoader = LevelLoader(context)

    // Level tracking
    private var currentLevelId: Int = 1
    private var maxUnlockedLevel: Int = 1
    private var totalLevels: Int = 0

    // Level states
    private var isLoadingLevel: Boolean = false
    private var levelLoadError: String? = null

    companion object {
        const val FIRST_LEVEL = 1
        const val SCORE_MULTIPLIER = 100  // Base score multiplier for level completion
    }

    init {
        totalLevels = levelLoader.getTotalLevels()
    }

    // Start game from beginning or specific level
    fun startGame(startLevel: Int = FIRST_LEVEL) {
        currentLevelId = startLevel.coerceIn(FIRST_LEVEL, maxUnlockedLevel)
        loadCurrentLevel()
    }

    // Load specific level
    fun loadLevel(levelId: Int): Boolean {
        if (!canLoadLevel(levelId)) return false

        isLoadingLevel = true
        levelLoadError = null

        try {
            val level = levelLoader.loadLevel(levelId)
            if (level != null) {
                currentLevelId = levelId
                gameState.loadLevel(level)
                isLoadingLevel = false
                return true
            } else {
                levelLoadError = "Failed to load level $levelId"
            }
        } catch (e: Exception) {
            levelLoadError = "Error loading level: ${e.message}"
            e.printStackTrace()
        }

        isLoadingLevel = false
        return false
    }

    // Progress to next level
    fun nextLevel(): Boolean {
        if (currentLevelId >= totalLevels) {
            return false
        }

        val nextLevelId = currentLevelId + 1
        if (canLoadLevel(nextLevelId)) {
            // Update progression
            if (nextLevelId > maxUnlockedLevel) {
                maxUnlockedLevel = nextLevelId
            }

            // Calculate and award level completion bonus
            awardLevelCompletionBonus()

            // Load next level
            return loadLevel(nextLevelId)
        }

        return false
    }

    // Restart current level
    fun restartLevel(): Boolean {
        return loadLevel(currentLevelId)
    }

    // Check if level can be loaded
    private fun canLoadLevel(levelId: Int): Boolean {
        return levelId in FIRST_LEVEL..maxUnlockedLevel &&
                levelId <= totalLevels &&
                levelLoader.levelExists(levelId)
    }

    // Load current level
    private fun loadCurrentLevel() {
        loadLevel(currentLevelId)
    }

    // Award completion bonus
    private fun awardLevelCompletionBonus() {
        val timeBonus = calculateTimeBonus()
        val levelBonus = SCORE_MULTIPLIER * currentLevelId
        val totalBonus = timeBonus + levelBonus

        gameState.getPlayer()?.addScore(totalBonus)
    }

    // Calculate time bonus
    private fun calculateTimeBonus(): Int {
        val levelTime = gameState.getLevelTime()
        return when {
            levelTime < 30f -> SCORE_MULTIPLIER * 3  // Super fast completion
            levelTime < 60f -> SCORE_MULTIPLIER * 2  // Fast completion
            levelTime < 90f -> SCORE_MULTIPLIER      // Normal completion
            else -> 0                                // Slow completion
        }
    }

    // Save/Load level progress
    fun saveProgress() {
        context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
            .edit()
            .putInt("max_unlocked_level", maxUnlockedLevel)
            .apply()
    }

    fun loadProgress() {
        maxUnlockedLevel = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
            .getInt("max_unlocked_level", FIRST_LEVEL)
    }

    // Level information
    fun getCurrentLevelId(): Int = currentLevelId
    fun getMaxUnlockedLevel(): Int = maxUnlockedLevel
    fun getTotalLevels(): Int = totalLevels
    fun isLastLevel(): Boolean = currentLevelId == totalLevels
    fun isLoading(): Boolean = isLoadingLevel
    fun getLoadError(): String? = levelLoadError

    // Level status
    fun isLevelUnlocked(levelId: Int): Boolean = levelId <= maxUnlockedLevel
    fun hasNextLevel(): Boolean = currentLevelId < totalLevels
    fun hasPreviousLevel(): Boolean = currentLevelId > FIRST_LEVEL

    // Debug information
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "Current Level" to currentLevelId,
            "Max Unlocked" to maxUnlockedLevel,
            "Total Levels" to totalLevels,
            "Loading" to isLoadingLevel,
            "Error" to (levelLoadError ?: "None")
        )
    }
}