package com.example.dangerousdave.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log

abstract class GameObject(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
) {
    // Enums
    enum class Direction { LEFT, RIGHT, UP, DOWN }
    enum class State { IDLE, MOVING, JUMPING, FALLING, ATTACKING, HURT, DEAD }

    // Physics properties
    val bounds = RectF()
    var velocityX = 0f
    var velocityY = 0f
    var accelerationX = 0f
    var accelerationY = 0f
    var maxVelocityX = Float.POSITIVE_INFINITY
    var maxVelocityY = Float.POSITIVE_INFINITY
    var friction = 0f

    // Visual properties
    protected val paint = Paint()
    var spriteList = listOf<Bitmap>()  // Changed from sprites to spriteList
    protected var currentFrame = 0
    protected var frameTimer = 0f
    protected var frameDuration = 0.1f
    protected var hitboxOffset = RectF(0f, 0f, 0f, 0f)
    var facing = Direction.RIGHT

    // State properties
    var isActive = true
    var isVisible = true
    var isCollidable = true
    var currentState = State.IDLE

    init {
        updateBounds()
    }

    // Physics methods
    fun updateBounds() {
        bounds.set(x, y, x + width, y + height)
    }

    fun intersects(other: GameObject): Boolean {
        if (!isCollidable || !other.isCollidable) return false
        return RectF.intersects(bounds, other.bounds)
    }

    // Position methods
    fun setPosition(newX: Float, newY: Float) {
        x = newX
        y = newY
        updateBounds()
    }

    fun getCenterX(): Float = x + width / 2
    fun getCenterY(): Float = y + height / 2

    // Velocity methods
    fun setVelocity(newVelocityX: Float, newVelocityY: Float) {
        velocityX = newVelocityX
        velocityY = newVelocityY
    }

    fun setAcceleration(newAccelX: Float, newAccelY: Float) {
        accelerationX = newAccelX
        accelerationY = newAccelY
    }

    // Animation methods
    fun updateSprites(newSprites: List<Bitmap>) {  // Changed from setSprites to updateSprites
        spriteList = newSprites
        currentFrame = 0
        frameTimer = 0f
    }
    // Add to GameObject class

    fun updateHitbox() {
        bounds.set(
            x + hitboxOffset.left,
            y + hitboxOffset.top,
            x + width - hitboxOffset.right,
            y + height - hitboxOffset.bottom
        )
    }
    fun getSprites(): List<Bitmap> = spriteList  // Getter for sprites

    fun setAnimationSpeed(frameDurationSeconds: Float) {
        frameDuration = frameDurationSeconds
    }

    // State methods
    fun getState(): State = currentState

    protected fun setState(newState: State) {
        currentState = newState
    }

    fun isMoving(): Boolean = velocityX != 0f || velocityY != 0f
    fun isJumping(): Boolean = velocityY < 0
    fun isFalling(): Boolean = velocityY > 0
    fun checkIsDead(): Boolean = currentState == State.DEAD  // Changed from isDead to checkIsDead

    // Update methods
    private fun updatePhysics(deltaTime: Float) {
        // Update velocities with acceleration
        velocityX += accelerationX * deltaTime
        velocityY += accelerationY * deltaTime

        // Apply friction
        if (velocityX > 0) {
            velocityX = maxOf(0f, velocityX - friction * deltaTime)
        } else if (velocityX < 0) {
            velocityX = minOf(0f, velocityX + friction * deltaTime)
        }

        // Clamp velocities
        velocityX = velocityX.coerceIn(-maxVelocityX, maxVelocityX)
        velocityY = velocityY.coerceIn(-maxVelocityY, maxVelocityY)

        // Update position
        x += velocityX * deltaTime
        y += velocityY * deltaTime

        updateBounds()
    }

    private fun updateAnimation(deltaTime: Float) {
        if (spriteList.isNotEmpty()) {
            frameTimer += deltaTime
            if (frameTimer >= frameDuration) {
                frameTimer = 0f
                currentFrame = (currentFrame + 1) % spriteList.size
            }
        }
    }

    protected open fun updateState() {
        currentState = when {
            checkIsDead() -> State.DEAD
            velocityY < 0 -> State.JUMPING
            velocityY > 0 -> State.FALLING
            velocityX != 0f -> State.MOVING
            else -> State.IDLE
        }
    }

    open fun update(deltaTime: Float) {
        if (!isActive) return

        updatePhysics(deltaTime)
        updateAnimation(deltaTime)
        updateState()
    }
    // Add to GameObject class
    fun isCollisionTop(other: GameObject): Boolean {
        return bounds.bottom >= other.bounds.top &&
                bounds.top < other.bounds.top &&
                bounds.right > other.bounds.left &&
                bounds.left < other.bounds.right
    }

    fun isCollisionBottom(other: GameObject): Boolean {
        return bounds.top <= other.bounds.bottom &&
                bounds.bottom > other.bounds.bottom &&
                bounds.right > other.bounds.left &&
                bounds.left < other.bounds.right
    }

    fun isCollisionLeft(other: GameObject): Boolean {
        return bounds.right >= other.bounds.left &&
                bounds.left < other.bounds.left &&
                bounds.bottom > other.bounds.top &&
                bounds.top < other.bounds.bottom
    }

    fun isCollisionRight(other: GameObject): Boolean {
        return bounds.left <= other.bounds.right &&
                bounds.right > other.bounds.right &&
                bounds.bottom > other.bounds.top &&
                bounds.top < other.bounds.bottom
    }

    // Abstract methods
    abstract fun draw(canvas: Canvas)
    open fun onCollision(other: GameObject) {}

    // Lifecycle methods
    open fun kill() {
        setState(State.DEAD)
        isCollidable = false
    }

    open fun reset() {
        // Reset state
        isActive = true
        isVisible = true
        isCollidable = true
        setState(State.IDLE)

        // Reset physics
        velocityX = 0f
        velocityY = 0f
        accelerationX = 0f
        accelerationY = 0f

        // Reset animation
        currentFrame = 0
        frameTimer = 0f
        facing = Direction.RIGHT
    }
}