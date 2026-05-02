import korlibs.image.bitmap.BmpSlice

/**
 * All enemy configurations for the game.
 *
 * Each enemy is defined purely through EnemyConfig (no subclasses).
 * xpGain is set per-enemy to reflect its difficulty/reward level.
 */

object EnemyConfigs {

    // ============================================================
    // MELEE SKELETONS
    // ============================================================

    /**
     * Skeleton Warrior — basic melee enemy.
     * xpGain = 10 (easiest melee enemy)
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
     * xpGain = 12 (ranged but fragile)
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
                speed = 400.0,
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
            attackRange = 400.0,
            attackCooldown = 2.0,
            deathLingerTime = 2.0,
            frameDuration = 0.12,
            xpGain = 12.0
        )
    }

    /**
     * Skeleton Spearman — melee with longer range.
     * xpGain = 15 (slightly harder than basic skeleton)
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
                count = 10
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
     * Skeleton Boss — high-health ranged boss.
     * xpGain = 50 (major milestone reward)
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
                count = 8
            ),
            attackConfig = FrameConfig(
                folder = "skeleton_enemy/skeleton_boss",
                sheet = SpriteSheetConfig("skeleton_boss_attack", columns = 6, rows = 1),
                count = 15
            ),
            deathConfig = FrameConfig(
                folder = "skeleton_enemy/skeleton_boss",
                sheet = SpriteSheetConfig("skeleton_boss_attack", columns = 6, rows = 1),
                count = 5
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
            attackCooldown = 0.6,
            deathLingerTime = 1.0,
            frameDuration = 0.12,
            xpGain = 80.0
        )
    }

    // ============================================================
    // WOLVES (MELEE VARIANTS)
    // ============================================================

    /**
     * Wolf 1 — fast melee.
     * xpGain = 20
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
            maxHealth = 40.0,
            moveSpeed = 180.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.3,
            deathLingerTime = 2.0,
            frameDuration = 0.10,
            xpGain = 40.0
        )
    }

    /**
     * Wolf 2 — slightly stronger wolf variant.
     * xpGain = 25
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
                count = 10
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
                damage = 7.0,
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
            maxHealth = 50.0,
            moveSpeed = 160.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.5,
            deathLingerTime = 2.0,
            frameDuration = 0.10,
            xpGain = 50.0
        )
    }

    /**
     * Wolf 3 — fastest, most dangerous wolf.
     * xpGain = 30
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
                count = 11
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
                damage = 8.0,
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
            moveSpeed = 300.0,
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.2,
            deathLingerTime = 2.0,
            frameDuration = 0.09,
            xpGain = 100.0
        )
    }
}
