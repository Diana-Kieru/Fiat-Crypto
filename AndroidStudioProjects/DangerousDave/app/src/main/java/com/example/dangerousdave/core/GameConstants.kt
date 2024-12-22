package com.example.dangerousdave.core


object GameConstants {
    // Screen and Display
    object Screen {
        const val DEFAULT_WIDTH = 1920f
        const val DEFAULT_HEIGHT = 1080f
        const val ASPECT_RATIO = DEFAULT_WIDTH / DEFAULT_HEIGHT
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 2.0f
    }

    // Physics Constants
    object Physics {
        const val GRAVITY = 1000f
        const val MAX_FALL_SPEED = 800f
        const val GROUND_FRICTION = 800f
        const val AIR_RESISTANCE = 100f
        const val BOUNCE_FACTOR = 0.2f
        const val MIN_MOVEMENT_SPEED = 0.01f
    }

    // Player Constants
    object Player {
        // Movement
        const val MOVE_SPEED = 200f
        const val JUMP_FORCE = 400f
        const val MAX_JUMP_TIME = 0.4f
        const val JETPACK_FORCE = 300f
        const val MAX_JETPACK_FUEL = 100f
        const val JETPACK_CONSUMPTION = 20f

        // Combat
        const val SHOOT_COOLDOWN = 0.5f
        const val INVULNERABILITY_TIME = 2f
        const val INITIAL_LIVES = 3
        const val MAX_LIVES = 5

        // Size
        const val WIDTH = 64f
        const val HEIGHT = 64f
    }

    // Enemy Constants
    object Enemy {
        // Movement
        const val WALKER_SPEED = 100f
        const val FLYER_SPEED = 150f
        const val JUMPER_FORCE = 300f

        // Combat
        const val CONTACT_DAMAGE = 1
        const val PROJECTILE_DAMAGE = 1
        const val SHOOT_INTERVAL = 2f

        // Size
        const val DEFAULT_WIDTH = 64f
        const val DEFAULT_HEIGHT = 64f
    }

    // Platform Constants
    object Platform {
        const val DEFAULT_WIDTH = 128f
        const val DEFAULT_HEIGHT = 32f
        const val MOVING_SPEED = 100f
        const val MOVING_DISTANCE = 200f
        const val BREAK_TIME = 0.5f
        const val RESPAWN_TIME = 3f
    }

    // Collectible Constants
    object Collectible {
        const val DEFAULT_SIZE = 32f
        const val FLOAT_AMPLITUDE = 10f
        const val FLOAT_SPEED = 2f
        const val COLLECTION_ANIMATION_TIME = 0.5f

        // Scoring
        const val TROPHY_POINTS = 100
        const val GUN_POINTS = 250
        const val EXTRA_LIFE_POINTS = 1000
        const val JETPACK_POINTS = 500
        const val KEY_POINTS = 150
        const val CROWN_POINTS = 5000
    }

    // Projectile Constants
    object Projectile {
        const val SPEED = 400f
        const val SIZE = 10f
        const val LIFETIME = 3.0f
        const val PLAYER_DAMAGE = 2
        const val ENEMY_DAMAGE = 1
    }

    // Game State Constants
    object GameState {
        const val MAX_LEVELS = 10
        const val TIME_BONUS_THRESHOLD = 60f
        const val TIME_BONUS_POINTS = 1000
        const val LEVEL_COMPLETION_BONUS = 500
    }

    // Input Constants
    object Input {
        const val TOUCH_SLOP = 8f
        const val LONG_PRESS_TIME = 500L
        const val DOUBLE_TAP_TIME = 300L
        const val MIN_SWIPE_DISTANCE = 100f
    }

    // Animation Constants
    object Animation {
        const val FRAME_TIME = 0.1f
        const val HURT_FLASH_TIME = 0.1f
        const val FADE_DURATION = 0.5f
        const val SHAKE_DURATION = 0.3f
        const val SHAKE_INTENSITY = 5f
    }

    // Debug Constants
    object Debug {
        const val SHOW_COLLISION_BOXES = false
        const val SHOW_FPS = true
        const val SHOW_ENTITY_COUNT = true
        const val LOG_LEVEL = 2  // 0=None, 1=Errors, 2=Warnings, 3=Info, 4=Debug
        const val INVINCIBLE_MODE = false
    }

    // Sound Constants
    object Sound {
        const val DEFAULT_SFX_VOLUME = 1.0f
        const val DEFAULT_MUSIC_VOLUME = 0.8f
        const val MAX_SIMULTANEOUS_SOUNDS = 10
        const val FADE_DURATION = 1.0f
    }

    // UI Constants
    object UI {
        const val BUTTON_SIZE = 64f
        const val PADDING = 16f
        const val FONT_SIZE = 24f
        const val HUD_OPACITY = 0.8f
        const val TOUCH_AREA_MULTIPLIER = 1.5f
    }
}