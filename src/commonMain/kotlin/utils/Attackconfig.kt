package utils

import korlibs.image.bitmap.BmpSlice
import korlibs.korge.view.View

/**
 * All tunable parameters for one AttackDisplay instance.
 *
 * @param frames          animation frames for this attack
 * @param frameDuration   seconds per frame
 * @param damage          TOTAL damage this attack deals
 * @param moving          true  = projectile moves, hits each target ONCE then stops damaging them
 *                        false = stationary, damage split across frames, applied each frame on overlap
 * @param speed           pixels/second (positive = right, negative = left)
 * @param hitboxScaleX    hitbox width  as fraction of sprite width  (default 0.5)
 * @param hitboxScaleY    hitbox height as fraction of sprite height (default 0.5)
 * @param repeatAnimation how many times the animation loops before the attack is removed
 * @param followParent    when set, each frame this display snaps to parent position + offsets
 * @param healSelfPerAnimationFrame applied once each time the animation advances to a new frame
 * @param damageEnemies   when false, no damage is applied to targets
 */
data class AttackConfig(
    val frames:          List<BmpSlice>,
    val frameDuration:   Double  = 0.10,
    val damage:          Double  = 10.0,
    val moving:          Boolean = false,
    val speed:           Double  = 0.0,
    val hitboxScaleX:    Double  = 0.5,
    val hitboxScaleY:    Double  = 0.5,
    val repeatAnimation: Int     = 1,
    val displayScale:    Double  = 1.0,
    val offsetX:         Double  = 0.0,
    val offsetY:         Double  = 0.0,
    val followParent:     View?   = null,
    val followOffsetX:   Double  = 0.0,
    val followOffsetY:   Double  = 0.0,
    val healSelfPerAnimationFrame: Double = 0.0,
    val damageEnemies:   Boolean = true
)
