package entities.heroes

import utils.Constants
import utils.SkillConfig

object FireWizardHero {
    const val ID = "fire_wizard"

    fun createConfig(): HeroConfig = createFireWizardPlaceholderStats(ID, "Fire Wizard")
}

/**
 * Shared stats/tuning for the Fire Wizard kit. [WandererMagicianHero] reuses this until real assets/config exist.
 */
internal fun createFireWizardPlaceholderStats(
    id: String,
    displayName: String
): HeroConfig {
    val basicAttackSkill = SkillConfig(
        name = "Basic Attack",
        cooldownMax = 0.0,
        manaCost = 0,
        damage = 5.0,
        unlockLevel = 1,
        requiresPointUnlock = false,
        damagePerUpgrade = 1.0,
        maxUpgrades = 10
    )
    val skill1Config = SkillConfig(
        name = "Skill 1",
        cooldownMax = 4.0,
        manaCost = 20,
        damage = 10.0,
        unlockLevel = 4,
        damagePerUpgrade = 3.0,
        cooldownReductionPerUpgrade = 0.3,
        maxUpgrades = 5
    )
    val skill2Config = SkillConfig(
        name = "Skill 2",
        cooldownMax = 10.0,
        manaCost = 40,
        damage = 15.0,
        unlockLevel = 8,
        damagePerUpgrade = 3.0,
        cooldownReductionPerUpgrade = 0.5,
        maxUpgrades = 5
    )
    val skill3Config = SkillConfig(
        name = "Skill 3",
        cooldownMax = 12.0,
        manaCost = 50,
        damage = 30.0,
        unlockLevel = 12,
        damagePerUpgrade = 4.0,
        cooldownReductionPerUpgrade = 0.5,
        maxUpgrades = 5
    )
    val skill4Config = SkillConfig(
        name = "Skill 4",
        cooldownMax = 20.0,
        manaCost = 80,
        damage = 50.0,
        unlockLevel = 18,
        damagePerUpgrade = 5.0,
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

    val skill2RepeatCount = 5

    return HeroConfig(
        id = id,
        displayName = displayName,
        maxHealth = 200.0,
        maxMana = 100.0,
        manaRegen = 5.0,
        healthRegen = 0.5,
        runningSpeed = 200.0,
        charSpeed = 1.0,
        animFrameDuration = 0.12,
        jumpForce = -600.0,
        gravity = Constants.GRAVITY,
        skill2RepeatCount = skill2RepeatCount,
        basicAttackSkill = basicAttackSkill,
        skill1Config = skill1Config,
        skill2Config = skill2Config,
        skill3Config = skill3Config,
        skill4Config = skill4Config,
        healingSkillConfig = healingSkillConfig,
        maxHealthSkillConfig = maxHealthSkillConfig,
        basicAttackTuning = BasicAttackTuning(
            frameDuration = 0.08,
            moving = true,
            speed = 0.0,
            hitboxScaleX = 1.1,
            hitboxScaleY = 0.9,
            repeatAnimation = 2,
            displayScale = 2.0,
            offsetX = -20.0,
            offsetY = 30.0
        ),
        skill1Tuning = SkillAttackTuning(
            frameDuration = 0.07,
            moving = true,
            projectileSpeed = 400.0,
            hitboxScaleX = 0.8,
            hitboxScaleY = 0.8,
            repeatAnimation = 1,
            displayScale = 3.0,
            offsetX = -130.0,
            offsetY = 25.0
        ),
        skill2Tuning = SkillAttackTuning(
            frameDuration = 0.07,
            moving = false,
            projectileSpeed = 0.0,
            hitboxScaleX = 0.4,
            hitboxScaleY = 0.5,
            repeatAnimation = skill2RepeatCount,
            displayScale = 0.4,
            offsetX = -20.0,
            offsetY = 5.0
        ),
        skill3Tuning = SkillAttackTuning(
            frameDuration = 0.06,
            moving = false,
            projectileSpeed = 0.0,
            hitboxScaleX = 0.5,
            hitboxScaleY = 0.5,
            repeatAnimation = 1,
            displayScale = 0.3,
            offsetX = -20.0,
            offsetY = 15.0
        ),
        skill4Tuning = SkillAttackTuning(
            frameDuration = 0.08,
            moving = false,
            projectileSpeed = 0.0,
            hitboxScaleX = 1.0,
            hitboxScaleY = 1.0,
            repeatAnimation = 1,
            displayScale = 1.2,
            offsetX = 50.0,
            offsetY = -110.0
        )
    )
}
