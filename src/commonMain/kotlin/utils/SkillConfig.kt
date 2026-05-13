package utils

/**
 * Data-driven per-skill configuration.
 *
 * Holds both static tuning values (max cooldown, mana cost, base damage)
 * and mutable runtime state (remaining cooldown, upgrade count).
 *
 * NEW FIELDS
 * ──────────
 * unlockLevel              — minimum player level before you may spend a point to unlock / upgrade
 * requiresPointUnlock      — if true, player must spend 1 point to enable the slot after level gate (skills 1–4)
 * paidUnlock               — set true after spending first point on that slot
 * damagePerUpgrade         — how much damage increases per upgrade
 * cooldownReductionPerUpgrade — how many seconds cooldown shrinks per upgrade
 * minCooldown              — floor that cooldown can never go below after upgrades
 * maxUpgrades              — maximum number of times this skill can be upgraded
 * upgradeCount             — runtime: how many times this skill has been upgraded
 *
 * All new fields have safe defaults so existing creation sites compile unchanged.
 */
data class SkillConfig(
    /** Display / lookup name (e.g. "Fireball") */
    val name: String,

    /** Maximum cooldown in seconds. 0 = no cooldown. */
    var cooldownMax: Double = 0.0,

    /** Current remaining cooldown (ticks down each frame). */
    var cooldownRemaining: Double = 0.0,

    /** Mana required to cast. 0 = free. */
    var manaCost: Int = 0,

    /** Total base damage. Updated by upgrade(). */
    var damage: Double = 0.0,

    // -------------------------------------------------------
    // UNLOCK / UPGRADE FIELDS (new)
    // -------------------------------------------------------

    /** Player level required before you may unlock or use this skill (with paidUnlock). */
    val unlockLevel: Int = 1,

    /**
     * When true (default for slot skills), reaching [unlockLevel] is not enough — the player must
     * spend one skill point to enable the slot. Basic attack uses false.
     */
    val requiresPointUnlock: Boolean = true,

    /** After level gate, becomes true when the player spends one point to unlock this slot. */
    var paidUnlock: Boolean = false,

    /** Flat damage gained per upgrade. */
    val damagePerUpgrade: Double = 2.0,

    /** Seconds removed from cooldownMax per upgrade. Never goes below minCooldown. */
    val cooldownReductionPerUpgrade: Double = 0.2,

    /** Minimum cooldown after repeated reductions. */
    val minCooldown: Double = 0.5,

    /** Maximum number of upgrades allowed. */
    val maxUpgrades: Int = 5,

    /** Runtime counter — how many upgrades have been applied. */
    var upgradeCount: Int = 0,

    var heal:Double = 0.0

) {
    // -------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------

    /** True when the cooldown has fully elapsed. */
    val isOffCooldown: Boolean get() = cooldownRemaining <= 0.0

    /** True when the player has enough mana AND the skill is off cooldown. */
    fun isUsable(currentMana: Double): Boolean =
        isOffCooldown && currentMana >= manaCost

    /** Level gate only (UI: show LV# until this is true). */
    fun meetsLevelRequirement(playerLevel: Int): Boolean = playerLevel >= unlockLevel

    /** True when the skill can be cast (level + point unlock when required). */
    fun isUnlockedForUse(playerLevel: Int): Boolean =
        if (!requiresPointUnlock) meetsLevelRequirement(playerLevel)
        else meetsLevelRequirement(playerLevel) && paidUnlock

    /** Same as [meetsLevelRequirement] — kept for call sites that only check the level gate. */
    fun isUnlocked(playerLevel: Int): Boolean = meetsLevelRequirement(playerLevel)

    /** True if further upgrades are possible. */
    val canUpgrade: Boolean get() = upgradeCount < maxUpgrades

    // -------------------------------------------------------
    // ACTIONS
    // -------------------------------------------------------

    /** Call after successfully casting. Starts the cooldown. */
    fun startCooldown() {
        cooldownRemaining = cooldownMax
    }

    /** Call every frame with deltaTime to tick down the cooldown. */
    fun tickCooldown(dt: Double) {
        if (cooldownRemaining > 0.0) {
            cooldownRemaining = (cooldownRemaining - dt).coerceAtLeast(0.0)
        }
    }

    /**
     * Apply one upgrade:
     *  • damage  += damagePerUpgrade
     *  • cooldownMax -= cooldownReductionPerUpgrade (never below minCooldown)
     *
     * Safe to call multiple times; stops silently once maxUpgrades is reached.
     */
    fun upgrade() {
        if (!canUpgrade) return
        upgradeCount++
        damage      += damagePerUpgrade
        cooldownMax  = (cooldownMax - cooldownReductionPerUpgrade).coerceAtLeast(minCooldown)
        heal += damagePerUpgrade

    }

    /** Reset runtime state (e.g. on scene restart). */
    fun resetCooldown() {
        cooldownRemaining = 0.0
    }
}
