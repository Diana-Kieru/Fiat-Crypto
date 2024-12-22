package com.example.dangerousdave.entities

import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import com.example.dangerousdave.core.GameObject
import com.example.dangerousdave.utils.TextureManager

class Enemy(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    val type: EnemyType
) : GameObject(x, y, width, height) {

    // Enemy types with their specific behaviors
    enum class EnemyType {
        WALKER,     // Moves left and right on platforms
        FLYER,      // Moves in a flying pattern
        SHOOTER,    // Stationary enemy that shoots
        JUMPER      // Jumps periodically
    }

    // Enemy-specific properties
    private var points: Int = 100              // Points awarded when killed
    private var damage: Int = 1                // Damage dealt to player
    private var patrolDistance: Float = 200f   // Distance to patrol for walking enemies
    private var startX: Float = x              // Starting X position for patrol
    private var startY: Float = y              // Starting Y position for flying enemies
    private var shootTimer: Float = 0f         // Timer for shooting enemies
    private var jumpTimer: Float = 0f          // Timer for jumping enemies
    private val matrix = Matrix()              // Reusable matrix for drawing

    // Property to track if enemy is on ground
    private var isOnGround: Boolean = false
        set(value) {
            field = value
            if (value) velocityY = 0f
        }

    companion object {
        // Movement constants
        const val WALK_SPEED = 100f            // Walking speed
        const val FLY_SPEED = 150f             // Flying speed
        const val JUMP_VELOCITY = -300f        // Jump strength
        const val SHOOT_INTERVAL = 2f          // Seconds between shots
        const val JUMP_INTERVAL = 3f           // Seconds between jumps
        const val MAX_FALL_SPEED = 400f        // Maximum falling speed
    }

    init {
        // Load sprites based on enemy type
        val enemySprites = TextureManager.getEnemyTextures(type.name.toLowerCase())
        updateSprites(enemySprites)

        // Set enemy-specific properties based on type
        when (type) {
            EnemyType.WALKER -> {
                maxVelocityX = WALK_SPEED
                maxVelocityY = MAX_FALL_SPEED
                accelerationY = 1000f  // Gravity
                velocityX = WALK_SPEED
                facing = Direction.RIGHT
            }
            EnemyType.FLYER -> {
                maxVelocityX = FLY_SPEED
                maxVelocityY = FLY_SPEED
                accelerationY = 0f     // No gravity for flyers
            }
            EnemyType.SHOOTER -> {
                maxVelocityX = 0f
                maxVelocityY = MAX_FALL_SPEED
                accelerationY = 1000f  // Gravity
            }
            EnemyType.JUMPER -> {
                maxVelocityX = WALK_SPEED
                maxVelocityY = MAX_FALL_SPEED
                accelerationY = 1000f  // Gravity
            }
        }
    }

    override fun update(deltaTime: Float) {
        if (!isActive || checkIsDead()) return

        // Update behavior based on type
        when (type) {
            EnemyType.WALKER -> updateWalker(deltaTime)
            EnemyType.FLYER -> updateFlyer(deltaTime)
            EnemyType.SHOOTER -> updateShooter(deltaTime)
            EnemyType.JUMPER -> updateJumper(deltaTime)
        }

        super.update(deltaTime)
    }

    private fun updateWalker(deltaTime: Float) {
        // Patrol back and forth
        if (x > startX + patrolDistance) {
            velocityX = -WALK_SPEED
            facing = Direction.LEFT
        } else if (x < startX) {
            velocityX = WALK_SPEED
            facing = Direction.RIGHT
        }
    }

    private fun updateFlyer(deltaTime: Float) {
        // Circular or wave pattern movement
        val time = System.currentTimeMillis() / 1000f
        x = startX + Math.sin((time * 2).toDouble()).toFloat() * 100f
        y = startY + Math.cos((time * 2).toDouble()).toFloat() * 50f
        updateBounds()
    }

    private fun updateShooter(deltaTime: Float) {
        shootTimer -= deltaTime
        if (shootTimer <= 0f) {
            shoot()
            shootTimer = SHOOT_INTERVAL
        }
    }

    private fun updateJumper(deltaTime: Float) {
        if (isOnGround) {
            jumpTimer -= deltaTime
            if (jumpTimer <= 0f) {
                jump()
                jumpTimer = JUMP_INTERVAL
            }
        }
    }

    override fun draw(canvas: Canvas) {
        if (!isVisible) return

        getSprites().getOrNull(currentFrame)?.let { sprite ->
            matrix.reset()
            if (facing == Direction.LEFT) {
                matrix.setScale(-1f, 1f, width / 2, height / 2)
                matrix.postTranslate(x, y)
            } else {
                matrix.setTranslate(x, y)
            }
            canvas.drawBitmap(sprite, matrix, paint)
        }
    }

    override fun onCollision(other: GameObject) {
        when (other) {
            is Platform -> handlePlatformCollision(other)
            is Player -> handlePlayerCollision(other)
            is Projectile -> handleProjectileCollision(other)
        }
    }

    private fun handlePlatformCollision(platform: Platform) {
        val overlapX = (getCenterX() - platform.getCenterX()) / (width + platform.width)
        val overlapY = (getCenterY() - platform.getCenterY()) / (height + platform.height)

        if (Math.abs(overlapX) > Math.abs(overlapY)) {
            // Horizontal collision - reverse direction
            if (overlapX > 0) {
                x = platform.x + platform.width
                if (type == EnemyType.WALKER) {
                    velocityX = WALK_SPEED
                    facing = Direction.RIGHT
                }
            } else {
                x = platform.x - width
                if (type == EnemyType.WALKER) {
                    velocityX = -WALK_SPEED
                    facing = Direction.LEFT
                }
            }
            velocityX = 0f
        } else {
            // Vertical collision
            if (overlapY > 0) {
                // Hitting platform from below
                y = platform.y + platform.height
                velocityY = 0f
            } else {
                // Landing on platform
                y = platform.y - height
                velocityY = 0f
                isOnGround = true
            }
        }
        updateBounds()
    }

    private fun handlePlayerCollision(player: Player) {
        // Player handles the collision effects
    }

    private fun handleProjectileCollision(projectile: Projectile) {
        if (projectile.fromPlayer) {
            kill()
        }
    }

    private fun shoot() {
        // ProjectileManager will handle projectile creation
    }

    private fun jump() {
        if (isOnGround) {
            velocityY = JUMP_VELOCITY
            isOnGround = false
        }
    }

    override fun kill() {
        super.kill()
        isCollidable = false
        // Death animation would be handled by the sprite system
    }

    override fun reset() {
        super.reset()

        // Reset position
        x = startX
        y = startY

        // Reset type-specific properties
        when (type) {
            EnemyType.WALKER -> {
                velocityX = WALK_SPEED
                facing = Direction.RIGHT
            }
            EnemyType.SHOOTER -> shootTimer = 0f
            EnemyType.JUMPER -> jumpTimer = 0f
            else -> {}
        }

        // Reset state
        isOnGround = false
    }

    // Getters
    fun getPoints(): Int = points
    fun getDamage(): Int = damage

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
        return Color.RED
    }
}