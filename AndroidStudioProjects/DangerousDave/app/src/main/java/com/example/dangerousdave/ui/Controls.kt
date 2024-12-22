package com.example.dangerousdave.ui


import android.graphics.RectF

class Controls {
    // Control areas
    private val leftButton = RectF()
    private val rightButton = RectF()
    private val jumpButton = RectF()
    private val shootButton = RectF()
    private val pauseButton = RectF()

    companion object {
        // Button sizes and positions in percentages of screen
        private const val MOVE_BUTTON_SIZE = 0.15f    // 15% of screen height
        private const val ACTION_BUTTON_SIZE = 0.18f   // 18% of screen height
        private const val PAUSE_BUTTON_SIZE = 0.08f    // 8% of screen height
        private const val BUTTON_MARGIN = 0.02f        // 2% of screen width/height
    }

    // Update control dimensions based on screen size
    fun updateDimensions(screenWidth: Int, screenHeight: Int) {
        val moveButtonSize = screenHeight * MOVE_BUTTON_SIZE
        val actionButtonSize = screenHeight * ACTION_BUTTON_SIZE
        val pauseButtonSize = screenHeight * PAUSE_BUTTON_SIZE
        val margin = screenWidth * BUTTON_MARGIN

        // Left movement button (bottom left)
        leftButton.set(
            margin,
            screenHeight - moveButtonSize - margin,
            margin + moveButtonSize,
            screenHeight - margin
        )

        // Right movement button (to the right of left button)
        rightButton.set(
            leftButton.right + margin,
            screenHeight - moveButtonSize - margin,
            leftButton.right + margin + moveButtonSize,
            screenHeight - margin
        )

        // Jump button (bottom right)
        jumpButton.set(
            screenWidth - actionButtonSize - margin,
            screenHeight - actionButtonSize - margin,
            screenWidth - margin,
            screenHeight - margin
        )

        // Shoot button (above jump button)
        shootButton.set(
            screenWidth - actionButtonSize - margin,
            jumpButton.top - actionButtonSize - margin,
            screenWidth - margin,
            jumpButton.top - margin
        )

        // Pause button (top right)
        pauseButton.set(
            screenWidth - pauseButtonSize - margin,
            margin,
            screenWidth - margin,
            margin + pauseButtonSize
        )
    }

    // Button press checks
    fun isLeftPressed(x: Float, y: Float): Boolean = leftButton.contains(x, y)
    fun isRightPressed(x: Float, y: Float): Boolean = rightButton.contains(x, y)
    fun isJumpPressed(x: Float, y: Float): Boolean = jumpButton.contains(x, y)
    fun isShootPressed(x: Float, y: Float): Boolean = shootButton.contains(x, y)
    fun isPausePressed(x: Float, y: Float): Boolean = pauseButton.contains(x, y)

    // Get button bounds for rendering
    fun getLeftButtonBounds(): RectF = leftButton
    fun getRightButtonBounds(): RectF = rightButton
    fun getJumpButtonBounds(): RectF = jumpButton
    fun getShootButtonBounds(): RectF = shootButton
    fun getPauseButtonBounds(): RectF = pauseButton
}