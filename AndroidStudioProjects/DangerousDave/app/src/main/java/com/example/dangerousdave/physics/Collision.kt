package com.example.dangerousdave.physics

import com.example.dangerousdave.core.GameObject
import com.example.dangerousdave.entities.Platform
import android.graphics.RectF

class Collision {

    companion object {
        const val PENETRATION_TOLERANCE = 0.01f
        const val COLLISION_BOUNCE = 0.2f
    }

    data class CollisionResult(
        val hasCollision: Boolean = false,
        val penetrationX: Float = 0f,
        val penetrationY: Float = 0f,
        val normal: Pair<Float, Float> = Pair(0f, 0f)
    )

    fun checkCollision(obj1: GameObject, obj2: GameObject): CollisionResult {
        if (!obj1.isCollidable || !obj2.isCollidable) {
            return CollisionResult()
        }

        val overlap = RectF()
        if (!RectF.intersects(obj1.bounds, obj2.bounds)) {
            return CollisionResult()
        }

        overlap.setIntersect(obj1.bounds, obj2.bounds)

        val penetrationX = overlap.width()
        val penetrationY = overlap.height()

        val centerDiffX = obj2.getCenterX() - obj1.getCenterX()
        val centerDiffY = obj2.getCenterY() - obj1.getCenterY()

        val normal = if (penetrationX < penetrationY) {
            Pair(if (centerDiffX > 0) 1f else -1f, 0f)
        } else {
            Pair(0f, if (centerDiffY > 0) 1f else -1f)
        }

        return CollisionResult(true, penetrationX, penetrationY, normal)
    }

    fun resolveCollision(obj1: GameObject, obj2: GameObject, result: CollisionResult) {
        if (!result.hasCollision) return

        when (obj2) {
            is Platform -> resolvePlatformCollision(obj1, obj2, result)
            else -> resolveGeneralCollision(obj1, obj2, result)
        }
    }

    private fun resolvePlatformCollision(obj: GameObject, platform: Platform, result: CollisionResult) {
        val (normalX, normalY) = result.normal

        when (platform.type) {
            Platform.PlatformType.SOLID -> {
                if (Math.abs(normalX) > 0) {
                    obj.x += result.penetrationX * normalX
                    obj.velocityX = 0f
                } else {
                    obj.y += result.penetrationY * normalY
                    obj.velocityY = 0f
                }
            }
            Platform.PlatformType.PASSTHROUGH -> {
                if (normalY > 0 && obj.velocityY > 0) {
                    obj.y -= result.penetrationY
                    obj.velocityY = 0f
                }
            }
            Platform.PlatformType.BREAKABLE -> {
                if (Math.abs(normalX) > 0) {
                    obj.x += result.penetrationX * normalX
                    obj.velocityX = 0f
                } else {
                    obj.y += result.penetrationY * normalY
                    obj.velocityY = 0f
                }
            }
            else -> {}
        }

        obj.updateBounds()
    }

    private fun resolveGeneralCollision(obj1: GameObject, obj2: GameObject, result: CollisionResult) {
        val (normalX, normalY) = result.normal
        val massRatio1 = 0.5f
        val massRatio2 = 0.5f

        if (Math.abs(normalX) > 0) {
            obj1.x -= result.penetrationX * normalX * massRatio1
            obj2.x += result.penetrationX * normalX * massRatio2
        } else {
            obj1.y -= result.penetrationY * normalY * massRatio1
            obj2.y += result.penetrationY * normalY * massRatio2
        }

        obj1.updateBounds()
        obj2.updateBounds()
    }

    fun pointInObject(x: Float, y: Float, obj: GameObject): Boolean {
        return obj.bounds.contains(x, y)
    }

    fun getCollisionNormal(obj1: GameObject, obj2: GameObject): Pair<Float, Float> {
        val centerDiffX = obj2.getCenterX() - obj1.getCenterX()
        val centerDiffY = obj2.getCenterY() - obj1.getCenterY()

        return if (Math.abs(centerDiffX) > Math.abs(centerDiffY)) {
            Pair(if (centerDiffX > 0) 1f else -1f, 0f)
        } else {
            Pair(0f, if (centerDiffY > 0) 1f else -1f)
        }
    }

    fun calculateCollisionResponse(
        velocity: Float,
        normal: Float,
        restitution: Float = COLLISION_BOUNCE
    ): Float {
        return -velocity * restitution
    }

    fun isGrounded(obj: GameObject, ground: GameObject): Boolean {
        val rayRect = RectF(
            obj.x,
            obj.y + obj.height,
            obj.x + obj.width,
            obj.y + obj.height + 2f
        )

        return RectF.intersects(rayRect, ground.bounds)
    }

    fun sweepTest(
        obj: GameObject,
        velocityX: Float,
        velocityY: Float,
        obstacles: List<GameObject>
    ): CollisionResult? {
        val sweptBounds = RectF(
            if (velocityX > 0) obj.x else obj.x + velocityX,
            if (velocityY > 0) obj.y else obj.y + velocityY,
            if (velocityX > 0) obj.x + obj.width + velocityX else obj.x + obj.width,
            if (velocityY > 0) obj.y + obj.height + velocityY else obj.y + obj.height
        )

        for (obstacle in obstacles) {
            if (RectF.intersects(sweptBounds, obstacle.bounds)) {
                return checkCollision(obj, obstacle)
            }
        }

        return null
    }
}