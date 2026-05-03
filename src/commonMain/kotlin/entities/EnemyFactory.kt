package entities

import utils.*
import managers.GameAssets

/**
 * Factory for creating enemies by type string.
 * 
 * Maps string type names to EnemyConfig objects,
 * then creates Enemy instances using Enemy.create()
 * 
 * Usage:
 *   val enemy = EnemyFactory.create("skeleton")
 *   val enemy = EnemyFactory.create("wolf2")
 */

class EnemyFactory {
    companion object {
        /**
         * Create an enemy by type string.
         * 
         * @param type Enemy type: "skeleton", "skeleton_spearman", "skeleton_archer", "skeleton_ranger", "skeleton_brute", "skeleton_scout", "skeleton_boss", "flying_eye", "goblin", "kobold", "wolf1", "wolf2", "wolf3"
         * @return Enemy instance
         * @throws IllegalArgumentException if type is unknown
         */
        suspend fun create(type: String): Enemy {
            // First, load the attack display frames for this enemy type
            val attackFrames = loadAttackFrames(type)
            
            // Get the config for this enemy type
            val config = when (type) {
                "skeleton"           -> EnemyConfigs.skeleton(attackFrames)
                "skeleton_spearman"  -> EnemyConfigs.skeletonSpearman(attackFrames)
                "skeleton_archer"    -> EnemyConfigs.skeletonArcher(attackFrames)
                "skeleton_boss"      -> EnemyConfigs.skeletonBoss(attackFrames)

                "wolf1"              -> EnemyConfigs.wolf1(attackFrames)
                "wolf2"              -> EnemyConfigs.wolf2(attackFrames)
                "wolf3"              -> EnemyConfigs.wolf3(attackFrames)

                "flying_eye"         -> EnemyConfigs.flyingEye(attackFrames)
                "goblin"             -> EnemyConfigs.goblin(attackFrames)
                "kobold"             -> EnemyConfigs.kobold(attackFrames)
                else                 -> throw IllegalArgumentException("Unknown enemy type: $type")
            }
            
            // Create and return the enemy using the existing Enemy.create()
            return Enemy.create(config)
        }
        
        /**
         * Load attack animation frames for a specific enemy type.
         * Different enemies use different attack visuals.
         */
        private suspend fun loadAttackFrames(type: String) = when (type) {
            "skeleton" -> {
                // Skeleton uses basic slash
                GameAssets.loadFrames(
                    FrameConfig(
                        folder = "skeleton_enemy/skeleton_slash",
                        sheet = SpriteSheetConfig("slash", columns = 4, rows = 1),
                        count = 4
                    )
                )
            }
            "skeleton_spearman" -> {
                // Spearman uses spear slash
                GameAssets.loadFrames(
                    FrameConfig(
                        folder = "skeleton_enemy/skeleton_slash",
                        sheet = SpriteSheetConfig("spear_slash", columns = 5, rows = 1),
                        count = 5
                    )
                )
            }
            "skeleton_archer" -> {
                // Archer uses arrow
                GameAssets.loadFrames(
                    FrameConfig(
                        folder = "skeleton_enemy",
                        sheet = SpriteSheetConfig("arrow", columns = 1, rows = 1),
                        count = 1
                    )
                )
            }
            "skeleton_boss" -> {
                // Boss uses sword projectile
                GameAssets.loadFrames(
                    FrameConfig(
                        folder = "skeleton_enemy/skeleton_boss",
                        sheet = SpriteSheetConfig("sword_projectile", columns = 8, rows = 1),
                        count = 8
                    )
                )
            }

            "flying_eye" -> {
                // Flying Eye uses eye projectile
                GameAssets.loadFrames(
                    FrameConfig(
                        folder = "monster_enemy/attacks",
                        sheet = SpriteSheetConfig("eye_projectile", columns = 8, rows = 1),
                        count = 8
                    )
                )
            }
            "goblin" -> {
                // Goblin uses bomb projectile
                GameAssets.loadFrames(
                    FrameConfig(
                        folder = "monster_enemy/attacks",
                        sheet = SpriteSheetConfig("bomb", columns = 19, rows = 1),
                        count = 19
                    )
                )
            }
            "kobold" -> {
                // Kobold uses existing melee projectile (same as skeleton for now)
                GameAssets.loadFrames(
                    FrameConfig(
                        folder = "skeleton_enemy/skeleton_slash",
                        sheet = SpriteSheetConfig("slash", columns = 4, rows = 1),
                        count = 4
                    )
                )
            }

            "wolf1", "wolf2", "wolf3" -> {
                // All wolves use the same wolf slash animation
                GameAssets.loadFrames(
                    FrameConfig(
                        folder = "wolf_enemy/wolf_slash",
                        prefix = "slash4_",
                        zeroPad = 5,
                        startIndex = 1,
                        count = 12
                    )
                )
            }
            else -> throw IllegalArgumentException("Unknown enemy type: $type")
        }
        
        /**
         * Get all available enemy types.
         */
        fun getAllTypes() = listOf(
            "skeleton",
            "skeleton_spearman",
            "skeleton_archer",
            "skeleton_boss",
            "flying_eye",
            "goblin",
            "kobold",
            "wolf1",
            "wolf2",
            "wolf3"
        )
    }
}
