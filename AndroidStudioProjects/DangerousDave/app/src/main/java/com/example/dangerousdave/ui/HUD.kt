package com.example.dangerousdave.ui


import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class HUD {
    // HUD elements
    private val scoreBounds = RectF()
    private val livesBounds = RectF()
    private val weaponBounds = RectF()
    private val timerBounds = RectF()

    // Paint objects for drawing
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 40f
        color = android.graphics.Color.WHITE
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(128, 0, 0, 0)
    }

    companion object {
        private const val HUD_MARGIN = 0.02f        // 2% of screen width/height
        private const val HUD_ELEMENT_HEIGHT = 0.06f // 6% of screen height
        private const val HUD_ELEMENT_WIDTH = 0.2f   // 20% of screen width
    }

    fun updateDimensions(screenWidth: Int, screenHeight: Int) {
        val margin = screenWidth * HUD_MARGIN
        val elementHeight = screenHeight * HUD_ELEMENT_HEIGHT
        val elementWidth = screenWidth * HUD_ELEMENT_WIDTH

        // Score display (top left)
        scoreBounds.set(
            margin,
            margin,
            margin + elementWidth,
            margin + elementHeight
        )

        // Lives display (top right)
        livesBounds.set(
            screenWidth - margin - elementWidth,
            margin,
            screenWidth - margin,
            margin + elementHeight
        )

        // Weapon status (bottom left, above controls)
        weaponBounds.set(
            margin,
            screenHeight - margin - elementHeight * 2,
            margin + elementWidth,
            screenHeight - margin - elementHeight
        )

        // Timer (top center)
        timerBounds.set(
            screenWidth/2 - elementWidth/2,
            margin,
            screenWidth/2 + elementWidth/2,
            margin + elementHeight
        )
    }

    fun draw(canvas: Canvas, score: Int, lives: Int, hasGun: Boolean, timeLeft: Float) {
        // Draw score
        canvas.drawRect(scoreBounds, backgroundPaint)
        canvas.drawText("Score: $score", scoreBounds.left + 10, scoreBounds.bottom - 10, textPaint)

        // Draw lives
        canvas.drawRect(livesBounds, backgroundPaint)
        canvas.drawText("Lives: $lives", livesBounds.left + 10, livesBounds.bottom - 10, textPaint)

        // Draw weapon status if has gun
        if (hasGun) {
            canvas.drawRect(weaponBounds, backgroundPaint)
            canvas.drawText("Gun Active", weaponBounds.left + 10, weaponBounds.bottom - 10, textPaint)
        }

        // Draw timer
        canvas.drawRect(timerBounds, backgroundPaint)
        canvas.drawText(
            "Time: ${timeLeft.toInt()}",
            timerBounds.left + 10,
            timerBounds.bottom - 10,
            textPaint
        )
    }

    // Get bounds for testing/debugging
    fun getScoreBounds(): RectF = scoreBounds
    fun getLivesBounds(): RectF = livesBounds
    fun getWeaponBounds(): RectF = weaponBounds
    fun getTimerBounds(): RectF = timerBounds
}