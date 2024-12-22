package com.example.dangerousdave.entities

import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.dangerousdave.core.GameObject

class Collectible(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    val type: Type
) : GameObject(x, y, width, height) {

    // Collectible types
    enum class Type {
        TROPHY,         // Main collectible for points
        GUN,            // Weapon power-up
        EXTRA_LIFE,     // Additional life
        JETPACK,        // Allows flying
        KEY,           // Opens doors
        CROWN          // Special high-value collectible
    }

    // Collectible properties
    private var points: Int = 0
    private var isCollected: Boolean = false
    private var collectionAnimationTimer: Float = 0f
    private var floatOffset: Float = 0f        // For floating animation
    private var originalY: Float = y           // Store original Y for floating animation

    companion object {
        const val COLLECTION_ANIMATION_TIME = 0.5f  // Time for collection animation
        const val FLOAT_AMPLITUDE = 10f            // Pixels to float up/down
        const val FLOAT_SPEED = 2f                 // Float cycle speed
        const val SPIN_SPEED = 0.2f                // Frame duration for spin animation

        // Points values for different types
        private val TYPE_POINTS = mapOf(
            Type.TROPHY to 100,
            Type.GUN to 250,
            Type.EXTRA_LIFE to 1000,
            Type.JETPACK to 500,
            Type.KEY to 150,
            Type.CROWN to 5000
        )
    }

    init {
        // Set type-specific properties
        points = TYPE_POINTS[type] ?: 0
        isCollidable = true
        isVisible = true
        frameDuration = SPIN_SPEED

        // Some collectibles don't spin
        when (type) {
            Type.GUN, Type.JETPACK -> frameDuration = Float.POSITIVE_INFINITY
            else -> {}
        }
    }

    override fun update(deltaTime: Float) {
        if (isCollected) {
            updateCollectionAnimation(deltaTime)
        } else {
            updateFloatingAnimation(deltaTime)
        }

        super.update(deltaTime)
    }

    private fun updateFloatingAnimation(deltaTime: Float) {
        // Create a smooth floating motion
        val time = System.currentTimeMillis() / 1000f
        floatOffset = Math.sin(time * FLOAT_SPEED * Math.PI).toFloat() * FLOAT_AMPLITUDE
        y = originalY + floatOffset
        updateBounds()
    }

    private fun updateCollectionAnimation(deltaTime: Float) {
        collectionAnimationTimer -= deltaTime
        if (collectionAnimationTimer <= 0f) {
            isVisible = false
            isCollidable = false
        } else {
            // Shrink and fade during collection
            val progress = collectionAnimationTimer / COLLECTION_ANIMATION_TIME
            paint.alpha = (255 * progress).toInt()

            // Move upward while being collected
            y -= deltaTime * 100f
            updateBounds()
        }
    }

    override fun draw(canvas: Canvas) {
        if (!isVisible) return

        getSprites().getOrNull(currentFrame)?.let { sprite ->  // Changed from sprites to getSprites()
            // Apply collection animation effects
            if (isCollected) {
                val scale = collectionAnimationTimer / COLLECTION_ANIMATION_TIME
                val matrix = android.graphics.Matrix()
                matrix.setScale(scale, scale, width / 2, height / 2)
                matrix.postTranslate(x, y)
                canvas.drawBitmap(sprite, matrix, paint)
            } else {
                canvas.drawBitmap(sprite, x, y, paint)
            }
        }
    }

    override fun onCollision(other: GameObject) {
        if (other is Player && !isCollected) {
            collect()
        }
    }

    // Called when the player collects this item
    fun collect() {
        if (!isCollected) {
            isCollected = true
            collectionAnimationTimer = COLLECTION_ANIMATION_TIME
            // Play collection sound effect here
        }
    }

    // Effects when collected
    fun applyEffect(player: Player) {
        when (type) {
            Type.TROPHY -> player.addScore(points)
            Type.GUN -> {
                player.addScore(points)
                player.acquireGun()
            }
            Type.EXTRA_LIFE -> {
                player.addScore(points)
                player.addLife()
            }
            Type.JETPACK -> {
                player.addScore(points)
                player.acquireJetpack()
            }
            Type.KEY -> {
                player.addScore(points)
                player.acquireKey()
            }
            Type.CROWN -> {
                player.addScore(points)
                player.completeLevelBonus()
            }
        }
    }

    // Override reset for reuse
    override fun reset() {
        super.reset()
        isCollected = false
        isVisible = true
        isCollidable = true
        collectionAnimationTimer = 0f
        y = originalY
        paint.alpha = 255
    }

    // Getters
    fun getPoints(): Int = points
    fun isCollected(): Boolean = isCollected

    // Helper method to spawn collection effect
    private fun spawnCollectionEffect() {
        // Create particle effect or spawn score popup
        // This would be implemented by the particle system
    }

    // Render method
    fun render(canvas: Canvas) {
        try {
            val paint = Paint().apply {
                color = getEntityColor() // Define appropriate colors for each entity
                style = Paint.Style.FILL
            }

            // Draw the entity
            canvas.drawRect(x, y, x + width, y + height, paint)

            Log.d(TAG, "Entity rendered at ($x, $y)")
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering entity: ${e.message}")
        }
    }

    private fun getEntityColor(): Int {
        return Color.YELLOW
    }
}