package com.example.dangerousdave.utils


import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object TextureManager {
    // Texture storage
    private val textures = mutableMapOf<String, Bitmap>()

    // Texture paths
    private object TexturePaths {
        const val PLAYER = "textures/player/"
        const val ENEMIES = "textures/enemies/"
        const val PLATFORMS = "textures/platforms/"
        const val COLLECTIBLES = "textures/collectibles/"
        const val UI = "textures/ui/"
    }

    fun loadTextures(assets: AssetManager) {
        try {
            // Load player textures
            loadTextureSet(assets, TexturePaths.PLAYER, "player")

            // Load enemy textures
            loadTextureSet(assets, TexturePaths.ENEMIES, "enemy")

            // Load platform textures
            loadTextureSet(assets, TexturePaths.PLATFORMS, "platform")

            // Load collectible textures
            loadTextureSet(assets, TexturePaths.COLLECTIBLES, "collectible")

            // Load UI textures
            loadTextureSet(assets, TexturePaths.UI, "ui")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getEnemyTextures(enemyType: String): List<Bitmap> {
        val texturesList = mutableListOf<Bitmap>()
        // Assuming enemy textures are named like "walker_1", "walker_2", etc.
        for (i in 1..4) {  // Assuming 4 frames per animation
            getEnemyTexture("${enemyType.toLowerCase()}_$i")?.let { bitmap ->
                texturesList.add(bitmap)
            }
        }
        return texturesList
    }
    // Add this to TextureManager
    fun getPlatformTextures(platformType: String): List<Bitmap> {
        val texturesList = mutableListOf<Bitmap>()
        // For breakable platforms that might have multiple frames
        for (i in 1..4) {  // Assuming maximum 4 frames for breaking animation
            getPlatformTexture("${platformType.toLowerCase()}_$i")?.let { bitmap ->
                texturesList.add(bitmap)
            }
        }
        return texturesList
    }

    private fun loadTextureSet(assets: AssetManager, path: String, prefix: String) {
        try {
            assets.list(path)?.forEach { fileName ->
                val fullPath = "$path$fileName"
                val textureName = "$prefix/${fileName.substringBeforeLast('.')}"
                assets.open(fullPath).use { inputStream ->
                    textures[textureName] = BitmapFactory.decodeStream(inputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTexture(name: String): Bitmap? = textures[name]

    fun getPlayerTexture(state: String): Bitmap? =
        textures["player/$state"]

    fun getEnemyTexture(type: String): Bitmap? =
        textures["enemy/$type"]

    fun getPlatformTexture(type: String): Bitmap? =
        textures["platform/$type"]

    fun getCollectibleTexture(type: String): Bitmap? =
        textures["collectible/$type"]

    fun getUITexture(name: String): Bitmap? =
        textures["ui/$name"]

    // Clean up resources
    fun dispose() {
        textures.values.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        textures.clear()
    }
}