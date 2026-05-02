/**
 * Tracks player XP, current level, and available upgrade points.
 *
 * Design principles:
 *  - XP requirements are defined per level in a table; missing levels fall back
 *    to defaultXpRequirement.
 *  - Level-up awards exactly 1 upgrade point.
 *  - Excess XP is carried over correctly (handles multi-level bursts).
 *  - At maxLevel the XP state freezes and xpProgress() returns 1.0.
 */
class PlayerProgress {

    // -------------------------------------------------------
    // CONFIGURATION
    // -------------------------------------------------------



    /**
     * XP required to go FROM level N to level N+1.
     * Levels not listed fall back to defaultXpRequirement.
     */
    val xpTable: Map<Int, Double> = mapOf(
        1 to 50.0,
        2 to 60.0,
        3 to 70.0,
        4 to 80.0,
        5 to 100.0,
        6 to 120.0,
        7 to 140.0,
        8 to 160.0,
        9 to 180.0,

        10 to 200.0,
        11 to 200.0,
        12 to 200.0,
        13 to 200.0
    )

    val maxLevel = xpTable.size

    val defaultXpRequirement = 30.0

    // -------------------------------------------------------
    // STATE  (read externally, written only here)
    // -------------------------------------------------------
    var level         = 1; private set
    var currentXp     = 0.0; private set
    var upgradePoints = 0; private set

    // -------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------

    fun isMaxLevel() = level >= maxLevel

    /** XP required to reach the NEXT level from the current one. */
    fun xpForNextLevel(): Double =
        if (isMaxLevel()) Double.MAX_VALUE
        else xpTable[level] ?: defaultXpRequirement

    /**
     * 0.0 – 1.0 fraction of progress toward the next level.
     * Returns 1.0 when at max level.
     */
    fun xpProgress(): Double =
        if (isMaxLevel()) 1.0
        else (currentXp / xpForNextLevel()).coerceIn(0.0, 1.0)

    // -------------------------------------------------------
    // ACTIONS
    // -------------------------------------------------------

    /**
     * Award XP to the player.
     * Handles multi-level bursts correctly: if a single award crosses
     * multiple level thresholds, all levels are applied.
     */
    fun addXp(amount: Double) {
        if (isMaxLevel()) return
        currentXp += amount
        while (!isMaxLevel() && currentXp >= xpForNextLevel()) {
            currentXp -= xpForNextLevel()
            level++
            upgradePoints++
        }
        // At max level, freeze XP display at full
        if (isMaxLevel()) currentXp = 0.0
    }

    /**
     * Spends one upgrade point.
     * Returns true if a point was available and consumed, false otherwise.
     */
    fun spendUpgradePoint(): Boolean {
        if (upgradePoints <= 0) return false
        upgradePoints--
        return true
    }

    /** Full reset — use when restarting a run. */
    fun reset() {
        level         = 1
        currentXp     = 0.0
        upgradePoints = 0
    }
}
