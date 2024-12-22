package com.example.dangerousdave.entities

import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Bitmap
import android.util.Log
import com.example.dangerousdave.core.GameObject

class Projectile(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    val fromPlayer: Boolean
) : GameObject(x, y, width, height) {

    // Projectile properties
    private var damage: Int = 1
    private var lifetime: Float = 3.0f  // Seconds until despawn
    private var hasHit: Boolean = false

    companion object {
        const val PROJECTILE_SPEED = 400f  // Pixels per second
        const val PLAYER_DAMAGE = 2        // Damage dealt by player projectiles
        const val ENEMY_DAMAGE = 1         // Damage dealt by enemy projectiles
    }

    init {
        // Set initial properties
        isCollidable = true
        damage = if (fromPlayer) PLAYER_DAMAGE else ENEMY_DAMAGE
        maxVelocityX = PROJECTILE_SPEED
        maxVelocityY = PROJECTILE_SPEED
    }

    fun shoot(direction: Direction) {
        when (direction) {
            Direction.LEFT -> {
                velocityX = -PROJECTILE_SPEED
                velocityY = 0f
            }
            Direction.RIGHT -> {
                velocityX = PROJECTILE_SPEED
                velocityY = 0f
            }
            Direction.UP -> {
                velocityX = 0f
                velocityY = -PROJECTILE_SPEED
            }
            Direction.DOWN -> {
                velocityX = 0f
                velocityY = PROJECTILE_SPEED
            }
        }
        facing = direction
    }

    override fun update(deltaTime: Float) {
        if (!isActive || hasHit) return

        // Update lifetime
        lifetime -= deltaTime
        if (lifetime <= 0f) {
            kill()
            return
        }

        super.update(deltaTime)
    }

    override fun draw(canvas: Canvas) {
        if (!isVisible || hasHit) return

        spriteList.getOrNull(currentFrame)?.let { sprite: Bitmap ->
            val matrix = android.graphics.Matrix()

            // Rotate sprite based on direction
            val rotation = when (facing) {
                Direction.LEFT -> 180f
                Direction.UP -> 270f
                Direction.DOWN -> 90f
                Direction.RIGHT -> 0f
            }

            matrix.setRotate(rotation, width / 2, height / 2)
            matrix.postTranslate(x, y)
            canvas.drawBitmap(sprite, matrix, paint)
        }
    }

    override fun onCollision(other: GameObject) {
        if (hasHit) return

        when (other) {
            is Player -> {
                if (!fromPlayer) {
                    handleHit()
                }
            }
            is Enemy -> {
                if (fromPlayer) {
                    handleHit()
                }
            }
            is Platform -> {
                if (other.type != Platform.PlatformType.PASSTHROUGH) {
                    handleHit()
                }
            }
        }
    }

    private fun handleHit() {
        hasHit = true
        isCollidable = false
        // Spawn hit effect
        kill()
    }

    override fun reset() {
        super.reset()
        hasHit = false
        isCollidable = true
        lifetime = 3.0f
        velocityX = 0f
        velocityY = 0f
    }

    // Getters
    fun getDamage(): Int = damage
    fun isFromPlayer(): Boolean = fromPlayer

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
        return Color.WHITE
    }
}