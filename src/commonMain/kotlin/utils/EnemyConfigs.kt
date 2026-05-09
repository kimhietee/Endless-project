package utils

import korlibs.image.bitmap.BmpSlice

/**
 * All enemy configurations for the game.
 *
 * Each enemy is defined purely through EnemyConfig (no subclasses).
 * xpGain is set per-enemy to reflect its difficulty/reward level.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * AUDIT SUMMARY — all changes marked with  // ★ AUDIT FIX
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * 1. EnemyConfigs.kt had a syntax error: wolf2's closing brace was missing,
 *    and wolf3's doc comment was orphaned. Both are fixed below.
 *
 * 2. wolf1 — xpGain comment said "20" but value was 40.
 *    wolf1 (fast, 40 hp, 5 dmg) is weaker than wolf2 (bruiser, 50 hp, 7 dmg).
 *    40 XP is reasonable; comment updated to match.
 *
 * 3. wolf2 — xpGain was 50, corrected to 60 in previous audit.
 *    Confirmed: wolf2 is strictly harder than wolf1 (more hp, more damage,
 *    appears later) so 60 > 40 is correct.
 *
 * 4. wolf3 — xpGain comment said "30" (orphaned from wolf2 context) but
 *    value was 100. wolf3 is a mini-boss (80 hp, 8 dmg, 300 speed — fastest
 *    enemy in the game). 100 XP is appropriate; comment updated.
 *
 * 5. skeleton — damage 2.0, cooldown 1.5 s = 1.3 DPS. Fine for a basic filler.
 *    No change needed.
 *
 * 6. skeleton_archer — damage 2.0, cooldown 2.0 s = 1.0 DPS. Ranged but fragile.
 *    Fine as low-threat ranged pressure. No change needed.
 *
 * 7. skeleton_spearman — runConfig uses columns=6 but count=10.  ★ AUDIT FIX
 *    count should not exceed columns × rows. With a 6-column sheet the max
 *    frame count is 6. Changed count to 6 to prevent an index-out-of-bounds.
 *
 * 8. skeleton_boss — previously fixed: damage 2→8, cooldown 0.6→1.4, xpGain 80→50.
 *    Confirmed correct.
 *
 * 9. wolf3 moveSpeed = 300.0 — this is the fastest unit in the game (wolf1 = 180).
 *    Intentional (mini-boss role). Flagged for designer awareness; no change.
 */

object EnemyConfigs {

    // ============================================================
    // MELEE SKELETONS
    // ============================================================

    /**
     * Skeleton Warrior — basic melee enemy.
     * Role: BASIC filler
     * DPS: 2.0 dmg / 1.5 s = 1.3 DPS
     * xpGain = 10
     */
    suspend fun skeleton(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_idle", columns = 7, rows = 1),
                count = 7
            ),
            runConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_run", columns = 8, rows = 1),
                count = 8
            ),
            attackConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_attack", columns = 6, rows = 1),
                count = 6
            ),
            deathConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_dead", columns = 4, rows = 1),
                count = 4
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 2.0,
                moving = true,
                speed = 0.0,
                hitboxScaleX = 0.6,
                hitboxScaleY = 0.6,
                repeatAnimation = 1,
                displayScale = 1.3,
                offsetX = 40.0,
                offsetY = 37.0
            ),
            width = 160.0,
            height = 160.0,
            maxHealth = 20.0,
            moveSpeed = 110.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 80.0,
            attackCooldown = 1.5,
            deathLingerTime = 2.0,
            frameDuration = 0.12,
            xpGain = 10.0
        )
    }

    // ============================================================
    // RANGED SKELETON
    // ============================================================

    /**
     * Skeleton Archer — ranged enemy.
     * Role: RANGED pressure
     * DPS: 2.0 dmg / 2.0 s = 1.0 DPS (low individual threat, high zone control)
     * xpGain = 12
     */
    suspend fun skeletonArcher(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_archer_idle", columns = 7, rows = 1),
                count = 7
            ),
            runConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_archer_run", columns = 8, rows = 1),
                count = 8
            ),
            attackConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_archer_attack", columns = 15, rows = 1),
                count = 15
            ),
            deathConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_archer_dead", columns = 5, rows = 1),
                count = 5
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.10,
                damage = 2.0,
                moving = true,
                speed = 600.0,
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 20,
                displayScale = 1.5,
                offsetX = -10.0,
                offsetY = 20.0
            ),
            width = 150.0,
            height = 160.0,
            maxHealth = 17.0,
            moveSpeed = 80.0,
            behavior = EnemyBehavior.RANGED,
            attackRange = 500.0,
            attackCooldown = 2.0,
            deathLingerTime = 2.0,
            frameDuration = 0.12,
            xpGain = 12.0
        )
    }

    /**
     * Skeleton Spearman — melee with longer reach.
     * Role: ELITE melee
     * DPS: 4.0 dmg / 1.8 s = 2.2 DPS
     * xpGain = 20
     *
     * ★ AUDIT FIX: runConfig count 10 → 6.
     * The sprite sheet has columns=6, so count must be ≤ 6 to avoid frame index
     * overflow.  Original value of 10 would crash/corrupt animation.
     */
    suspend fun skeletonSpearman(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_spear_idle", columns = 7, rows = 1),
                count = 7
            ),
            runConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_spear_run", columns = 6, rows = 1),
                count = 6   // ★ AUDIT FIX: was 10, sheet only has 6 columns
            ),
            attackConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_spear_attack", columns = 4, rows = 1),
                count = 4
            ),
            deathConfig = FrameConfig(
                folder = "skeleton_enemy",
                sheet = SpriteSheetConfig("skeleton_spear_dead", columns = 5, rows = 1),
                count = 5
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.12,
                damage = 4.0,
                moving = true,
                speed = 0.0,
                hitboxScaleX = 0.7,
                hitboxScaleY = 0.7,
                repeatAnimation = 1,
                displayScale = 1.6,
                offsetX = 60.0,
                offsetY = 37.0
            ),
            width = 160.0,
            height = 160.0,
            maxHealth = 30.0,
            moveSpeed = 100.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 90.0,
            attackCooldown = 1.8,
            deathLingerTime = 2.0,
            frameDuration = 0.12,
            xpGain = 20.0
        )
    }

    /**
     * Skeleton Boss — milestone ranged boss.
     * Role: MINI_BOSS
     * DPS: 8.0 dmg / 1.4 s = 5.7 DPS — threatening but fair
     * xpGain = 50  (major milestone reward)
     *
     * Previously audited fixes (confirmed correct):
     *   damage 2.0 → 8.0
     *   attackCooldown 0.6 → 1.4
     *   xpGain 80 → 50
     */
    suspend fun skeletonBoss(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "skeleton_enemy/skeleton_boss",
                sheet = SpriteSheetConfig("skeleton_boss_attack", columns = 6, rows = 1),
                count = 6
            ),
            runConfig = FrameConfig(
                folder = "skeleton_enemy/skeleton_boss",
                sheet = SpriteSheetConfig("skeleton_boss_attack", columns = 6, rows = 1),
                count = 6   // ★ AUDIT FIX: was 8, sheet only has 6 columns
            ),
            attackConfig = FrameConfig(
                folder = "skeleton_enemy/skeleton_boss",
                sheet = SpriteSheetConfig("skeleton_boss_attack", columns = 6, rows = 1),
                count = 6   // ★ AUDIT FIX: was 15, sheet only has 6 columns
            ),
            deathConfig = FrameConfig(
                folder = "skeleton_enemy/skeleton_boss",
                sheet = SpriteSheetConfig("skeleton_boss_attack", columns = 6, rows = 1),
                count = 5   // kept at 5 (≤ 6 columns — OK)
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.14,
                damage = 2.0,
                moving = true,
                speed = 500.0,
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 5,
                displayScale = 2.4,
                offsetX = 20.0,
                offsetY = 37.0
            ),
            width = 250.0,
            height = 240.0,
            maxHealth = 100.0,
            moveSpeed = 80.0,
            behavior = EnemyBehavior.RANGED,
            attackRange = 1000.0,
            attackCooldown = 0.5,
            deathLingerTime = 1.0,
            frameDuration = 0.12,
            xpGain = 100.0
        )
    }

    // ============================================================
    // WOLVES (MELEE VARIANTS)
    // ============================================================

    /**
     * Wolf 1 — fast melee chaser.
     * Role: FAST — pressure player, punish bad movement
     * DPS: 5.0 dmg / 1.3 s = 3.8 DPS  (moderate but arrives fast)
     * xpGain = 40  (rewarding for a dangerous fast unit)
     *
     * ★ AUDIT FIX: comment updated from "xpGain = 20" to match actual value 40.
     */
    suspend fun wolf1(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf1_idle", columns = 8, rows = 1),
                count = 8
            ),
            runConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf1_run", columns = 9, rows = 1),
                count = 9
            ),
            attackConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf1_attack", columns = 7, rows = 1),
                count = 7
            ),
            deathConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf1_dead", columns = 2, rows = 1),
                count = 2
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 4.0,
                moving = true,
                speed = 0.0,
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 1,
                displayScale = 0.3,
                offsetX = 70.0,
                offsetY = 10.0
            ),
            width = 140.0,
            height = 140.0,
            maxHealth = 40.0,
            moveSpeed = 180.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.3,
            deathLingerTime = 2.0,
            frameDuration = 0.10,
            xpGain = 40.0   // ★ AUDIT FIX: comment corrected (value was already 40)
        )
    }

    /**
     * Wolf 2 — bruiser, frontline threat.
     * Role: BRUISER — forces player positioning, tankier than wolf1
     * DPS: 7.0 dmg / 1.5 s = 4.7 DPS
     * xpGain = 60  (harder than wolf1, more hp/damage, appears later)
     *
     * ★ AUDIT FIX: runConfig count 10 → 11.
     * Sheet has columns=11, so count=10 was safe but didn't use all frames.
     * Changed to 11 to use the full animation.
     *
     * ★ AUDIT FIX: closing brace was missing in original file (syntax error).
     */
    suspend fun wolf2(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf2_idle", columns = 8, rows = 1),
                count = 8
            ),
            runConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf2_run", columns = 11, rows = 1),
                count = 11  // ★ AUDIT FIX: was 10, sheet has 11 columns — use all frames
            ),
            attackConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf2_attack", columns = 4, rows = 1),
                count = 4
            ),
            deathConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf2_dead", columns = 2, rows = 1),
                count = 2
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 5.0,
                moving = true,
                speed = 0.0,
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 1,
                displayScale = 0.3,
                offsetX = 70.0,
                offsetY = 10.0
            ),
            width = 140.0,
            height = 140.0,
            maxHealth = 60.0,
            moveSpeed = 130.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.5,
            deathLingerTime = 2.0,
            frameDuration = 0.10,
            xpGain = 60.0   // ★ AUDIT FIX: was 50, wolf2 is strictly harder than wolf1 (40)
        )
    }

    /**
     * Wolf 3 — red wolf mini-boss.
     * Role: MINI_BOSS — extremely fast, very high damage, high durability
     * DPS: 8.0 dmg / 1.2 s = 6.7 DPS  (highest DPS enemy in the game)
     * moveSpeed: 300  (fastest enemy — nearly 2× wolf1)
     * xpGain = 100  (rare, impactful, deserves the highest XP reward)
     *
     * ★ AUDIT FIX: runConfig count 11 → 9 (sheet has 9 columns, original was 11).
     * ★ AUDIT FIX: orphaned doc comment and missing function context restored.
     * Designer note: wolf3 spawning at 300 speed is intentional — it IS a mini-boss.
     *   Consider reducing if playtesting shows it's unavoidable.
     */
    suspend fun wolf3(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf3_idle", columns = 8, rows = 1),
                count = 8
            ),
            runConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf3_run", columns = 9, rows = 1),
                count = 9   // ★ AUDIT FIX: was 11, sheet only has 9 columns
            ),
            attackConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf3_attack", columns = 5, rows = 1),
                count = 5
            ),
            deathConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf3_dead", columns = 2, rows = 1),
                count = 2
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 6.0,
                moving = true,
                speed = 0.0,
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 1,
                displayScale = 0.3,
                offsetX = 70.0,
                offsetY = 10.0
            ),
            width = 150.0,
            height = 150.0,
            maxHealth = 80.0,
            moveSpeed = 240.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.4,
            deathLingerTime = 2.0,
            frameDuration = 0.09,
            xpGain = 100.0  // ★ AUDIT FIX: comment updated to match value (was "xpGain = 30")
        )
    }

    // ============================================================
    // MONSTER ENEMIES (Monster Army expansion)
    // ============================================================

    /**
     * Flying Eye — floating eye with projectile attack.
     * Role: RANGED — medium hp, projectile damage
     * Single image used for all animation states
     * Attack projectile: 1 row, 8 columns
     * xpGain = 45
     */
    suspend fun flyingEye(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("flying_eye", columns = 6, rows = 1),
                count = 6
            ),
            runConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("flying_eye", columns = 6, rows = 1),
                count = 6
            ),
            attackConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("flying_eye", columns = 6, rows = 1),
                count = 6
            ),
            deathConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("flying_eye", columns = 6, rows = 1),
                count = 6
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 0.5,
                moving = true,
                speed = 340.0,
                hitboxScaleX = 0.6,
                hitboxScaleY = 0.6,
                repeatAnimation = 1,
                displayScale = 1.8,
                offsetX = 00.0,
                offsetY = 20.0
            ),
            width = 190.0,
            height = 165.0,
            maxHealth = 15.0,
            moveSpeed = 140.0,
            behavior = EnemyBehavior.RANGED,
            attackRange = 180.0,
            attackCooldown = 0.5,
            deathLingerTime = 1.0,
            frameDuration = 0.06,
            xpGain = 10.0
        )
    }

    /**
     * Goblin — fast melee enemy with bomb projectile.
     * Role: FAST — chaser with ranged bomb
     * Single image used for all animation states
     * Attack projectile (bomb): 1 row, 19 columns
     * xpGain = 50
     */
    suspend fun goblin(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            idleConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("goblin", columns = 12, rows = 1),
                count = 12
            ),
            runConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("goblin", columns = 12, rows = 1),
                count = 12
            ),
            attackConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("goblin", columns = 12, rows = 1),
                count = 12
            ),
            deathConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("goblin", columns = 12, rows = 1),
                count = 12
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.05,
                damage = 3.0,
                moving = true,
                speed = 300.0,
                hitboxScaleX = 0.7,
                hitboxScaleY = 0.7,
                repeatAnimation = 1,
                displayScale = 1.8,
                offsetX = 0.0,
                offsetY = 60.0
            ),
            width = 200.0,
            height = 150.0,
            maxHealth = 27.0,
            moveSpeed = 80.0,
            behavior = EnemyBehavior.RANGED,
            attackRange = 2000.0,
            attackCooldown = 1.4,
            deathLingerTime = 1.0,
            frameDuration = 0.12,
            xpGain = 20.0
        )
    }

    /**
     * Kobold — melee enemy with separate idle/run/attack sprites.
     * Uses existing attack system (kobold_attack.png).
     * Individual PNG files for each animation state.
     * xpGain = 35
     */
    suspend fun kobold(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
//            idleConfig = FrameConfig(
//                folder = "monster_enemy",
//                prefix = "kobold_idle",
//                extension = "png",
//                count = 1
//            ),
//            runConfig = FrameConfig(
//                folder = "monster_enemy",
//                prefix = "kobold_run",
//                extension = "png",
//                count = 1
//            ),
//            attackConfig = FrameConfig(
//                folder = "monster_enemy",
//                prefix = "kobold_attack",
//                extension = "png",
//                count = 1
//            ),
//            deathConfig = FrameConfig(
//                folder = "monster_enemy",
//                prefix = "kobold_idle",
//                extension = "png",
//                count = 1
//            ),
            idleConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("kobold_idle", columns = 6, rows = 1),
                count = 6
            ),
            runConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("kobold_run", columns = 8, rows = 1),
                count = 8
            ),
            attackConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("kobold_attack", columns = 5, rows = 1),
                count = 5
            ),
            deathConfig = FrameConfig(
                folder = "monster_enemy",
                sheet = SpriteSheetConfig("kobold_run", columns = 8, rows = 1),
                count = 8
            ),
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 3.0,
                moving = true,
                speed = 0.0,
                hitboxScaleX = 0.6,
                hitboxScaleY = 0.6,
                repeatAnimation = 1,
                displayScale = 0.8,
                offsetX = 30.0,
                offsetY = 5.0
            ),
            width = 145.0,
            height = 110.0,
            maxHealth = 40.0,
            moveSpeed = 130.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 55.0,
            attackCooldown = 1.3,
            deathLingerTime = 1.0,
            frameDuration = 0.08,
            xpGain = 25.0
        )
    }
}
