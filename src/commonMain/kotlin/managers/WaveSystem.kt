// =============================================================================
// WaveSystem.kt â€” Hybrid Wave Progression System
//
// DROP-IN REPLACEMENT for WaveSchedule.kt.
// Fully backward-compatible: SpawnEvent is unchanged, EnemySpawner is unchanged.
//
// Architecture
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  WaveSystem          â€” top-level entry point (mirrors WaveSchedule.apply)
//  EnemyRole           â€” semantic role enum used by the auto-scaler
//  EnemyProfile        â€” data-driven descriptor for every enemy type
//  EnemyRegistry       â€” single place to register / add enemies
//  WavePhase           â€” time window with difficulty scalar (manual or auto)
//  AutoWaveGenerator   â€” generates SpawnEvents after manual phases end
//
// Usage
// â”€â”€â”€â”€â”€
//  Replace  WaveSchedule.apply(spawner)
//  with     WaveSystem.apply(spawner)
//
// Adding a new enemy
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  1. Add an entry to EnemyRegistry.profiles (name, role, stats)
//  2. Done â€” the auto-scaler picks it up automatically based on its role.
// =============================================================================

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// ROLE TAXONOMY
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
package managers

import korlibs.math.*
import kotlin.random.*
import entities.EnemySpawner
import entities.SpawnEvent
enum class EnemyRole {
    BASIC,      // Filler melee â€” always fair to spawn in groups
    RANGED,     // Ranged pressure â€” limit simultaneous count
    ELITE,      // Stronger melee â€” one or two at a time
    FAST,       // Speed chaser (wolf1) â€” dangerous in packs; cap group size
    BRUISER,    // Tanky slow hitter (wolf2) â€” limit per wave
    MINI_BOSS   // High-threat rare unit (wolf3, skeleton_boss) â€” never spammed
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// DATA-DRIVEN ENEMY DESCRIPTOR
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

data class EnemyProfile(
    val name: String,
    val role: EnemyRole,

    // Difficulty weight: used when selecting enemies proportionally.
    // Higher = rarer / harder.  Range: 1â€“10 (tunable).
    val difficultyWeight: Double,

    // Minimum game-time (seconds) before this enemy can appear in auto-waves.
    val unlockTime: Double,

    // Preferred spawn X position (screen right side).
    val spawnX: Double,

    // Max count the auto-generator will ever put in one SpawnEvent for this enemy.
    val maxGroupSize: Int,

    // Spacing used when groupSize > 1.
    val groupSpacing: Double = 40.0
)

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// ENEMY REGISTRY â€” single place to add / configure enemies
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

object EnemyRegistry {

    /**
     * Add new enemies here. Order doesn't matter â€” the scaler queries by role.
     *
     * difficultyWeight guidelines
     *   1â€“2  : always-fair filler (skeleton)
     *   3â€“4  : moderate threat (archer, spearman, wolf1)
     *   5â€“6  : meaningful threat (wolf2)
     *   7â€“8  : serious threat (skeleton_boss)
     *   9â€“10 : endgame only (wolf3)
     */
    val profiles: List<EnemyProfile> = listOf(
        // ── Skeletons ───────────────────────────────────────
        EnemyProfile(
            name            = "skeleton",
            role            = EnemyRole.BASIC,
            difficultyWeight = 1.0,
            unlockTime      = 0.0,
            spawnX          = 1000.0,
            maxGroupSize    = 3,
            groupSpacing    = 40.0
        ),
        EnemyProfile(
            name            = "skeleton_archer",
            role            = EnemyRole.RANGED,
            difficultyWeight = 2.0,
            unlockTime      = 0.0,
            spawnX          = 1050.0,
            maxGroupSize    = 2,
            groupSpacing    = 50.0
        ),
        EnemyProfile(
            name            = "skeleton_spearman",
            role            = EnemyRole.ELITE,
            difficultyWeight = 3.0,
            unlockTime      = 10.0,
            spawnX          = 950.0,
            maxGroupSize    = 2,
            groupSpacing    = 50.0
        ),
        EnemyProfile(
            name            = "skeleton_boss",
            role            = EnemyRole.MINI_BOSS,
            difficultyWeight = 8.0,
            unlockTime      = 60.0,
            spawnX          = 640.0,
            maxGroupSize    = 1,
            groupSpacing    = 0.0
        ),

        // ── Kobold (uses same attack system) ────────────────
        EnemyProfile(
            name            = "kobold",
            role            = EnemyRole.ELITE,
            difficultyWeight = 3.0,
            unlockTime      = 30.0,
            spawnX          = 1000.0,
            maxGroupSize    = 3,
            groupSpacing    = 40.0
        ),

        // ── Flying Eye & Goblin ───────────────────────────────
        EnemyProfile(
            name            = "flying_eye",
            role            = EnemyRole.RANGED,
            difficultyWeight = 2.0,
            unlockTime      = 0.0,
            spawnX          = 1050.0,
            maxGroupSize    = 2,
            groupSpacing    = 60.0
        ),
        EnemyProfile(
            name            = "goblin",
            role            = EnemyRole.RANGED,
            difficultyWeight = 2.0,
            unlockTime      = 0.0,
            spawnX          = 950.0,
            maxGroupSize    = 3,
            groupSpacing    = 50.0
        ),

        // ── Wolves ──────────────────────────────────────────
        EnemyProfile(
            name            = "wolf1",
            role            = EnemyRole.FAST,
            difficultyWeight = 4.0,
            unlockTime      = 30.0,
            spawnX          = 950.0,
            maxGroupSize    = 2,
            groupSpacing    = 60.0
        ),
        EnemyProfile(
            name            = "wolf2",
            role            = EnemyRole.BRUISER,
            difficultyWeight = 5.0,
            unlockTime      = 60.0,
            spawnX          = 900.0,
            maxGroupSize    = 1,
            groupSpacing    = 0.0
        ),
        EnemyProfile(
            name            = "wolf3",
            role            = EnemyRole.MINI_BOSS,
            difficultyWeight = 8.0,
            unlockTime      = 100.0,
            spawnX          = 900.0,
            maxGroupSize    = 1,
            groupSpacing    = 0.0
        )
    )

    // Convenience accessors
    fun byName(name: String): EnemyProfile? = profiles.firstOrNull { it.name == name }
    fun byRole(role: EnemyRole): List<EnemyProfile> = profiles.filter { it.role == role }
    fun available(atTime: Double): List<EnemyProfile> = profiles.filter { it.unlockTime <= atTime }
}

data class Wave(
    val duration: Double,
    val events: List<SpawnEvent>,
    val name: String = ""
) {
    fun withStart(startTime: Double): List<SpawnEvent> = events.map { it.copy(time = startTime + it.time) }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// WAVE PHASE â€” describes a time window with a difficulty scalar
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

data class WavePhase(
    val startTime: Double,
    val endTime:   Double,
    // 0.0 = trivial, 1.0 = late-game baseline.
    // Manual phases set this for documentation purposes only.
    // The AutoWaveGenerator uses it as the starting difficulty for auto-waves.
    val difficulty: Double
)

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// AUTO WAVE GENERATOR
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Generates SpawnEvents dynamically after the manual phases end.
 *
 * Scaling rules (all tunable via constructor params):
 *
 *  spawnInterval  = baseInterval  * (1 / (1 + intervalDecayRate * t))
 *  groupSizeBonus = floor(groupScaleRate * t)   (capped by profile.maxGroupSize)
 *  difficulty(t)  = startDifficulty + difficultyGainPerSecond * t
 *
 * Enemy selection:
 *  1. Filter by unlockTime â‰¤ current time.
 *  2. Weight by role budget (limits how many MINI_BOSS / BRUISER slots open at once).
 *  3. Pick proportionally by inverted difficultyWeight so weaker enemies remain common.
 *
 * Breathing windows:
 *  Every breathingWindowInterval seconds, add a forced quiet gap of breathingWindowDuration.
 */
class AutoWaveGenerator(
    private val startTime:             Double = 240.0,   // when auto-waves begin (seconds)
    private val startDifficulty:       Double = 1.0,     // matches last manual phase difficulty
    private val difficultyGainPerSec:  Double = 0.004,   // +1 difficulty every ~250 s
    private val baseInterval:          Double = 12.0,    // initial seconds between spawn events
    private val intervalDecayRate:     Double = 0.0015,  // how fast interval shrinks
    private val minInterval:           Double = 4.0,     // floor on spawn interval
    private val groupScaleRate:        Double = 0.003,   // group size +1 every ~333 s
    private val breathingWindowEvery:  Double = 90.0,    // quiet gap every N seconds
    private val breathingWindowLength: Double = 8.0,     // how long the gap lasts
    private val durationToGenerate:    Double = 600.0    // how far ahead to pre-generate (10 min)
) {

    // Role budgets: max proportion of a wave that can be a given role (0â€“1).
    // Reduces as difficulty rises â€” more dangerous enemies become more common.
    private val roleBudget: Map<EnemyRole, Double> = mapOf(
        EnemyRole.BASIC     to 1.0,   // always unrestricted
        EnemyRole.RANGED    to 0.8,
        EnemyRole.ELITE     to 0.7,
        EnemyRole.FAST      to 0.4,
        EnemyRole.BRUISER   to 0.25,
        EnemyRole.MINI_BOSS to 0.10
    )

    // Track how many of each role we've emitted in recent history (rolling window)
    private val recentRoleCount = mutableMapOf<EnemyRole, Int>()
    private val roleCooldownUntil = mutableMapOf<EnemyRole, Double>()

    // Mandatory cooldowns (seconds) between spawns of that role
    private val roleCooldown: Map<EnemyRole, Double> = mapOf(
        EnemyRole.MINI_BOSS to 60.0,
        EnemyRole.BRUISER   to 30.0,
        EnemyRole.FAST      to 20.0,
        EnemyRole.ELITE     to 10.0,
        EnemyRole.RANGED    to 5.0,
        EnemyRole.BASIC     to 0.0
    )

    fun generate(): List<SpawnEvent> {
        val events = mutableListOf<SpawnEvent>()
        var cursor = startTime         // current time pointer
        val endTime = startTime + durationToGenerate
        var nextBreathingAt = startTime + breathingWindowEvery

        while (cursor < endTime) {
            val elapsed = cursor - startTime
            val difficulty = startDifficulty + difficultyGainPerSec * elapsed

            // â”€â”€ Breathing window â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            if (cursor >= nextBreathingAt) {
                cursor += breathingWindowLength
                nextBreathingAt = cursor + breathingWindowEvery
                continue
            }

            // â”€â”€ Select enemy â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            val candidate = selectEnemy(cursor, difficulty)

            if (candidate == null) {
                cursor += 5.0
                continue
            }

            // â”€â”€ Group size â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            val groupBonus = (groupScaleRate * elapsed).toInt()
            val groupSize  = (1 + groupBonus).coerceAtMost(candidate.maxGroupSize)

            // â”€â”€ Emit SpawnEvent â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            events.add(
                SpawnEvent(
                    time      = cursor,
                    enemyType = candidate.name,
                    x         = Random.nextDouble(100.0, 1000.0),
                    count     = groupSize,
                    offsetX   = if (groupSize > 1) candidate.groupSpacing else 0.0
                )
            )

            // Update role cooldown
            roleCooldownUntil[candidate.role] = cursor + (roleCooldown[candidate.role] ?: 0.0)

            // â”€â”€ Advance time â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            val interval = (baseInterval / (1.0 + intervalDecayRate * elapsed))
                .coerceAtLeast(minInterval)
            // Vary slightly so it feels organic (Â±20 %)
            val jitter = interval * 0.2 * (Math.random() * 2.0 - 1.0)
            cursor += (interval + jitter).coerceAtLeast(minInterval * 0.8)
        }

        return events
    }

    /**
     * Weighted enemy selection.
     *
     * Strategy:
     *  - Filter to unlocked enemies whose role cooldown has expired.
     *  - Assign weight = 1 / difficultyWeight so weaker enemies remain common.
     *  - Sample proportionally.
     */
    private fun selectEnemy(atTime: Double, difficulty: Double): EnemyProfile? {
        val eligible = EnemyRegistry.available(atTime).filter { profile ->
            val cooldownOk = (roleCooldownUntil[profile.role] ?: 0.0) <= atTime
            // Only unlock MINI_BOSS / BRUISER after difficulty reaches a threshold
            val diffOk = when (profile.role) {
                EnemyRole.MINI_BOSS -> difficulty >= 1.5
                EnemyRole.BRUISER   -> difficulty >= 1.2
                EnemyRole.FAST      -> difficulty >= 1.0
                else                -> true
            }
            cooldownOk && diffOk
        }
        if (eligible.isEmpty()) return null

        // Invert weight so easier enemies are chosen more often
        val totalWeight = eligible.sumOf { 1.0 / it.difficultyWeight }
        var rand = Math.random() * totalWeight
        for (profile in eligible) {
            rand -= 1.0 / profile.difficultyWeight
            if (rand <= 0.0) return profile
        }
        return eligible.last()
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// WAVE SYSTEM â€” top-level entry point
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Drop-in replacement for WaveSchedule.
 *
 * Manual phases (exact same SpawnEvent syntax you already know):
 *   Phase 1 â€” Learning Phase:  t =   0 â€“  60 s  (difficulty 0.2)
 *   Phase 1.1 â€” Starting:      t =  60 â€“ 120 s  (difficulty 0.4)
 *   Phase 2 â€” Pressure:        t = 120 â€“ 180 s  (difficulty 0.7)
 *   Phase 3 â€” Endgame:         t = 180 â€“ 240 s  (difficulty 1.0)
 *
 * Auto-phase:                  t = 240+ s        (auto-scaler takes over)
 */
object WaveSystem {

    // Phase boundaries â€” for documentation and future use by UI / analytics.
    // Manual waves are explicitly defined below; auto-phase begins after wave 6.
    val phases = listOf(
        WavePhase(startTime =   0.0, endTime =  60.0, difficulty = 0.2),
        WavePhase(startTime =  60.0, endTime = 150.0, difficulty = 0.4),
        WavePhase(startTime = 150.0, endTime = 240.0, difficulty = 0.7),
        WavePhase(startTime = 240.0, endTime = 330.0, difficulty = 1.0),
        WavePhase(startTime = 330.0, endTime = 420.0, difficulty = 1.2),
        WavePhase(startTime = 420.0, endTime = 510.0, difficulty = 1.5)
    )

    private val manualWaves = listOf(
        Wave(
            duration = 60.0,
            name = "Wave 1",
            events = listOf(
                SpawnEvent(1.0, "skeleton", 900.0),
                SpawnEvent(5.0, "skeleton", 1100.0),
                SpawnEvent(10.0, "skeleton", 900.0),
                SpawnEvent(12.0, "skeleton_archer", 1000.0),
                SpawnEvent(18.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(20.0, "skeleton_archer", 1000.0),
                SpawnEvent(26.0, "skeleton_spearman", 950.0),
                SpawnEvent(35.0, "skeleton", 1000.0),
                SpawnEvent(40.0, "skeleton", 1000.0, 2, 40.0),
                SpawnEvent(40.0, "skeleton_archer", 1000.0),
                SpawnEvent(50.0, "skeleton_spearman", 900.0),
                SpawnEvent(50.0, "skeleton_archer", 1000.0),
                SpawnEvent(50.0, "skeleton", 1000.0),
                SpawnEvent(60.0, "skeleton_boss", 640.0)
//                SpawnEvent(1.0, "goblin", 900.0),
//                SpawnEvent(1.0, "kobold", 500.0),
//                SpawnEvent(1.0, "flying_eye", 900.0),
//
//                SpawnEvent(10.0, "goblin", 900.0, 3, 50.0),
//                SpawnEvent(10.0, "kobold", 500.0, 2, 50.0),
//                SpawnEvent(10.0, "flying_eye", 900.0, 4, 70.0),

            )
        ),
        Wave(
            duration = 90.0,
            name = "Wave 2",
            events = listOf(
                SpawnEvent(1.0, "skeleton", 900.0),
                SpawnEvent(5.0, "skeleton", 1100.0),
                SpawnEvent(10.0, "skeleton", 900.0),
                SpawnEvent(12.0, "skeleton_archer", 1000.0),
                SpawnEvent(18.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(20.0, "skeleton_archer", 1000.0),
                SpawnEvent(26.0, "skeleton_spearman", 950.0),
                SpawnEvent(35.0, "skeleton", 1000.0),
                SpawnEvent(40.0, "skeleton", 1000.0, 2, 40.0),
                SpawnEvent(40.0, "skeleton_archer", 1000.0),
                SpawnEvent(50.0, "skeleton_spearman", 900.0),
                SpawnEvent(50.0, "skeleton_archer", 1000.0),
                SpawnEvent(50.0, "skeleton", 1000.0),
                SpawnEvent(60.0, "skeleton_boss", 640.0),
                SpawnEvent(65.0, "skeleton_spearman", 900.0),
                SpawnEvent(70.0, "skeleton_archer", 1000.0, 2, 40.0),
                SpawnEvent(70.0, "skeleton", 1000.0),
                SpawnEvent(80.0, "skeleton", 1000.0, 2, 50.0)
            )
        ),
        Wave(
            duration = 90.0,
            name = "Wave 3",
            events = listOf(
                SpawnEvent(1.0, "skeleton", 1000.0, 2, 20.0),
                SpawnEvent(5.0, "wolf1", 950.0),
                SpawnEvent(10.0, "skeleton_archer", 1000.0, 2, 40.0),
                SpawnEvent(12.0, "skeleton", 1000.0, 2, 20.0),
                SpawnEvent(20.0, "skeleton_spearman", 1000.0),
                SpawnEvent(20.0, "wolf1", 950.0),
                SpawnEvent(30.0, "skeleton", 1000.0),
                SpawnEvent(35.0, "skeleton_archer", 1000.0, 2, 40.0),
                SpawnEvent(35.0, "wolf1", 900.0),
                SpawnEvent(40.0, "skeleton_spearman", 950.0),
                SpawnEvent(40.0, "skeleton", 1000.0, 2, 20.0),
                SpawnEvent(48.0, "skeleton", 1000.0),
                SpawnEvent(50.0, "skeleton_boss", 640.0 * 1.2),
                SpawnEvent(55.0, "wolf1", 900.0),
                SpawnEvent(80.0, "wolf1", 900.0)
            )
        ),
        Wave(
            duration = 90.0,
            name = "Wave 4",
            events = listOf(
                SpawnEvent(1.0, "skeleton_spearman", 900.0),
                SpawnEvent(2.0, "skeleton_archer", 1000.0, 2, 40.0),
                SpawnEvent(5.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(7.0, "skeleton", 100.0, 2, 60.0),
                SpawnEvent(10.0, "skeleton", 100.0, 1, 60.0),
                SpawnEvent(11.0, "skeleton_archer", 1200.0, 1, 50.0),
                SpawnEvent(20.0, "skeleton", 100.0, 2, 60.0),
                SpawnEvent(20.0, "wolf1", 90.0),
                SpawnEvent(20.0, "skeleton_archer", 1000.0),
                SpawnEvent(30.0, "skeleton_spearman", 950.0, 1, 40.0),
                SpawnEvent(33.0, "skeleton", 90.0, 1, 40.0),
                SpawnEvent(36.0, "skeleton_archer", 100.0, 1, 30.0),
                SpawnEvent(37.0, "wolf1", 1100.0, 1, 80.0),
//                SpawnEvent(40.0, "wolf2", 1000.0, 1, 20.0),
                SpawnEvent(44.0, "skeleton_archer", 1000.0, 2, 30.0),
                SpawnEvent(47.0, "skeleton", 90.0, 2, 50.0),
                SpawnEvent(49.0, "skeleton_spearman", 90.0, 2, 100.0),
                SpawnEvent(50.0, "skeleton_boss", 640.0 * 0.6),
                SpawnEvent(53.0, "skeleton_spearman", 95.0),
                SpawnEvent(55.0, "skeleton_archer", 100.0, 1, 50.0),
                SpawnEvent(56.0, "skeleton", 90.0, 2, 40.0),
                SpawnEvent(58.0, "skeleton_archer", 100.0, 1, 50.0),
                SpawnEvent(60.0, "skeleton", 90.0, 1, 40.0),
                SpawnEvent(70.0, "wolf1", 50.0, 2, 100.0),
                SpawnEvent(75.0, "wolf2", 50.0),
                SpawnEvent(80.0, "wolf3", 50.0),
            )
        ),
        Wave(
            duration = 90.0,
            name = "Wave 5",
            events = listOf(
                SpawnEvent(2.0, "skeleton", 1000.0, 1, 50.0),
                SpawnEvent(2.0, "skeleton", 100.0, 1, 50.0),
                SpawnEvent(4.0, "skeleton_archer", 1000.0, 1, 50.0),
                SpawnEvent(4.0, "skeleton_archer", 100.0, 1, 50.0),
                SpawnEvent(7.0, "skeleton_spearman", 1000.0, 1, 50.0),
                SpawnEvent(7.0, "skeleton_spearman", 100.0, 1, 50.0),
                SpawnEvent(7.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(7.0, "skeleton", 100.0, 2, 50.0),
                SpawnEvent(15.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(15.0, "skeleton", 100.0, 2, 50.0),
                SpawnEvent(18.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(18.0, "skeleton", 100.0, 2, 50.0),
                SpawnEvent(20.0, "skeleton", 1000.0, 1, 50.0),
                SpawnEvent(20.0, "skeleton", 100.0, 1, 50.0),
                SpawnEvent(20.0, "skeleton_archer", 1000.0, 1, 50.0),
                SpawnEvent(20.0, "skeleton_archer", 100.0, 1, 50.0),
                SpawnEvent(20.0, "skeleton_spearman", 1000.0, 1, 50.0),
                SpawnEvent(20.0, "skeleton_spearman", 100.0, 1, 50.0),
                SpawnEvent(30.0, "skeleton", 1000.0, 1, 50.0),
                SpawnEvent(30.0, "skeleton", 100.0, 1, 50.0),
                SpawnEvent(30.0, "skeleton_archer", 1000.0, 1, 50.0),
                SpawnEvent(30.0, "skeleton_archer", 100.0, 1, 50.0),
                SpawnEvent(30.0, "skeleton_spearman", 1000.0, 1, 50.0),
                SpawnEvent(30.0, "skeleton_spearman", 100.0, 1, 50.0),
                SpawnEvent(32.0, "skeleton", 1000.0, 1, 50.0),
                SpawnEvent(32.0, "skeleton", 100.0, 1, 50.0),
                SpawnEvent(34.0, "skeleton_archer", 1000.0, 1, 50.0),
                SpawnEvent(34.0, "skeleton_archer", 100.0, 1, 50.0),
                SpawnEvent(37.0, "skeleton_spearman", 1000.0, 1, 50.0),
                SpawnEvent(37.0, "skeleton_spearman", 100.0, 1, 50.0),
                SpawnEvent(37.0, "skeleton", 1000.0, 1, 50.0),
                SpawnEvent(37.0, "skeleton", 100.0, 1, 50.0),
                SpawnEvent(55.0, "skeleton_boss", 900.0),
                SpawnEvent(62.0, "skeleton", 1000.0, 1, 50.0),
                SpawnEvent(62.0, "skeleton", 100.0, 1, 50.0),
                SpawnEvent(64.0, "skeleton_archer", 1000.0, 1, 50.0),
                SpawnEvent(64.0, "skeleton_archer", 100.0, 1, 50.0),
                SpawnEvent(67.0, "skeleton_spearman", 1000.0, 1, 50.0),
                SpawnEvent(67.0, "skeleton_spearman", 100.0, 1, 50.0),
                SpawnEvent(67.0, "skeleton", 1000.0, 1, 50.0),
                SpawnEvent(67.0, "skeleton", 100.0, 1, 50.0),
                SpawnEvent(70.0, "skeleton_archer", 900.0, 2, 50.0),
                SpawnEvent(72.0, "skeleton", 900.0, 2, 50.0),
                SpawnEvent(72.0, "skeleton_spearman", 900.0, 2, 50.0),
                SpawnEvent(75.0, "skeleton_archer", 900.0, 2, 50.0),
                SpawnEvent(76.0, "skeleton", 900.0, 2, 50.0),
                SpawnEvent(79.0, "skeleton", 900.0, 2, 50.0)
            )
        ),
        Wave(
            duration = 90.0,
            name = "Wave 6 (Control Phase)",
            events = listOf(

                // ── Phase 1: Warm-up ──
                SpawnEvent(1.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(4.0, "skeleton_archer", 1000.0, 1, 50.0),

                // ── Phase 2: Add flying pressure ──
                SpawnEvent(10.0, "flying_eye", 1050.0),
                SpawnEvent(10.0, "flying_eye", 50.0),
                SpawnEvent(12.0, "skeleton", 150.0, 2, 40.0),
                SpawnEvent(12.0, "skeleton", 900.0, 2, 40.0),
                SpawnEvent(15.0, "skeleton_spearman", 1000.0, 1, 50.0),
                SpawnEvent(15.0, "skeleton_spearman", 100.0, 1, 50.0),
                SpawnEvent(18.0, "skeleton_spearman", 1000.0, 1, 50.0),
                SpawnEvent(18.0, "skeleton_spearman", 100.0, 1, 50.0),


                // ── Phase 3: Introduce goblin (SAFE) ──
                SpawnEvent(20.0, "goblin", 700.0),
                SpawnEvent(22.0, "skeleton_spearman", 800.0, 1, 50.0),
                SpawnEvent(25.0, "skeleton_spearman", 800.0, 1, 50.0),
                SpawnEvent(27.0, "skeleton_archer", 1000.0, 2, 40.0),


                // ── Phase 4: Add melee pressure ──
                SpawnEvent(35.0, "kobold", 900.0),
                SpawnEvent(36.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(39.0, "kobold", 900.0),


                // ── Phase 5: Controlled mix ──
                SpawnEvent(40.0, "goblin", 900.0),
                SpawnEvent(42.0, "flying_eye", 1050.0),
                SpawnEvent(45.0, "kobold", 1000.0),

                // ── Breathing window ──
                // (no spawns 50–55)

                // ── Phase 6: Slight spike ──
                SpawnEvent(55.0, "skeleton", 1000.0, 3, 40.0),
                SpawnEvent(58.0, "skeleton_archer", 1000.0, 2, 50.0),

                // ── Boss moment ──
                SpawnEvent(65.0, "skeleton_boss", 640.0),

                // ── Final mix (still fair) ──
                SpawnEvent(70.0, "goblin", 900.0),
                SpawnEvent(72.0, "kobold", 1000.0),
                SpawnEvent(75.0, "flying_eye", 1050.0),

                // ── End stabilization ──
                SpawnEvent(82.0, "skeleton", 1000.0, 2, 50.0)

            )
        ),
        Wave(
            duration = 90.0,
            name = "Wave 7 (Punishment Phase)",
            events = listOf(

                // ── Phase 1 ──
                SpawnEvent(1.0, "skeleton", 1000.0, 2, 50.0),
                SpawnEvent(4.0, "skeleton_archer", 1000.0, 2, 50.0),

                // ── Introduce wolf (no goblin yet) ──
                SpawnEvent(10.0, "wolf1", 900.0),
                SpawnEvent(12.0, "skeleton", 1000.0, 2, 40.0),

                // ── Add goblin AFTER wolf resolves ──
                SpawnEvent(18.0, "goblin", 900.0),

                // ── Mid pressure ──
                SpawnEvent(25.0, "kobold", 1000.0),
                SpawnEvent(28.0, "flying_eye", 1050.0),

                // ── Boss ──
                SpawnEvent(35.0, "skeleton_boss", 640.0),

                // ── Controlled chaos ──
                SpawnEvent(45.0, "wolf1", 900.0),
                SpawnEvent(48.0, "skeleton", 1000.0, 3, 40.0),

                // Goblin delayed
                SpawnEvent(52.0, "goblin", 900.0),

                // ── Heavy push ──
                SpawnEvent(60.0, "wolf2", 850.0),
                SpawnEvent(63.0, "skeleton_archer", 1000.0, 2, 50.0),

                // ── Final spike ──
                SpawnEvent(70.0, "wolf1", 900.0, 2, 40.0),
                SpawnEvent(73.0, "kobold", 1000.0, 2, 40.0),

                // Goblin last (safe timing)
                SpawnEvent(78.0, "goblin", 900.0),

                // ── End ──
                SpawnEvent(85.0, "skeleton", 1000.0, 2, 50.0)

            )
        ),
        Wave(
            duration = 90.0,
            name = "Wave 8 (High Pressure Control)",
            events = listOf(

                // ─────────────────────────────
                // Early pressure starts immediately (no safe opening)
                // ─────────────────────────────
                SpawnEvent(1.0, "kobold", 900.0),
                SpawnEvent(2.5, "flying_eye", 1050.0),
                SpawnEvent(3.5, "kobold", 950.0),
                SpawnEvent(5.0, "goblin", 900.0),

                // ─────────────────────────────
                // Early overlap (2 threats at once begins early)
                // ─────────────────────────────
                SpawnEvent(8.0, "kobold", 900.0, 2, 40.0),
                SpawnEvent(9.5, "flying_eye", 1050.0),
                SpawnEvent(11.0, "goblin", 950.0),

                // ─────────────────────────────
                // Mid-phase compression (less spacing, more stacking)
                // ─────────────────────────────
                SpawnEvent(15.0, "kobold", 900.0),
                SpawnEvent(16.0, "kobold", 950.0),
                SpawnEvent(17.5, "flying_eye", 1050.0),

                SpawnEvent(20.0, "goblin", 900.0),
                SpawnEvent(21.0, "kobold", 900.0),
                SpawnEvent(22.0, "flying_eye", 1050.0),
                SpawnEvent(23.0, "kobold", 950.0),

                // ─────────────────────────────
                // Pressure spike cluster (multi-threat overlap window)
                // ─────────────────────────────
                SpawnEvent(28.0, "goblin", 900.0),
                SpawnEvent(29.0, "kobold", 900.0, 2, 40.0),
                SpawnEvent(30.0, "flying_eye", 1050.0),
                SpawnEvent(31.0, "kobold", 950.0),

                SpawnEvent(34.0, "kobold", 900.0),
                SpawnEvent(35.0, "goblin", 900.0),
                SpawnEvent(36.0, "flying_eye", 1050.0),

                // ─────────────────────────────
                // Minimal breathing window (very short)
                // ─────────────────────────────
                // 40–43s (slightly reduced recovery compared to previous wave)

                // ─────────────────────────────
                // Post-breathing punish (immediate re-engage)
                // ─────────────────────────────
                SpawnEvent(43.0, "kobold", 900.0),
                SpawnEvent(44.0, "goblin", 900.0),
                SpawnEvent(45.0, "kobold", 950.0),
                SpawnEvent(46.0, "flying_eye", 1050.0),

                // ─────────────────────────────
                // Late-phase sustained pressure (no downtime stacking)
                // ─────────────────────────────
                SpawnEvent(50.0, "kobold", 900.0, 2, 40.0),
                SpawnEvent(52.0, "flying_eye", 1050.0),
                SpawnEvent(54.0, "goblin", 900.0),

                SpawnEvent(57.0, "kobold", 900.0),
                SpawnEvent(58.0, "kobold", 950.0),
                SpawnEvent(59.0, "flying_eye", 1050.0),

                SpawnEvent(62.0, "goblin", 900.0),
                SpawnEvent(64.0, "kobold", 900.0),
                SpawnEvent(66.0, "flying_eye", 1050.0),

                // ─────────────────────────────
                // Final pressure stack (continuous threat layering)
                // ─────────────────────────────
                SpawnEvent(72.0, "kobold", 900.0, 2, 40.0),
                SpawnEvent(73.5, "goblin", 900.0),
                SpawnEvent(75.0, "flying_eye", 1050.0),
                SpawnEvent(76.0, "kobold", 950.0),

                SpawnEvent(80.0, "kobold", 900.0),
                SpawnEvent(82.0, "goblin", 900.0),
                SpawnEvent(84.0, "flying_eye", 1050.0),

                SpawnEvent(88.0, "kobold", 900.0, 2, 40.0)
            )
        )
    )

    private val manualWaveStartTimes: List<Double> by lazy {
        var current = 0.0
        manualWaves.map { wave ->
            val start = current
            current += wave.duration
            start
        }
    }

    private val manualSchedule: List<SpawnEvent> by lazy {
        manualWaves.flatMapIndexed { index, wave ->
            wave.withStart(manualWaveStartTimes[index])
        }.sortedBy { it.time }
    }

    private val autoPhaseStart: Double by lazy {
        manualWaveStartTimes.last() + manualWaves.last().duration
    }

    fun getWaveNumber(elapsedTime: Double): Int {
        if (elapsedTime >= autoPhaseStart) return manualWaves.size + 1
        return manualWaveStartTimes.indexOfLast { it <= elapsedTime } + 1
    }

    fun apply(spawner: EnemySpawner) {
        // Schedule manual waves
        spawner.schedule(*manualSchedule.toTypedArray())

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // AUTO-PHASE â€” pre-generate and schedule endless waves
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        val autoEvents = AutoWaveGenerator(
            startTime            = autoPhaseStart,
            startDifficulty      = 0.5,     // matches Wave 5 difficulty
            difficultyGainPerSec = 0.004,   // +1 difficulty per ~4 min â€” smooth climb
            baseInterval         = 5.0,    // start at one spawn every 12 s
            intervalDecayRate    = 0.0015,  // interval shrinks to ~4 s by t = 700 s
            minInterval          = 2.0,
            groupScaleRate       = 0.008,
            breathingWindowEvery = 90.0,    // 8-second breather every 90 seconds
            breathingWindowLength = 8.0,
            durationToGenerate   = 600.0    // generate 10 minutes of auto-waves
        ).generate()

        spawner.scheduleList(autoEvents)
    }
}

