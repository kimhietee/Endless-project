import korlibs.image.bitmap.BmpSlice

/**
 * Describes how to load frames from a folder.
 *
 * Resulting path: "$folder/${prefix}${paddedIndex}.$extension"
 *
 * Examples:
 *   prefix="frame_", zeroPad=0  →  run/frame_0.png, run/frame_1.png
 *   prefix="",        zeroPad=0  →  run/0.png,       run/1.png
 *   prefix="tile",   zeroPad=3  →  run/tile000.png, run/tile001.png
 */
data class SpriteSheetConfig(
    val fileName: String,
    val columns: Int,
    val rows: Int = 1
)

data class FrameConfig(
    val folder:     String,
    val prefix:     String = "frame_",
    val extension:  String = "png",
    val startIndex: Int    = 0,
    val count:      Int    = 1,
    val zeroPad:    Int    = 0,
    val sheet:      SpriteSheetConfig? = null
)

// -------------------------------------------------------
// ENEMY BEHAVIOR TYPE
// -------------------------------------------------------
enum class EnemyBehavior {
    MELEE,   // chase → attack on contact
    RANGED   // chase → stop at attack range → attack
}

// -------------------------------------------------------
// ENEMY FULL CONFIG
// -------------------------------------------------------
/**
 * All tunable parameters for one enemy type.
 *
 * @param idleConfig           sprite frames for idle state
 * @param runConfig            sprite frames for run state
 * @param attackConfig         sprite frames for attack animation
 * @param deathConfig          sprite frames for death animation
 * @param attackDisplayConfig  AttackConfig for the spawned AttackDisplay when this enemy attacks
 * @param width                render width
 * @param height               render height
 * @param maxHealth            starting health
 * @param moveSpeed            pixels per second while chasing
 * @param behavior             MELEE or RANGED
 * @param attackRange          distance in pixels to trigger attack
 * @param attackCooldown       seconds between attacks
 * @param deathLingerTime      seconds to wait after death animation before removal
 * @param frameDuration        seconds per animation frame
 */
data class EnemyConfig(
    // --- sprite loaders ---
    val idleConfig:           FrameConfig,
    val runConfig:            FrameConfig,
    val attackConfig:         FrameConfig,
    val deathConfig:          FrameConfig,

    // --- spawned attack ---
    val attackDisplayConfig:  AttackConfig,

    // --- size ---
    val width:  Double = 140.0,
    val height: Double = 160.0,

    // --- stats ---
    val maxHealth:  Double = 100.0,
    val moveSpeed:  Double = 180.0,

    // --- behavior ---
    val behavior:        EnemyBehavior = EnemyBehavior.MELEE,
    val attackRange:     Double        = 80.0,
    val attackCooldown:  Double        = 1.2,
    val deathLingerTime: Double        = 2.0,

    // --- animation ---
    val frameDuration: Double = 0.12
)
