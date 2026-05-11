package entities.heroes

import utils.SkillConfig

/**
 * Presentation-only tuning for attacks (frames still come from [managers.GameAssets] / the player constructor).
 */
data class BasicAttackTuning(
    val frameDuration: Double,
    val moving: Boolean,
    val speed: Double,
    val hitboxScaleX: Double,
    val hitboxScaleY: Double,
    val repeatAnimation: Int,
    val displayScale: Double,
    val offsetX: Double,
    val offsetY: Double
)

data class SkillAttackTuning(
    val frameDuration: Double,
    val moving: Boolean,
    /** Pixels/sec for moving attacks; sign is applied from facing in [entities.Character]. */
    val projectileSpeed: Double,
    val hitboxScaleX: Double,
    val hitboxScaleY: Double,
    val repeatAnimation: Int,
    val displayScale: Double,
    val offsetX: Double,
    val offsetY: Double
)

/** Which body animation sheet to use during certain skills (see [entities.Character]). */
data class HeroBodyAnimRules(
    val skill1UsesAttackAnimForBody: Boolean = false,
    val skill2HealingAuraFollowPlayer: Boolean = false,
    val skill3BodyUsesCastSheet: Boolean = false,
    val skill4ChargeBeforeBall: Boolean = false
)

/**
 * All tunable hero-specific stats, skills, and attack presentation.
 * Add new heroes by creating a new file that returns a [HeroConfig] (see [FireWizardHero]).
 */
data class HeroConfig(
    val id: String,
    val displayName: String,
    val maxHealth: Double,
    val maxMana: Double,
    val manaRegen: Double,
    val healthRegen: Double,
    val runningSpeed: Double,
    val charSpeed: Double,
    val animFrameDuration: Double,
    val jumpForce: Double,
    val gravity: Double,
    /** Matches legacy skill2 total ticks / damage multiplier. */
    val skill2RepeatCount: Int,
    val basicAttackSkill: SkillConfig,
    val skill1Config: SkillConfig,
    val skill2Config: SkillConfig,
    val skill3Config: SkillConfig,
    val skill4Config: SkillConfig,
    val healingSkillConfig: SkillConfig,
    val maxHealthSkillConfig: SkillConfig,
    val basicAttackTuning: BasicAttackTuning,
    val skill1Tuning: SkillAttackTuning,
    val skill2Tuning: SkillAttackTuning,
    val skill3Tuning: SkillAttackTuning,
    val skill4Tuning: SkillAttackTuning,
    val bodyAnimRules: HeroBodyAnimRules = HeroBodyAnimRules(),
    /** When > 0, skill2 spawns a follow-player aura that heals this total over its duration (no enemy damage). */
    val skill2AuraTotalHeal: Double = 0.0,
    /** How long the player stays in skill2 “casting” state while the aura runs. */
    val skill2AuraDurationSeconds: Double = 0.0,
    /** Seconds per frame while playing skill4 charge sheets on the body. */
    val skill4ChargeFrameDuration: Double = 0.06,
    /** After the orb spawns, max time before the hero can act again (ball may leave screen sooner). */
    val skill4BallMaxDurationSeconds: Double = 0.0
) {
    val allSkills: List<SkillConfig> = listOf(
        basicAttackSkill,
        skill1Config,
        skill2Config,
        skill3Config,
        skill4Config,
        healingSkillConfig,
        maxHealthSkillConfig
    )
}
