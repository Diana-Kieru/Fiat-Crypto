package com.example.dangerousdave.entities

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.dangerousdave.core.GameObject

class Player(
    x: Float,
    y: Float,
    width: Float,
    height: Float
) : GameObject(x, y, width, height) {

    // Player-specific properties
    private var lives: Int = 3
    private var score: Int = 0
    private var isJumping: Boolean = false
    private var canJump: Boolean = false
    private var hasGun: Boolean = false
    private var isInvulnerable: Boolean = false
    private var invulnerabilityTimer: Float = 0f
    private var hasJetpack: Boolean = false
    private var jetpackFuel: Float = 0f
    private var hasKey: Boolean = false
    private var levelComplete: Boolean = false

    // Movement constants
    companion object {
        const val MOVE_SPEED = 200f         // Pixels per second
        const val JUMP_VELOCITY = -400f     // Negative because y-axis is down
        const val MAX_JUMP_TIME = 0.4f      // Maximum jump duration in seconds
        const val INVULNERABILITY_TIME = 2f // Seconds of invulnerability after hit
        const val MAX_JETPACK_FUEL = 100f
        const val JETPACK_FUEL_CONSUMPTION = 20f  // Fuel used per second
        const val JETPACK_THRUST = -300f
    }

    fun acquireJetpack() {
        hasJetpack = true
        jetpackFuel = MAX_JETPACK_FUEL
    }

    fun acquireKey() {
        hasKey = true
    }

    fun completeLevelBonus() {
        levelComplete = true
        // Additional level completion logic can be added here
    }

    fun useJetpack(deltaTime: Float) {
        if (hasJetpack && jetpackFuel > 0) {
            velocityY = JETPACK_THRUST
            jetpackFuel -= JETPACK_FUEL_CONSUMPTION * deltaTime

            if (jetpackFuel <= 0) {
                hasJetpack = false
                jetpackFuel = 0f
            }
        }
    }

    fun useKey(): Boolean {
        if (hasKey) {
            hasKey = false
            return true
        }
        return false
    }

    fun hasJetpack(): Boolean = hasJetpack
    fun getJetpackFuel(): Float = jetpackFuel
    fun hasKey(): Boolean = hasKey
    fun isLevelComplete(): Boolean = levelComplete

    // Override update to handle jetpack physics

    // Current jump state
    private var jumpTimeLeft: Float = 0f

    init {
        // Set player-specific physics properties
        maxVelocityX = MOVE_SPEED
        maxVelocityY = Math.abs(JUMP_VELOCITY) * 1.5f
        friction = 800f          // Ground friction
        accelerationY = 1000f    // Gravity
    }

    override fun update(deltaTime: Float) {
        // Update invulnerability
        if (isInvulnerable) {
            invulnerabilityTimer -= deltaTime
            if (invulnerabilityTimer <= 0) {
                isInvulnerable = false
            }
        }
        if (hasJetpack && jetpackFuel > 0) {
            // Reduce gravity effect while jetpack is active
            accelerationY *= 0.5f
        }
        // Update jump state
        if (isJumping) {
            jumpTimeLeft -= deltaTime
            if (jumpTimeLeft <= 0) {
                stopJump()
            }
        }

        // Call parent update for physics and animation
        super.update(deltaTime)
    }

    override fun draw(canvas: Canvas) {
        // Skip drawing every other frame when invulnerable for blinking effect
        if (isInvulnerable && frameTimer % 0.2f > 0.1f) {
            return
        }

        // Draw the current sprite
        spriteList.getOrNull(currentFrame)?.let { sprite: Bitmap ->
            val flipMatrix = android.graphics.Matrix()
            if (facing == Direction.LEFT) {
                flipMatrix.setScale(-1f, 1f, width / 2, height / 2)
                flipMatrix.postTranslate(x, y)
                canvas.drawBitmap(sprite, flipMatrix, paint)
            } else {
                canvas.drawBitmap(sprite, x, y, paint)
            }
        }
    }

    // Movement controls
    fun moveLeft() {
        velocityX = -MOVE_SPEED
        facing = Direction.LEFT
        currentState = State.MOVING
    }

    fun moveRight() {
        velocityX = MOVE_SPEED
        facing = Direction.RIGHT
        currentState = State.MOVING
    }

    fun stopMoving() {
        velocityX = 0f
        if (!isJumping && !isFalling()) {
            currentState = State.IDLE
        }
    }

    fun jump() {
        if (canJump && !isJumping) {
            velocityY = JUMP_VELOCITY
            isJumping = true
            canJump = false
            jumpTimeLeft = MAX_JUMP_TIME
            currentState = State.JUMPING
        }
    }

    fun stopJump() {
        isJumping = false
        jumpTimeLeft = 0f
        if (velocityY < 0) {
            velocityY = 0f
        }
    }

    // Collision handling
    override fun onCollision(other: GameObject) {
        when (other) {
            is Platform -> handlePlatformCollision(other)
            is Enemy -> handleEnemyCollision(other)
            is Collectible -> handleCollectibleCollision(other)
        }
    }

    private fun handlePlatformCollision(platform: Platform) {
        // Determine collision side and adjust position
        val overlapX = (this.getCenterX() - platform.getCenterX()) / (this.width + platform.width)
        val overlapY = (this.getCenterY() - platform.getCenterY()) / (this.height + platform.height)

        if (Math.abs(overlapX) > Math.abs(overlapY)) {
            // Horizontal collision
            if (overlapX > 0) {
                x = platform.x + platform.width
            } else {
                x = platform.x - width
            }
            velocityX = 0f
        } else {
            // Vertical collision
            if (overlapY > 0) {
                // Hitting from below
                y = platform.y + platform.height
                velocityY = 0f
            } else {
                // Landing on top
                y = platform.y - height
                velocityY = 0f
                canJump = true
            }
        }
        updateBounds()
    }

    private fun handleEnemyCollision(enemy: Enemy) {
        if (!isInvulnerable) {
            takeDamage()
        }
    }

    private fun handleCollectibleCollision(collectible: Collectible) {
        when (collectible.type) {
            Collectible.Type.TROPHY -> addScore(100)
            Collectible.Type.GUN -> acquireGun()
            Collectible.Type.EXTRA_LIFE -> addLife()
            Collectible.Type.JETPACK -> {
                addScore(500)  // Bonus points for jetpack
                acquireJetpack()
            }
            Collectible.Type.KEY -> {
                addScore(150)  // Bonus points for key
                acquireKey()
            }
            Collectible.Type.CROWN -> {
                addScore(1000)  // Big bonus for crown
                completeLevelBonus()
            }
        }
        collectible.collect()
    }

    // State changes
    private fun takeDamage() {
        if (!isInvulnerable && !checkIsDead()) {
            lives--
            isInvulnerable = true
            invulnerabilityTimer = INVULNERABILITY_TIME

            if (lives <= 0) {
                kill()
            } else {
                currentState = State.HURT
            }
        }
    }

    fun addLife() {
        lives++
    }

    fun addScore(points: Int) {
        score += points
    }

    fun acquireGun() {
        hasGun = true
    }

    // Getters
    fun getLives(): Int = lives
    fun getScore(): Int = score
    fun hasGun(): Boolean = hasGun

    // Reset player state
    override fun reset() {
        super.reset()
        lives = 3
        score = 0
        hasGun = false
        isInvulnerable = false
        invulnerabilityTimer = 0f
        isJumping = false
        canJump = false
        jumpTimeLeft = 0f
        hasJetpack = false
        jetpackFuel = 0f
        hasKey = false
        levelComplete = false
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
        return Color.BLUE
    }
}