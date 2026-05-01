import korlibs.image.bitmap.BmpSlice

/**
 * All 6 enemy configurations for the game.
 * 
 * Each enemy is defined purely through EnemyConfig (no subclasses).
 * Enemies differ only in:
 * - Animation frames
 * - Attack display config
 * - Stats (health, speed, damage, etc.)
 * - Behavior (MELEE vs RANGED)
 */

// ============================================================
// MELEE SKELETONS
// ============================================================

object EnemyConfigs {
    
    /**
     * Skeleton Warrior - basic melee enemy
     * - Chases player
     * - Attacks on contact with melee slash
     * - Medium health
     */
    suspend fun skeleton(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            // --- Animation Frames ---
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
            
            // --- Attack Display Config ---
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 2.0,
                moving = true,      // Stationary melee hitbox
                speed = 0.0,
                hitboxScaleX = 0.6,
                hitboxScaleY = 0.6,
                repeatAnimation = 1,
                displayScale = 1.3,
                offsetX = 40.0,
                offsetY = 37.0
            ),
            
            // --- Size ---
            width = 160.0,
            height = 160.0,
            
            // --- Stats ---
            maxHealth = 20.0,
            moveSpeed = 110.0,
            
            // --- Behavior ---
            behavior = EnemyBehavior.MELEE,
            attackRange = 80.0,
            attackCooldown = 1.5,
            deathLingerTime = 2.0,
            
            // --- Animation ---
            frameDuration = 0.12
        )
    }
    
    /**
     * Skeleton Spearman - melee with longer range
     * - Chases player
     * - Longer attack range than basic skeleton
     * - Attacks with spear thrust
     * - Similar health to skeleton
     */
    suspend fun skeletonSpearman(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            // --- Animation Frames ---
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
            
            // --- Attack Display Config ---
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.12,
                damage = 4.0,
                moving = true,      // Stationary melee hitbox
                speed = 0.0,
                hitboxScaleX = 0.7,  // Larger hitbox for spear
                hitboxScaleY = 0.7,
                repeatAnimation = 1,
                displayScale = 1.6,
                offsetX = 60.0,
                offsetY = 37.0
            ),
            
            // --- Size ---
            width = 160.0,
            height = 160.0,
            
            // --- Stats ---
            maxHealth = 30.0,
            moveSpeed = 100.0,      // Slightly slower due to spear weight
            
            // --- Behavior ---
            behavior = EnemyBehavior.MELEE,
            attackRange = 90.0,    // Longer range due to spear
            attackCooldown = 1.8,   // Slower attack
            deathLingerTime = 2.0,
            
            // --- Animation ---
            frameDuration = 0.12
        )
    }

    // ============================================================
    // RANGED SKELETON
    // ============================================================
    
    /**
     * Skeleton Archer - ranged enemy
     * - Chases player from distance
     * - Stops at attack range and shoots arrows
     * - Lower health than melee skeletons
     * - Lower movement speed
     */
    suspend fun skeletonArcher(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            // --- Animation Frames ---
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
            
            // --- Attack Display Config ---
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.10,
                damage = 3.0,
                moving = true,       // Projectile - moves across screen
                speed = 400.0,       // Projectile speed (will be signed based on direction)
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 20,
                displayScale = 1.5,
                offsetX = -10.0,
                offsetY = 20.0
            ),
            
            // --- Size ---
            width = 150.0,
            height = 160.0,
            
            // --- Stats ---
            maxHealth = 17.0,        // Lower health for ranged
            moveSpeed = 80.0,       // Slower to compensate for range
            
            // --- Behavior ---
            behavior = EnemyBehavior.RANGED,
            attackRange = 400.0,     // Attacks from far away
            attackCooldown = 2.0,    // Slower fire rate
            deathLingerTime = 2.0,
            
            // --- Animation ---
            frameDuration = 0.12
        )
    }

    suspend fun skeletonBoss(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            // --- Animation Frames ---
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

            // --- Attack Display Config ---
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.14,
                damage = 2.0,
                moving = true,       // Projectile - moves across screen
                speed = 500.0,       // Projectile speed (will be signed based on direction)
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 5,
                displayScale = 2.4,
                offsetX = 20.0,
                offsetY = 37.0
            ),

            // --- Size ---
            width = 250.0,
            height = 240.0,

            // --- Stats ---
            maxHealth = 100.0,
            moveSpeed = 80.0,       // Slower to compensate for range

            // --- Behavior ---
            behavior = EnemyBehavior.RANGED,
            attackRange = 1000.0,
            attackCooldown = 0.6,
            deathLingerTime = 1.0,

            // --- Animation ---
            frameDuration = 0.12
        )
    }

    // ============================================================
    // WOLVES (MELEE VARIANTS)
    // ============================================================
    
    /**
     * Wolf 1 - melee enemy
     * - Faster than skeletons
     * - Medium health
     * - Aggressive AI
     */
    suspend fun wolf1(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            // --- Animation Frames ---
            idleConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf1_idle", columns = 10, rows = 1),
                count = 10
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
            
            // --- Attack Display Config ---
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 7.0,
                moving = true,      // Stationary melee hitbox
                speed = 0.0,
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 1,
                displayScale = 0.3,
                offsetX = 70.0,
                offsetY = 10.0
            ),
            
            // --- Size ---
            width = 140.0,
            height = 140.0,
            
            // --- Stats ---
            maxHealth = 40.0,
            moveSpeed = 180.0,       // Faster than skeletons
            
            // --- Behavior ---
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.3,    // Faster attacks
            deathLingerTime = 2.0,
            
            // --- Animation ---
            frameDuration = 0.10     // Faster animation for agile wolf
        )
    }
    
    /**
     * Wolf 2 - melee enemy (variant)
     * - Slightly faster than Wolf 1
     * - Slightly more health
     * - More aggressive
     */
    suspend fun wolf2(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            // --- Animation Frames ---
            idleConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf2_idle", columns = 11, rows = 1),
                count = 11
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
            
            // --- Attack Display Config ---
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 8.0,
                moving = true,      // Stationary melee hitbox
                speed = 0.0,
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 1,
                displayScale = 0.3,
                offsetX = 70.0,
                offsetY = 10.0
            ),
            
            // --- Size ---
            width = 140.0,
            height = 140.0,
            
            // --- Stats ---
            maxHealth = 50.0,
            moveSpeed = 160.0,       // Faster
            
            // --- Behavior ---
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.0,    // More aggressive
            deathLingerTime = 2.0,
            
            // --- Animation ---
            frameDuration = 0.10
        )
    }
    
    /**
     * Wolf 3 - melee enemy (variant)
     * - Fastest wolf
     * - Most health
     * - Most aggressive
     */
    suspend fun wolf3(attackFrames: List<BmpSlice>): EnemyConfig {
        return EnemyConfig(
            // --- Animation Frames ---
            idleConfig = FrameConfig(
                folder = "wolf_enemy",
                sheet = SpriteSheetConfig("wolf3_idle", columns = 9, rows = 1),
                count = 9
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
            
            // --- Attack Display Config ---
            attackDisplayConfig = AttackConfig(
                frames = attackFrames,
                frameDuration = 0.08,
                damage = 10.0,
                moving = true,      // Stationary melee hitbox
                speed = 0.0,
                hitboxScaleX = 0.5,
                hitboxScaleY = 0.5,
                repeatAnimation = 1,
                displayScale = 0.3,
                offsetX = 70.0,
                offsetY = 10.0
            ),
            
            // --- Size ---
            width = 150.0,
            height = 150.0,
            
            // --- Stats ---
            maxHealth = 80.0,
            moveSpeed = 300.0,       // Fastest
            
            // --- Behavior ---
            behavior = EnemyBehavior.MELEE,
            attackRange = 70.0,
            attackCooldown = 1.0,    // Most aggressive
            deathLingerTime = 2.0,
            
            // --- Animation ---
            frameDuration = 0.09     // Fastest animation
        )
    }
}
