/**
 * Data-driven per-skill configuration.
 *
 * Holds both static tuning values (max cooldown, mana cost, base damage)
 * and mutable runtime state (remaining cooldown).
 *
 * Future-proof: damage, cooldownMax, and manaCost can be modified at
 * runtime for upgrades/leveling without touching any other file.
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

    /** Total base damage. Treat as dynamic — may change with upgrades. */
    var damage: Double = 0.0
) {
    // -------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------

    /** True when the cooldown has fully elapsed. */
    val isOffCooldown: Boolean get() = cooldownRemaining <= 0.0

    /** True when the player has enough mana AND the skill is off cooldown. */
    fun isUsable(currentMana: Double): Boolean =
        isOffCooldown && currentMana >= manaCost

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

    /** Reset runtime state (e.g. on scene restart). */
    fun resetCooldown() {
        cooldownRemaining = 0.0
    }
}
