package com.example.dangerousdave.entities

import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.dangerousdave.core.GameObject
import com.example.dangerousdave.utils.TextureManager

class Platform(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    val type: PlatformType
) : GameObject(x, y, width, height) {

    // Enums and Constants
    enum class PlatformType {
        SOLID,      // Basic static platform
        MOVING,     // Moves horizontally or vertically
        HAZARD,     // Damages player on contact
        BREAKABLE,  // Breaks after player contact
        PASSTHROUGH // Can jump up through it
    }

    companion object {
        // Time constants
        private const val BREAK_TIME = 0.5f    // Time to break platform
        private const val RESPAWN_TIME = 3.0f  // Time to respawn platform

        // Movement constants
        private const val DEFAULT_MOVE_DISTANCE = 200f
        private const val DEFAULT_MOVE_SPEED = 100f

        // Damage constants
        private const val HAZARD_DAMAGE = 1
    }

    // State Properties
    private var isMoving = false
    private var isBreaking = false
    private var isPassedThrough = false

    // Position Properties
    private val startPosition = Position(x, y)
    private var currentMovementDistance = DEFAULT_MOVE_DISTANCE
    private var currentMovementSpeed = DEFAULT_MOVE_SPEED
    private var movementDirection = MovementDirection.HORIZONTAL

    // Timers
    private var breakTimer = 0f
    private var respawnTimer = 0f

    // Data Classes
    private data class Position(var x: Float, var y: Float)

    private enum class MovementDirection {
        HORIZONTAL, VERTICAL
    }

    init {
        // First set up basic properties
        isCollidable = true
        velocityX = 0f
        velocityY = 0f

        // Load appropriate sprites based on platform type
        val platformSprites = when (type) {
            PlatformType.SOLID -> listOf(TextureManager.getPlatformTexture("solid"))
            PlatformType.MOVING -> listOf(TextureManager.getPlatformTexture("moving"))
            PlatformType.HAZARD -> listOf(TextureManager.getPlatformTexture("hazard"))
            PlatformType.BREAKABLE -> TextureManager.getPlatformTextures("breakable")
            PlatformType.PASSTHROUGH -> listOf(TextureManager.getPlatformTexture("passthrough"))
        }.filterNotNull()

        updateSprites(platformSprites)

        // Then set up type-specific behavior
        when (type) {
            PlatformType.MOVING -> {
                isMoving = true
                setupMovingPlatform()
            }
            PlatformType.BREAKABLE -> {
                setupBreakablePlatform()
            }
            PlatformType.PASSTHROUGH -> {
                setupPassthroughPlatform()
            }
            else -> {
                // SOLID and HAZARD already have default setup
            }
        }
    }

    private fun setupMovingPlatform() {
        maxVelocityX = currentMovementSpeed
        maxVelocityY = currentMovementSpeed
        velocityX = currentMovementSpeed
    }

    private fun setupBreakablePlatform() {
        velocityX = 0f
        velocityY = 0f
    }

    private fun setupPassthroughPlatform() {
        velocityX = 0f
        velocityY = 0f
    }

    override fun update(deltaTime: Float) {
        when {
            isMoving -> updateMovement(deltaTime)
            isBreaking -> updateBreaking(deltaTime)
        }
        super.update(deltaTime)
    }

    private fun updateMovement(deltaTime: Float) {
        when (movementDirection) {
            MovementDirection.HORIZONTAL -> updateHorizontalMovement()
            MovementDirection.VERTICAL -> updateVerticalMovement()
        }
        updateBounds()
    }

    private fun updateHorizontalMovement() {
        if (x >= startPosition.x + currentMovementDistance) {
            velocityX = -currentMovementSpeed
        } else if (x <= startPosition.x) {
            velocityX = currentMovementSpeed
        }
    }

    private fun updateVerticalMovement() {
        if (y >= startPosition.y + currentMovementDistance) {
            velocityY = -currentMovementSpeed
        } else if (y <= startPosition.y) {
            velocityY = currentMovementSpeed
        }
    }

    private fun updateBreaking(deltaTime: Float) {
        breakTimer -= deltaTime
        if (breakTimer <= 0f) {
            breakPlatform()
        }
    }

    override fun draw(canvas: Canvas) {
        if (!isVisible) return

        getSprites().getOrNull(currentFrame)?.let { sprite ->
            val currentAlpha = if (isBreaking) {
                ((breakTimer / BREAK_TIME) * 255).toInt()
            } else {
                255
            }

            paint.alpha = currentAlpha
            canvas.drawBitmap(sprite, x, y, paint)
            paint.alpha = 255
        }
    }

    override fun onCollision(other: GameObject) {
        if (other !is Player) return

        when (type) {
            PlatformType.HAZARD -> handleHazardCollision(other)
            PlatformType.BREAKABLE -> handleBreakableCollision()
            PlatformType.PASSTHROUGH -> handlePassthroughCollision(other)
            else -> {} // Normal collision handling by physics system
        }
    }

    private fun handleHazardCollision(player: Player) {
        player.onCollision(this)
    }

    private fun handleBreakableCollision() {
        if (!isBreaking) {
            startBreaking()
        }
    }

    private fun handlePassthroughCollision(player: Player) {
        isPassedThrough = player.velocityY > 0 &&
                player.y + player.height > y
    }

    private fun startBreaking() {
        isBreaking = true
        breakTimer = BREAK_TIME
    }

    private fun breakPlatform() {
        isCollidable = false
        isVisible = false
        isBreaking = false
    }

    // Public Configuration Methods
    fun setMovementDistance(distance: Float) {
        currentMovementDistance = distance
    }

    fun setMovementSpeed(speed: Float) {
        currentMovementSpeed = speed
        maxVelocityX = speed
        maxVelocityY = speed
    }

    fun setVerticalMovement(vertical: Boolean) {
        movementDirection = if (vertical) {
            MovementDirection.VERTICAL
        } else {
            MovementDirection.HORIZONTAL
        }

        velocityX = 0f
        velocityY = if (vertical) currentMovementSpeed else 0f
    }

    // Reset
    override fun reset() {
        super.reset()

        // Reset position
        x = startPosition.x
        y = startPosition.y

        // Reset state
        isBreaking = false
        isPassedThrough = false
        breakTimer = 0f
        respawnTimer = 0f

        // Reset type-specific properties
        isCollidable = true

        when (type) {
            PlatformType.MOVING -> {
                isMoving = true
                maxVelocityX = currentMovementSpeed
                maxVelocityY = currentMovementSpeed
                if (movementDirection == MovementDirection.VERTICAL) {
                    velocityX = 0f
                    velocityY = currentMovementSpeed
                } else {
                    velocityX = currentMovementSpeed
                    velocityY = 0f
                }
            }
            PlatformType.BREAKABLE -> {
                velocityX = 0f
                velocityY = 0f
            }
            PlatformType.PASSTHROUGH -> {
                velocityX = 0f
                velocityY = 0f
            }
            else -> {
                // SOLID and HAZARD
                velocityX = 0f
                velocityY = 0f
            }
        }
    }

    // Public Queries
    fun isBreakable(): Boolean = type == PlatformType.BREAKABLE
    fun isHazard(): Boolean = type == PlatformType.HAZARD
    fun isPassthrough(): Boolean = type == PlatformType.PASSTHROUGH
    fun getDamage(): Int = if (isHazard()) HAZARD_DAMAGE else 0

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
        return when (type) {
            PlatformType.SOLID -> Color.GRAY
            PlatformType.MOVING -> Color.GREEN
            PlatformType.HAZARD -> Color.RED
            PlatformType.BREAKABLE -> Color.YELLOW
            PlatformType.PASSTHROUGH -> Color.BLUE
        }
    }
}