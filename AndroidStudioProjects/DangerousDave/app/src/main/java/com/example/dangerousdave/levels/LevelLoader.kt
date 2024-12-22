package com.example.dangerousdave.levels


import android.content.Context
import android.util.JsonReader
import com.example.dangerousdave.entities.*
import org.json.JSONObject
import java.io.InputStreamReader

class LevelLoader(private val context: Context) {

    companion object {
        private const val LEVELS_DIR = "levels"
        private const val LEVEL_FILE_PREFIX = "level_"
        private const val LEVEL_FILE_EXTENSION = ".json"
    }

    // Load a specific level by ID
    fun loadLevel(levelId: Int): GameLevel? {
        try {
            val fileName = "$LEVEL_FILE_PREFIX$levelId$LEVEL_FILE_EXTENSION"
            val inputStream = context.assets.open("$LEVELS_DIR/$fileName")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            return parseLevel(JSONObject(jsonString), levelId)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun parseLevel(json: JSONObject, levelId: Int): GameLevel {
        // Parse level metadata
        val levelName = json.getString("name")
        val playerStartX = json.getDouble("playerStartX").toFloat()
        val playerStartY = json.getDouble("playerStartY").toFloat()
        val levelWidth = json.getDouble("width").toFloat()
        val levelHeight = json.getDouble("height").toFloat()
        val backgroundColor = json.optInt("backgroundColor", android.graphics.Color.BLACK)
        val backgroundMusic = json.optString("backgroundMusic", "")
        val gravity = json.optDouble("gravity", 1000.0).toFloat()

        // Parse platforms
        val platforms = mutableListOf<Platform>()
        val platformsArray = json.getJSONArray("platforms")
        for (i in 0 until platformsArray.length()) {
            val platformJson = platformsArray.getJSONObject(i)
            platforms.add(parsePlatform(platformJson))
        }

        // Parse enemies
        val enemies = mutableListOf<Enemy>()
        val enemiesArray = json.getJSONArray("enemies")
        for (i in 0 until enemiesArray.length()) {
            val enemyJson = enemiesArray.getJSONObject(i)
            enemies.add(parseEnemy(enemyJson))
        }

        // Parse collectibles
        val collectibles = mutableListOf<Collectible>()
        val collectiblesArray = json.getJSONArray("collectibles")
        for (i in 0 until collectiblesArray.length()) {
            val collectibleJson = collectiblesArray.getJSONObject(i)
            collectibles.add(parseCollectible(collectibleJson))
        }

        return GameLevel(
            levelId = levelId,
            levelName = levelName,
            playerStartX = playerStartX,
            playerStartY = playerStartY,
            platforms = platforms,
            enemies = enemies,
            collectibles = collectibles,
            levelWidth = levelWidth,
            levelHeight = levelHeight,
            backgroundColor = backgroundColor,
            backgroundMusic = backgroundMusic,
            gravity = gravity
        )
    }

    private fun parsePlatform(json: JSONObject): Platform {
        val x = json.getDouble("x").toFloat()
        val y = json.getDouble("y").toFloat()
        val width = json.getDouble("width").toFloat()
        val height = json.getDouble("height").toFloat()
        val typeString = json.getString("type")
        val type = Platform.PlatformType.valueOf(typeString)

        return Platform(x, y, width, height, type).apply {
            // Configure moving platform properties if applicable
            if (type == Platform.PlatformType.MOVING) {
                json.optDouble("movementDistance", 200.0).toFloat().let { setMovementDistance(it) }
                json.optDouble("movementSpeed", 100.0).toFloat().let { setMovementSpeed(it) }
                json.optBoolean("verticalMovement", false).let { setVerticalMovement(it) }
            }
        }
    }

    private fun parseEnemy(json: JSONObject): Enemy {
        val x = json.getDouble("x").toFloat()
        val y = json.getDouble("y").toFloat()
        val width = json.getDouble("width").toFloat()
        val height = json.getDouble("height").toFloat()
        val typeString = json.getString("type")
        val type = Enemy.EnemyType.valueOf(typeString)

        return Enemy(x, y, width, height, type).apply {
            // Additional enemy configuration can be added here
        }
    }

    private fun parseCollectible(json: JSONObject): Collectible {
        val x = json.getDouble("x").toFloat()
        val y = json.getDouble("y").toFloat()
        val width = json.getDouble("width").toFloat()
        val height = json.getDouble("height").toFloat()
        val typeString = json.getString("type")
        val type = Collectible.Type.valueOf(typeString)

        return Collectible(x, y, width, height, type)
    }

    // Validate level file existence
    fun levelExists(levelId: Int): Boolean {
        return try {
            val fileName = "$LEVEL_FILE_PREFIX$levelId$LEVEL_FILE_EXTENSION"
            context.assets.open("$LEVELS_DIR/$fileName").close()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get total number of available levels
    fun getTotalLevels(): Int {
        return try {
            context.assets.list(LEVELS_DIR)?.count {
                it.startsWith(LEVEL_FILE_PREFIX) && it.endsWith(LEVEL_FILE_EXTENSION)
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    // Example level file format for reference
    fun getLevelFileFormat(): String {
        return """
        {
            "name": "Level 1",
            "playerStartX": 100.0,
            "playerStartY": 100.0,
            "width": 1920.0,
            "height": 1080.0,
            "backgroundColor": "#000000",
            "backgroundMusic": "level1.mp3",
            "gravity": 1000.0,
            "platforms": [
                {
                    "x": 0.0,
                    "y": 500.0,
                    "width": 200.0,
                    "height": 40.0,
                    "type": "SOLID"
                }
            ],
            "enemies": [
                {
                    "x": 300.0,
                    "y": 400.0,
                    "width": 50.0,
                    "height": 50.0,
                    "type": "WALKER"
                }
            ],
            "collectibles": [
                {
                    "x": 150.0,
                    "y": 450.0,
                    "width": 30.0,
                    "height": 30.0,
                    "type": "TROPHY"
                }
            ]
        }
        """.trimIndent()
    }
}