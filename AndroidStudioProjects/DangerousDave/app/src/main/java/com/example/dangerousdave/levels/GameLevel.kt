package com.example.dangerousdave.levels


import com.example.dangerousdave.entities.Enemy
import com.example.dangerousdave.entities.Platform
import com.example.dangerousdave.entities.Collectible

data class GameLevel(
    // Level metadata
    val levelId: Int,
    val levelName: String,

    // Player starting position
    val playerStartX: Float,
    val playerStartY: Float,

    // Level entities
    val platforms: List<Platform>,
    val enemies: List<Enemy>,
    val collectibles: List<Collectible>,

    // Level boundaries
    val levelWidth: Float,
    val levelHeight: Float,

    // Level requirements
    val requiredScore: Int = 0,
    val timeLimit: Float = Float.POSITIVE_INFINITY,

    // Level properties
    val backgroundColor: Int = android.graphics.Color.BLACK,
    val backgroundMusic: String = "",
    val gravity: Float = 1000f  // Default gravity
) {
    companion object {
        // Helper function to create an empty level
        fun createEmpty(levelId: Int): GameLevel {
            return GameLevel(
                levelId = levelId,
                levelName = "Level $levelId",
                playerStartX = 100f,
                playerStartY = 100f,
                platforms = emptyList(),
                enemies = emptyList(),
                collectibles = emptyList(),
                levelWidth = 1920f,
                levelHeight = 1080f
            )
        }
    }

    // Validation method to check if level is properly configured
    fun isValid(): Boolean {
        return platforms.isNotEmpty() &&
                playerStartX >= 0 && playerStartX <= levelWidth &&
                playerStartY >= 0 && playerStartY <= levelHeight
    }

    // Helper method to get all collision objects
    fun getCollisionObjects(): List<Platform> = platforms

    // Helper method to get required collectibles
    fun getRequiredCollectibles(): List<Collectible> =
        collectibles.filter { it.type == Collectible.Type.TROPHY }

    // Helper method to check if coordinates are within level bounds
    fun isInBounds(x: Float, y: Float): Boolean =
        x >= 0 && x <= levelWidth && y >= 0 && y <= levelHeight
}