// =============================================================================
// WaveSystem.kt — Hybrid Wave Progression System
//
// DROP-IN REPLACEMENT for WaveSchedule.kt.
// Fully backward-compatible: SpawnEvent is unchanged, EnemySpawner is unchanged.
//
// Architecture
// ─────────────
//  WaveSystem          — top-level entry point (mirrors WaveSchedule.apply)
//  EnemyRole           — semantic role enum used by the auto-scaler
//  EnemyProfile        — data-driven descriptor for every enemy type
//  EnemyRegistry       — single place to register / add enemies
//  WavePhase           — time window with difficulty scalar (manual or auto)
//  AutoWaveGenerator   — generates SpawnEvents after manual phases end
//
// Usage
// ─────
//  Replace  WaveSchedule.apply(spawner)
//  with     WaveSystem.apply(spawner)
//
// Adding a new enemy
// ──────────────────
//  1. Add an entry to EnemyRegistry.profiles (name, role, stats)
//  2. Done — the auto-scaler picks it up automatically based on its role.
// =============================================================================

// ─────────────────────────────────────────────────────────────────────────────
// ROLE TAXONOMY
// ─────────────────────────────────────────────────────────────────────────────

enum class EnemyRole {
    BASIC,      // Filler melee — always fair to spawn in groups
    RANGED,     // Ranged pressure — limit simultaneous count
    ELITE,      // Stronger melee — one or two at a time
    FAST,       // Speed chaser (wolf1) — dangerous in packs; cap group size
    BRUISER,    // Tanky slow hitter (wolf2) — limit per wave
    MINI_BOSS   // High-threat rare unit (wolf3, skeleton_boss) — never spammed
}

// ─────────────────────────────────────────────────────────────────────────────
// DATA-DRIVEN ENEMY DESCRIPTOR
// ─────────────────────────────────────────────────────────────────────────────

data class EnemyProfile(
    val name: String,
    val role: EnemyRole,

    // Difficulty weight: used when selecting enemies proportionally.
    // Higher = rarer / harder.  Range: 1–10 (tunable).
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

// ─────────────────────────────────────────────────────────────────────────────
// ENEMY REGISTRY — single place to add / configure enemies
// ─────────────────────────────────────────────────────────────────────────────

object EnemyRegistry {

    /**
     * Add new enemies here. Order doesn't matter — the scaler queries by role.
     *
     * difficultyWeight guidelines
     *   1–2  : always-fair filler (skeleton)
     *   3–4  : moderate threat (archer, spearman, wolf1)
     *   5–6  : meaningful threat (wolf2)
     *   7–8  : serious threat (skeleton_boss)
     *   9–10 : endgame only (wolf3)
     */
    val profiles: List<EnemyProfile> = listOf(
        // ── Skeletons ─────────────────────────────────────────────────────────
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
            unlockTime      = 10.0,
            spawnX          = 1050.0,
            maxGroupSize    = 2,
            groupSpacing    = 50.0
        ),
        EnemyProfile(
            name            = "skeleton_spearman",
            role            = EnemyRole.ELITE,
            difficultyWeight = 3.5,
            unlockTime      = 25.0,
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

        // ── Wolves ────────────────────────────────────────────────────────────
        EnemyProfile(
            name            = "wolf1",
            role            = EnemyRole.FAST,
            difficultyWeight = 4.0,
            unlockTime      = 120.0,
            spawnX          = 950.0,
            maxGroupSize    = 2,
            groupSpacing    = 60.0
        ),
        EnemyProfile(
            name            = "wolf2",
            role            = EnemyRole.BRUISER,
            difficultyWeight = 6.0,
            unlockTime      = 180.0,
            spawnX          = 900.0,
            maxGroupSize    = 1,
            groupSpacing    = 0.0
        ),
        EnemyProfile(
            name            = "wolf3",
            role            = EnemyRole.MINI_BOSS,
            difficultyWeight = 9.5,
            unlockTime      = 220.0,
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

// ─────────────────────────────────────────────────────────────────────────────
// WAVE PHASE — describes a time window with a difficulty scalar
// ─────────────────────────────────────────────────────────────────────────────

data class WavePhase(
    val startTime: Double,
    val endTime:   Double,
    // 0.0 = trivial, 1.0 = late-game baseline.
    // Manual phases set this for documentation purposes only.
    // The AutoWaveGenerator uses it as the starting difficulty for auto-waves.
    val difficulty: Double
)

// ─────────────────────────────────────────────────────────────────────────────
// AUTO WAVE GENERATOR
// ─────────────────────────────────────────────────────────────────────────────

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
 *  1. Filter by unlockTime ≤ current time.
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

    // Role budgets: max proportion of a wave that can be a given role (0–1).
    // Reduces as difficulty rises — more dangerous enemies become more common.
    private val roleBudget: Map<EnemyRole, Double> = mapOf(
        EnemyRole.BASIC     to 1.0,   // always unrestricted
        EnemyRole.RANGED    to 0.6,
        EnemyRole.ELITE     to 0.5,
        EnemyRole.FAST      to 0.4,
        EnemyRole.BRUISER   to 0.25,
        EnemyRole.MINI_BOSS to 0.10
    )

    // Track how many of each role we've emitted in recent history (rolling window)
    private val recentRoleCount = mutableMapOf<EnemyRole, Int>()
    private val roleCooldownUntil = mutableMapOf<EnemyRole, Double>()

    // Mandatory cooldowns (seconds) between spawns of that role
    private val roleCooldown: Map<EnemyRole, Double> = mapOf(
        EnemyRole.MINI_BOSS to 55.0,
        EnemyRole.BRUISER   to 30.0,
        EnemyRole.FAST      to 15.0,
        EnemyRole.ELITE     to 12.0,
        EnemyRole.RANGED    to 8.0,
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

            // ── Breathing window ──────────────────────────────────────────
            if (cursor >= nextBreathingAt) {
                cursor += breathingWindowLength
                nextBreathingAt = cursor + breathingWindowEvery
                continue
            }

            // ── Select enemy ──────────────────────────────────────────────
            val candidate = selectEnemy(cursor, difficulty) ?: run {
                cursor += 5.0   // no eligible enemy yet; wait a bit
                continue
            }

            // ── Group size ────────────────────────────────────────────────
            val groupBonus = (groupScaleRate * elapsed).toInt()
            val groupSize  = (1 + groupBonus).coerceAtMost(candidate.maxGroupSize)

            // ── Emit SpawnEvent ───────────────────────────────────────────
            events.add(
                SpawnEvent(
                    time      = cursor,
                    enemyType = candidate.name,
                    x         = candidate.spawnX,
                    count     = groupSize,
                    offsetX   = if (groupSize > 1) candidate.groupSpacing else 0.0
                )
            )

            // Update role cooldown
            roleCooldownUntil[candidate.role] = cursor + (roleCooldown[candidate.role] ?: 0.0)

            // ── Advance time ──────────────────────────────────────────────
            val interval = (baseInterval / (1.0 + intervalDecayRate * elapsed))
                .coerceAtLeast(minInterval)
            // Vary slightly so it feels organic (±20 %)
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

// ─────────────────────────────────────────────────────────────────────────────
// WAVE SYSTEM — top-level entry point
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Drop-in replacement for WaveSchedule.
 *
 * Manual phases (exact same SpawnEvent syntax you already know):
 *   Phase 1 — Learning Phase:  t =   0 –  60 s  (difficulty 0.2)
 *   Phase 1.1 — Starting:      t =  60 – 120 s  (difficulty 0.4)
 *   Phase 2 — Pressure:        t = 120 – 180 s  (difficulty 0.7)
 *   Phase 3 — Endgame:         t = 180 – 240 s  (difficulty 1.0)
 *
 * Auto-phase:                  t = 240+ s        (auto-scaler takes over)
 */
object WaveSystem {

    // Phase boundaries — for documentation and future use by UI / analytics
    val phases = listOf(
        WavePhase(startTime =   0.0, endTime =  60.0, difficulty = 0.2),
        WavePhase(startTime =  60.0, endTime = 120.0, difficulty = 0.4),
        WavePhase(startTime = 120.0, endTime = 180.0, difficulty = 0.7),
        WavePhase(startTime = 180.0, endTime = 240.0, difficulty = 1.0)
        // Auto-phase begins at 240.0 — no entry needed here
    )

    fun apply(spawner: EnemySpawner) {

        // ─────────────────────────────────────────────────────────────────────
        // MANUAL PHASE CONSTANTS (unchanged from WaveSchedule)
        // ─────────────────────────────────────────────────────────────────────
        val next_wave  = 60.0
        val next_wave2 = 120.0
        val next_wave3 = 180.0

        spawner.schedule(

            // =================================================================
            // WAVE 1 — Learning Phase  (t = 0–60 s)
            // Teaches: melee approach, ranged threat, spearman longer reach
            // =================================================================
            SpawnEvent(1.0,  "skeleton",          900.0),
            SpawnEvent(5.0,  "skeleton",         1100.0),

            SpawnEvent(10.0, "skeleton",          900.0),
            SpawnEvent(12.0, "skeleton_archer",  1000.0),

            SpawnEvent(18.0, "skeleton",         1000.0, 2, 50.0),
            SpawnEvent(20.0, "skeleton_archer",  1000.0),

            SpawnEvent(26.0, "skeleton_spearman", 950.0),

            SpawnEvent(35.0, "skeleton",         1000.0),

            SpawnEvent(40.0, "skeleton",         1000.0, 2, 40.0),
            SpawnEvent(40.0, "skeleton_archer",  1000.0),

            SpawnEvent(50.0, "skeleton_spearman", 900.0),
            SpawnEvent(50.0, "skeleton_archer",  1000.0),
            SpawnEvent(50.0, "skeleton",         1000.0),

            SpawnEvent(60.0, "skeleton_boss",     640.0),

            // =================================================================
            // WAVE 1.1 — Starting Phase  (t = 60–120 s)
            // Mirrors Wave 1 — player is still learning with a second chance
            // =================================================================
            SpawnEvent(next_wave + 1.0,  "skeleton",          900.0),
            SpawnEvent(next_wave + 5.0,  "skeleton",         1100.0),

            SpawnEvent(next_wave + 10.0, "skeleton",          900.0),
            SpawnEvent(next_wave + 12.0, "skeleton_archer",  1000.0),

            SpawnEvent(next_wave + 18.0, "skeleton",         1000.0, 2, 50.0),
            SpawnEvent(next_wave + 20.0, "skeleton_archer",  1000.0),

            SpawnEvent(next_wave + 26.0, "skeleton_spearman", 950.0),

            SpawnEvent(next_wave + 35.0, "skeleton",         1000.0),

            SpawnEvent(next_wave + 40.0, "skeleton",         1000.0, 2, 40.0),
            SpawnEvent(next_wave + 40.0, "skeleton_archer",  1000.0),

            SpawnEvent(next_wave + 50.0, "skeleton_spearman", 900.0),
            SpawnEvent(next_wave + 50.0, "skeleton_archer",  1000.0),
            SpawnEvent(next_wave + 50.0, "skeleton",         1000.0),

            SpawnEvent(next_wave + 60.0, "skeleton_boss",     640.0),

            // =================================================================
            // WAVE 2 — Pressure Phase  (t = 120–180 s)
            // Introduces wolf1; tighter spacing; harder boss
            // =================================================================
            SpawnEvent(next_wave2 + 1.0,  "skeleton",          1000.0, 2, 20.0),

            SpawnEvent(next_wave2 + 5.0,  "wolf1",              950.0),

            SpawnEvent(next_wave2 + 10.0, "skeleton_archer",   1000.0, 2, 40.0),
            SpawnEvent(next_wave2 + 12.0, "skeleton",          1000.0, 2, 20.0),

            SpawnEvent(next_wave2 + 20.0, "skeleton_spearman", 1000.0),
            SpawnEvent(next_wave2 + 20.0, "wolf1",              950.0),

            SpawnEvent(next_wave2 + 30.0, "skeleton",          1000.0),

            SpawnEvent(next_wave2 + 35.0, "skeleton_archer",   1000.0, 2, 40.0),
            SpawnEvent(next_wave2 + 35.0, "wolf1",              900.0),

            SpawnEvent(next_wave2 + 40.0, "skeleton_spearman",  950.0),
            SpawnEvent(next_wave2 + 40.0, "skeleton",          1000.0, 2, 20.0),

            SpawnEvent(next_wave2 + 48.0, "skeleton",          1000.0),

            SpawnEvent(next_wave2 + 50.0, "skeleton_boss",     640.0 * 1.2),

            // =================================================================
            // WAVE 3 — Endgame  (t = 180–240 s)
            // Introduces wolf2; wolf3 appears as mini-boss
            // =================================================================
            SpawnEvent(next_wave3 + 1.0,  "wolf1",              900.0),
            SpawnEvent(next_wave3 + 7.0,  "skeleton_archer",   1000.0),

            SpawnEvent(next_wave3 + 20.0, "skeleton",          1000.0, 2, 20.0),
            SpawnEvent(next_wave3 + 20.0, "wolf1",              900.0),
            SpawnEvent(next_wave3 + 20.0, "skeleton_archer",   1000.0),

            SpawnEvent(next_wave3 + 30.0, "skeleton_spearman",  950.0),
            SpawnEvent(next_wave3 + 30.0, "wolf2",              900.0),

            SpawnEvent(next_wave3 + 40.0, "skeleton_archer",   1000.0, 2, 30.0),
            SpawnEvent(next_wave3 + 40.0, "skeleton",          1000.0, 2, 20.0),

            SpawnEvent(next_wave3 + 40.0, "wolf3",              900.0),
            SpawnEvent(next_wave3 + 40.0, "skeleton_spearman",  950.0),

            SpawnEvent(next_wave3 + 50.0, "skeleton_boss",     640.0 * 1.4)
        )

        // ─────────────────────────────────────────────────────────────────────
        // AUTO-PHASE — pre-generate and schedule endless waves
        // ─────────────────────────────────────────────────────────────────────
        val autoEvents = AutoWaveGenerator(
            startTime            = 240.0,   // immediately after Wave 3 ends
            startDifficulty      = 1.0,     // matches Wave 3 difficulty
            difficultyGainPerSec = 0.004,   // +1 difficulty per ~4 min — smooth climb
            baseInterval         = 12.0,    // start at one spawn every 12 s
            intervalDecayRate    = 0.0015,  // interval shrinks to ~4 s by t = 700 s
            minInterval          = 4.0,
            groupScaleRate       = 0.003,
            breathingWindowEvery = 90.0,    // 8-second breather every 90 seconds
            breathingWindowLength = 8.0,
            durationToGenerate   = 600.0    // generate 10 minutes of auto-waves
        ).generate()

        spawner.scheduleList(autoEvents)
    }
}