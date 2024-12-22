package com.example.dangerousdave.physics


import com.example.dangerousdave.core.GameObject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Movement {
    companion object {
        // Physics constants
        const val DEFAULT_GRAVITY = 1000f         // Pixels per second squared
        const val DEFAULT_MAX_FALL_SPEED = 800f   // Maximum falling speed
        const val DEFAULT_FRICTION = 800f         // Ground friction
        const val DEFAULT_AIR_RESISTANCE = 100f   // Air resistance
        const val MINIMUM_MOVEMENT_THRESHOLD = 0.01f  // Minimum speed before stopping
    }

    // Movement update with physics
    fun updateMovement(
        gameObject: GameObject,
        deltaTime: Float,
        gravity: Float = DEFAULT_GRAVITY,
        isOnGround: Boolean = false
    ) {
        // Apply gravity if not on ground
        if (!isOnGround) {
            applyGravity(gameObject, deltaTime, gravity)
        }

        // Apply friction/air resistance
        applyFriction(gameObject, deltaTime, isOnGround)

        // Update position based on velocity
        updatePosition(gameObject, deltaTime)

        // Clamp velocities to maximum values
        clampVelocities(gameObject)
    }

    // Apply gravity force
    private fun applyGravity(
        gameObject: GameObject,
        deltaTime: Float,
        gravity: Float
    ) {
        gameObject.velocityY += gravity * deltaTime

        // Clamp falling speed
        if (gameObject.velocityY > DEFAULT_MAX_FALL_SPEED) {
            gameObject.velocityY = DEFAULT_MAX_FALL_SPEED
        }
    }

    // Apply friction and air resistance
    private fun applyFriction(
        gameObject: GameObject,
        deltaTime: Float,
        isOnGround: Boolean
    ) {
        val frictionAmount = if (isOnGround) DEFAULT_FRICTION else DEFAULT_AIR_RESISTANCE

        // Apply horizontal friction
        when {
            gameObject.velocityX > 0 -> {
                gameObject.velocityX = max(0f,
                    gameObject.velocityX - frictionAmount * deltaTime)
            }
            gameObject.velocityX < 0 -> {
                gameObject.velocityX = min(0f,
                    gameObject.velocityX + frictionAmount * deltaTime)
            }
        }

        // Stop very small movements
        if (abs(gameObject.velocityX) < MINIMUM_MOVEMENT_THRESHOLD) {
            gameObject.velocityX = 0f
        }
    }

    // Update position based on velocity
    private fun updatePosition(gameObject: GameObject, deltaTime: Float) {
        // Update position with current velocity
        gameObject.x += gameObject.velocityX * deltaTime
        gameObject.y += gameObject.velocityY * deltaTime

        // Update object bounds
        gameObject.updateBounds()
    }

    // Clamp velocities to maximum values
    private fun clampVelocities(gameObject: GameObject) {
        gameObject.velocityX = gameObject.velocityX.coerceIn(
            -gameObject.maxVelocityX,
            gameObject.maxVelocityX
        )

        gameObject.velocityY = gameObject.velocityY.coerceIn(
            -gameObject.maxVelocityY,
            gameObject.maxVelocityY
        )
    }

    // Utility methods for specific movement types

    // Jump movement
    fun jump(gameObject: GameObject, jumpForce: Float) {
        gameObject.velocityY = -jumpForce  // Negative because y increases downward
    }

    // Horizontal movement
    fun moveHorizontal(gameObject: GameObject, speed: Float) {
        gameObject.velocityX = speed
    }

    // Stop movement
    fun stop(gameObject: GameObject) {
        gameObject.velocityX = 0f
    }

    // Bounce response (for collisions)
    fun bounce(
        gameObject: GameObject,
        bounceForce: Float,
        horizontalBounce: Boolean = false
    ) {
        if (horizontalBounce) {
            gameObject.velocityX *= -bounceForce
        } else {
            gameObject.velocityY *= -bounceForce
        }
    }

    // Check if object is moving
    fun isMoving(gameObject: GameObject): Boolean {
        return abs(gameObject.velocityX) > MINIMUM_MOVEMENT_THRESHOLD ||
                abs(gameObject.velocityY) > MINIMUM_MOVEMENT_THRESHOLD
    }

    // Get movement direction
    fun getMovementDirection(gameObject: GameObject): GameObject.Direction {
        return when {
            gameObject.velocityX > 0 -> GameObject.Direction.RIGHT
            gameObject.velocityX < 0 -> GameObject.Direction.LEFT
            gameObject.velocityY > 0 -> GameObject.Direction.DOWN
            gameObject.velocityY < 0 -> GameObject.Direction.UP
            else -> gameObject.facing  // Keep current facing if not moving
        }
    }

    // Calculate movement speed
    fun getSpeed(gameObject: GameObject): Float {
        return kotlin.math.sqrt(
            gameObject.velocityX * gameObject.velocityX +
                    gameObject.velocityY * gameObject.velocityY
        )
    }

    // Helper method for smooth acceleration
    fun accelerate(
        gameObject: GameObject,
        targetSpeed: Float,
        acceleration: Float,
        deltaTime: Float,
        horizontal: Boolean = true
    ) {
        if (horizontal) {
            val currentSpeed = gameObject.velocityX
            val speedDiff = targetSpeed - currentSpeed
            val accelerationThisFrame = acceleration * deltaTime

            gameObject.velocityX += speedDiff.coerceIn(
                -accelerationThisFrame,
                accelerationThisFrame
            )
        } else {
            val currentSpeed = gameObject.velocityY
            val speedDiff = targetSpeed - currentSpeed
            val accelerationThisFrame = acceleration * deltaTime

            gameObject.velocityY += speedDiff.coerceIn(
                -accelerationThisFrame,
                accelerationThisFrame
            )
        }
    }
}