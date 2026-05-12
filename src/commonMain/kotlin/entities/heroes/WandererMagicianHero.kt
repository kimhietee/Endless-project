package entities.heroes

import utils.Constants
import utils.SkillConfig

/**
 * Wanderer Magician — assets under `resources/wandererMagician/` (see [managers.GameAssets.loadWandererMagicianAssets]).
 *
 * Body: idle 1×8, attack 1×7 (basic + skill1 cast), run/jump 1×8.
 * Basic attack: ranged projectile from `skills/projectile_basic` (1×6).
 * Skill1: fireball-style projectile `skills/skill1` (1×9).
 * Skill2: self-heal aura from `skills/513.PNG` (10×5), follows player, 20 HP total.
 * Skill3: cast `334.PNG` (7×5) on body; explosion damage from `skills/explode` (1×9), stationary like Fire Wizard skill 3.
 * Skill4: body charge `skills/charge` (1×16), then moving orb from `vv1`–`vv3` (repeat 50, 50 damage).
 */
object WandererMagicianHero {
    const val ID = "wanderer_magician"

    fun createConfig(): HeroConfig {
        val basicAttackSkill = SkillConfig(
            name = "Arcane Bolt",
            cooldownMax = 0.0,
            manaCost = 0,
            damage = 5.0,
            unlockLevel = 1,
            requiresPointUnlock = false,
            damagePerUpgrade = 1.0,
            maxUpgrades = 10
        )
        val skill1Config = SkillConfig(
            name = "Comet Shard",
            cooldownMax = 3.0,
            manaCost = 15,
            damage = 8.0,
            unlockLevel = 3,
            damagePerUpgrade = 1.0,
            cooldownReductionPerUpgrade = 0.3,
            maxUpgrades = 5
        )
        val skill2Config = SkillConfig(
            name = "Sanctuary Veil",
            cooldownMax = 12.0,
            manaCost = 35,
            damage = 0.0,
            unlockLevel = 6,
            damagePerUpgrade = 0.0,
            cooldownReductionPerUpgrade = 0.4,
            maxUpgrades = 5
        )
        val skill3Config = SkillConfig(
            name = "Starfall Rupture",
            cooldownMax = 12.0,
            manaCost = 50,
            damage = 32.0,
            unlockLevel = 12,
            damagePerUpgrade = 4.0,
            cooldownReductionPerUpgrade = 0.5,
            maxUpgrades = 5
        )
        val skill4Config = SkillConfig(
            name = "Void Orb",
            cooldownMax = 22.0,
            manaCost = 90,
            damage = 50.0,
            unlockLevel = 18,
            damagePerUpgrade = 6.0,
            cooldownReductionPerUpgrade = 1.0,
            maxUpgrades = 5
        )
        val healingSkillConfig = SkillConfig(
            name = "Healing",
            cooldownMax = 3.0,
            manaCost = 30,
            damage = 10.0,
            unlockLevel = 1,
            requiresPointUnlock = true,
            damagePerUpgrade = 2.0,
            cooldownReductionPerUpgrade = 0.0,
            minCooldown = 3.0,
            maxUpgrades = 10
        )
        val maxHealthSkillConfig = SkillConfig(
            name = "Max Health",
            cooldownMax = 0.0,
            manaCost = 0,
            damage = 0.0,
            unlockLevel = 1,
            requiresPointUnlock = true,
            damagePerUpgrade = 0.0,
            cooldownReductionPerUpgrade = 0.0,
            maxUpgrades = 10
        )

        val auraFrameDur = 0.08
        val auraTicks = 50
        val skill2AuraDuration = auraFrameDur * auraTicks

        return HeroConfig(
            id = ID,
            displayName = "Wanderer Magician",
            maxHealth = 200.0,
            maxMana = 110.0,
            manaRegen = 5.5,
            healthRegen = 0.45,
            runningSpeed = 205.0,
            charSpeed = 1.0,
            animFrameDuration = 0.11,
            jumpForce = -600.0,
            gravity = Constants.GRAVITY,
            skill2RepeatCount = 1,
            basicAttackSkill = basicAttackSkill,
            skill1Config = skill1Config,
            skill2Config = skill2Config,
            skill3Config = skill3Config,
            skill4Config = skill4Config,
            healingSkillConfig = healingSkillConfig,
            maxHealthSkillConfig = maxHealthSkillConfig,
            basicAttackTuning = BasicAttackTuning(
                frameDuration = 0.07,
                moving = true,
                speed = 420.0,
                hitboxScaleX = 0.75,
                hitboxScaleY = 0.75,
                repeatAnimation = 1,
                displayScale = 2.2,
                offsetX = -40.0,
                offsetY = 20.0
            ),
            skill1Tuning = SkillAttackTuning(
                frameDuration = 0.07,
                moving = true,
                projectileSpeed = 400.0,
                hitboxScaleX = 0.8,
                hitboxScaleY = 0.8,
                repeatAnimation = 1,
                displayScale = 2.8,
                offsetX = -130.0,
                offsetY = 22.0
            ),
            skill2Tuning = SkillAttackTuning(
                frameDuration = auraFrameDur,
                moving = false,
                projectileSpeed = 0.0,
                hitboxScaleX = 1.25,
                hitboxScaleY = 1.1,
                repeatAnimation = 1,
                displayScale = 0.55,
                offsetX = 0.0,
                offsetY = -65.0
            ),
            skill3Tuning = SkillAttackTuning(
                frameDuration = 0.065,
                moving = false,
                projectileSpeed = 0.0,
                hitboxScaleX = 0.55,
                hitboxScaleY = 0.55,
                repeatAnimation = 1,
                displayScale = 0.35,
                offsetX = -18.0,
                offsetY = 12.0
            ),
            skill4Tuning = SkillAttackTuning(
                frameDuration = 0.08,
                moving = true,
                projectileSpeed = 260.0,
                hitboxScaleX = 0.65,
                hitboxScaleY = 0.65,
                repeatAnimation = 50,
                displayScale = 1.15,
                offsetX = 35.0,
                offsetY = -55.0
            ),
            bodyAnimRules = HeroBodyAnimRules(
                skill1UsesAttackAnimForBody = true,
                skill2HealingAuraFollowPlayer = true,
                skill3BodyUsesCastSheet = true,
                skill4ChargeBeforeBall = true
            ),
            skill2AuraTotalHeal = 20.0,
            skill2AuraDurationSeconds = skill2AuraDuration,
            skill4ChargeFrameDuration = 0.06,
            skill4BallMaxDurationSeconds = 14.0
        )
    }
}
