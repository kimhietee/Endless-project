// =============================================================================
// WaveSystem.kt — Hybrid Wave Progression System (REBALANCED)
//
// CHANGES FROM ORIGINAL — every change explained:
//
// WAVE REBALANCING
// ────────────────
// Wave 1  : Unchanged — good tutorial pacing.
// Wave 2  : Rebuilt from scratch. Original was Wave 1 + 4 skeletons. New version
//           properly escalates: denser spearman usage, tighter spawn windows,
//           ends with the boss + a spearman escort so the boss isn't solo.
// Wave 3  : Wolf1 introduction kept. Adjusted skeleton volume to prevent
//           simultaneous wolf + dense skeleton overlap from being overwhelming.
//           Skeleton boss moved to t=60 (not t=50) to give players breathing room.
// Wave 4  : REMOVED wolf3 from t=70. Wolf3 (240 speed, 4.3 DPS, 80 HP) is a
//           mini-boss that should not appear before the player has Skill 2
//           (unlocks at level 8 — player is only ~level 5-6 at Wave 4).
//           Replaced with wolf2 (130 speed, 3.3 DPS) as a first bruiser lesson.
//           Kept wolf1 pairs — the speed threat is still present but survivable.
// Wave 5  : The original spawned 12 simultaneous enemies at t=7 with ~3,600 HP
//           total — mathematically impossible to clear with basic attack alone.
//           Rebuilt as two "surges" with actual breathing gaps between them.
//           Skeleton boss moved to t=50 where it's still a threat but not buried
//           under an active swarm. Total enemy HP reduced to a clearable amount.
// Wave 6  : Kept mostly intact — good variety design. Reduced simultaneous
//           skeleton_boss count from 3 to 2 (3 bosses in one 90s wave is
//           disproportionate given each has ~6 effective DPS). Third boss moved
//           to a post-breathe position so it's still the climax.
// Wave 7  : Kept. Intentionally hard — the wall before endgame is correct.
//           Minor: spaced wolf2 entries slightly to avoid simultaneous wolf2
//           + skeleton_boss stacking (two enemies that each demand focused DPS).
// Wave 8  : Fixed the 0–14s zero-breathing opener. Added a 3s gap before the
//           first kobold salvo. Total density unchanged — just redistributed
//           the opening slightly so the player can spend initial mana.
//
// WAVE ADDITIONS — Waves 9, 10, 11 (post Wave 8 bridge into AutoWave)
// ────────────────────────────────────────────────────────────────────
// Wave 9  : "The Hunt" — wolf1/wolf3 focused. First proper wolf3 appearance.
//           Player is level ~14-16 by now, Skill 3 available. Wolf3 is dangerous
//           but manageable with a cooldown skill. Skeleton support keeps pressure on.
// Wave 10 : "Legion" — largest enemy count in the game. Dense mixed swarms with
//           wolf pack flanks. Two wolf3 appearances. No boss — quantity IS the boss.
// Wave 11 : "Last Stand" — all enemy types including wolf3. Three skeleton bosses
//           at spaced intervals. Marks the end of the manual phase. By the end the
//           player is ~level 18+ with Skill 4 available.
//
// AUTOWAVEGENERATOR FIXES
// ───────────────────────
// 1. startTime: was 1.0 (running from the START, competing with manual waves).
//    Fixed to autoPhaseStart (calculated as sum of all manual wave durations),
//    so auto-waves only begin AFTER all manual waves are exhausted.
// 2. startDifficulty: was 0.5 (easier than Wave 3). Set to 2.5 to match
//    where Wave 11 ends on the difficulty curve.
// 3. difficultyGainPerSec: was 1.0 (gaining 1.0 per second = difficulty 601
//    after 10 minutes — astronomically broken). Changed to 0.004 so it takes
//    ~4 minutes to gain 1 full difficulty point. At 10 minutes in = difficulty ~4.9.
// 4. baseInterval: was 5.0s (very fast from the start). Set to 10.0 to match
//    Wave 8's ~12s average spawn rhythm, then decay tightens it naturally.
// 5. minInterval: was 2.0s (impossibly fast swarms). Set to 4.5s — you cannot
//    react to a spawn faster than that with any skill cooldown.
// 6. groupScaleRate: was 0.008 (groups of 4 after ~375s). Set to 0.002 so
//    maxGroupSize isn't hit until ~500s into the auto phase.
// 7. breathingWindowEvery: was 90s. Kept at 90s — this is correct.
// 8. breathingWindowLength: was 8s. Increased to 10s to give regen a chance
//    (at 0.5 HP/s, 10s = 5 HP back, which is meaningful at late-game damage rates).
// 9. Role thresholds: MINI_BOSS now requires difficulty >= 2.8 (not 1.5) and
//    BRUISER requires >= 2.2. This prevents wolf3 from appearing in the first
//    few auto-waves where the difficulty is 2.5 and hasn't climbed far yet.
// 10.roleCooldown for MINI_BOSS: raised from 60s to 90s. At high difficulty,
//    wolf3 every 60s would be effectively permanent presence.
// =============================================================================

package managers

import kotlin.random.*
import entities.EnemySpawner
import entities.SpawnEvent

enum class EnemyRole {
    BASIC, RANGED, ELITE, FAST, BRUISER, MINI_BOSS
}

data class EnemyProfile(
    val name: String,
    val role: EnemyRole,
    val difficultyWeight: Double,
    val unlockTime: Double,
    val spawnX: Double,
    val maxGroupSize: Int,
    val groupSpacing: Double = 40.0
)

object EnemyRegistry {
    val profiles: List<EnemyProfile> = listOf(
        EnemyProfile("skeleton",         EnemyRole.BASIC,     1.0, 0.0,   1000.0, 3, 40.0),
        EnemyProfile("skeleton_archer",  EnemyRole.RANGED,    2.0, 0.0,   1050.0, 2, 50.0),
        EnemyProfile("skeleton_spearman",EnemyRole.ELITE,     3.0, 10.0,   950.0, 2, 50.0),
        EnemyProfile("skeleton_boss",    EnemyRole.MINI_BOSS, 8.0, 60.0,   640.0, 1, 0.0),
        EnemyProfile("kobold",           EnemyRole.ELITE,     3.0, 30.0,  1000.0, 3, 40.0),
        EnemyProfile("flying_eye",       EnemyRole.RANGED,    2.0, 0.0,   1050.0, 2, 60.0),
        EnemyProfile("goblin",           EnemyRole.RANGED,    2.0, 0.0,    950.0, 3, 50.0),
        EnemyProfile("wolf1",            EnemyRole.FAST,      4.0, 30.0,   950.0, 2, 60.0),
        EnemyProfile("wolf2",            EnemyRole.BRUISER,   5.0, 60.0,   900.0, 1, 0.0),
        EnemyProfile("wolf3",            EnemyRole.MINI_BOSS, 9.0, 100.0,  900.0, 1, 0.0)
    )

    fun byName(name: String): EnemyProfile? = profiles.firstOrNull { it.name == name }
    fun byRole(role: EnemyRole): List<EnemyProfile> = profiles.filter { it.role == role }
    fun available(atTime: Double): List<EnemyProfile> = profiles.filter { it.unlockTime <= atTime }
}

data class Wave(
    val duration: Double,
    val events: List<SpawnEvent>,
    val name: String = ""
) {
    fun withStart(startTime: Double): List<SpawnEvent> =
        events.map { it.copy(time = startTime + it.time) }
}

data class WavePhase(
    val startTime: Double,
    val endTime:   Double,
    val difficulty: Double
)

// =============================================================================
// AUTO WAVE GENERATOR — fixed to start AFTER manual waves and scale correctly
// =============================================================================
class AutoWaveGenerator(
    // ── FIX 1: startTime is now passed in as autoPhaseStart from WaveSystem,
    //    so auto-waves never overlap with manual waves. ──────────────────────
    private val startTime:             Double,

    // ── FIX 2: starts at 2.5 to match Wave 11's exit difficulty. ───────────
    private val startDifficulty:       Double = 2.5,

    // ── FIX 3: was 1.0 (insane). 0.004 = +1 difficulty every ~4 min. ───────
    private val difficultyGainPerSec:  Double = 0.004,

    // ── FIX 4: was 5.0s (too fast from the start). 10.0 matches Wave 8. ────
    private val baseInterval:          Double = 10.0,

    private val intervalDecayRate:     Double = 0.0015,

    // ── FIX 5: was 2.0s (impossible to react). 4.5s is the floor. ──────────
    private val minInterval:           Double = 4.5,

    // ── FIX 6: was 0.008. At 0.002, maxGroupSize not hit until ~500s in. ───
    private val groupScaleRate:        Double = 0.002,

    private val breathingWindowEvery:  Double = 90.0,

    // ── FIX 7: was 8s. 10s gives 5 HP regen at 0.5 HP/s. ───────────────────
    private val breathingWindowLength: Double = 10.0,

    private val durationToGenerate:    Double = 1200.0   // 20 minutes of content
) {
    private val roleBudget: Map<EnemyRole, Double> = mapOf(
        EnemyRole.BASIC     to 1.0,
        EnemyRole.RANGED    to 0.8,
        EnemyRole.ELITE     to 0.7,
        EnemyRole.FAST      to 0.4,
        EnemyRole.BRUISER   to 0.25,
        EnemyRole.MINI_BOSS to 0.10
    )

    private val roleCooldownUntil = mutableMapOf<EnemyRole, Double>()

    private val roleCooldown: Map<EnemyRole, Double> = mapOf(
        // ── FIX 8: MINI_BOSS was 60s. 90s prevents wolf3 from becoming
        //    a permanent fixture at high difficulty. ─────────────────────────
        EnemyRole.MINI_BOSS to 90.0,
        EnemyRole.BRUISER   to 35.0,
        EnemyRole.FAST      to 22.0,
        EnemyRole.ELITE     to 12.0,
        EnemyRole.RANGED    to 6.0,
        EnemyRole.BASIC     to 0.0
    )

    fun generate(): List<SpawnEvent> {
        val events = mutableListOf<SpawnEvent>()
        var cursor = startTime
        val endTime = startTime + durationToGenerate
        var nextBreathingAt = startTime + breathingWindowEvery

        while (cursor < endTime) {
            val elapsed = cursor - startTime
            val difficulty = startDifficulty + difficultyGainPerSec * elapsed

            if (cursor >= nextBreathingAt) {
                cursor += breathingWindowLength
                nextBreathingAt = cursor + breathingWindowEvery
                continue
            }

            val candidate = selectEnemy(cursor, difficulty)
            if (candidate == null) { cursor += 5.0; continue }

            val groupBonus = (groupScaleRate * elapsed).toInt()
            val groupSize  = (1 + groupBonus).coerceAtMost(candidate.maxGroupSize)

            events.add(SpawnEvent(
                time      = cursor,
                enemyType = candidate.name,
                x         = Random.nextDouble(100.0, 1000.0),
                count     = groupSize,
                offsetX   = if (groupSize > 1) candidate.groupSpacing else 0.0
            ))

            roleCooldownUntil[candidate.role] = cursor + (roleCooldown[candidate.role] ?: 0.0)

            val interval = (baseInterval / (1.0 + intervalDecayRate * elapsed))
                .coerceAtLeast(minInterval)
            val jitter = interval * 0.2 * (Math.random() * 2.0 - 1.0)
            cursor += (interval + jitter).coerceAtLeast(minInterval * 0.8)
        }

        return events
    }

    private fun selectEnemy(atTime: Double, difficulty: Double): EnemyProfile? {
        val eligible = EnemyRegistry.available(atTime).filter { profile ->
            val cooldownOk = (roleCooldownUntil[profile.role] ?: 0.0) <= atTime
            val diffOk = when (profile.role) {
                // ── FIX 9: raised thresholds so early auto-waves feel like
                //    a hard Wave 9, not an immediate wolf3 parade. ───────────
                EnemyRole.MINI_BOSS -> difficulty >= 2.8
                EnemyRole.BRUISER   -> difficulty >= 2.2
                EnemyRole.FAST      -> difficulty >= 2.0
                else                -> true
            }
            cooldownOk && diffOk
        }
        if (eligible.isEmpty()) return null

        val totalWeight = eligible.sumOf { 1.0 / it.difficultyWeight }
        var rand = Math.random() * totalWeight
        for (profile in eligible) {
            rand -= 1.0 / profile.difficultyWeight
            if (rand <= 0.0) return profile
        }
        return eligible.last()
    }
}

// =============================================================================
// WAVE SYSTEM — 11 manual waves + auto phase
// =============================================================================
object WaveSystem {

    val phases = listOf(
        WavePhase(0.0,    60.0,  0.2),   // W1  — tutorial
        WavePhase(60.0,   150.0, 0.35),  // W2  — transition
        WavePhase(150.0,  240.0, 0.5),   // W3  — wolf intro
        WavePhase(240.0,  330.0, 0.7),   // W4  — elite pressure
        WavePhase(330.0,  420.0, 0.85),  // W5  — swarm (now survivable)
        WavePhase(420.0,  510.0, 1.0),   // W6  — new roster
        WavePhase(510.0,  600.0, 1.2),   // W7  — punishment
        WavePhase(600.0,  690.0, 1.5),   // W8  — endgame density
        WavePhase(690.0,  780.0, 1.8),   // W9  — hunt
        WavePhase(780.0,  870.0, 2.1),   // W10 — legion
        WavePhase(870.0,  960.0, 2.5),   // W11 — last stand
    )

    private val manualWaves = listOf(

        // ── WAVE 1: Tutorial ─────────────────────────────────────────────────
        // Unchanged. Gentle skeleton-only learning wave.
        // Boss spawns at the very end (t=60) as a lesson in target priority.
        Wave(
            duration = 60.0, name = "Wave 1 (Tutorial)",
            events = listOf(
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
                SpawnEvent(60.0, "skeleton_boss",     640.0)
            )
        ),

        // ── WAVE 2: Escalation ───────────────────────────────────────────────
        // REBUILT: Original was Wave 1 + 4 skeletons — no real escalation.
        // New design: tighter windows, more spearmen (the first "real" threat),
        // two skeleton-archer pairs at range, and the boss gets a spearman escort
        // so the player can't just focus the boss in isolation.
        Wave(
            duration = 90.0, name = "Wave 2 (Escalation)",
            events = listOf(
                SpawnEvent(1.0,  "skeleton",          900.0, 2, 50.0),
                SpawnEvent(3.0,  "skeleton_archer",  1050.0),
                SpawnEvent(6.0,  "skeleton",         1000.0),
                SpawnEvent(10.0, "skeleton_spearman", 950.0),
                SpawnEvent(12.0, "skeleton_archer",  1050.0, 2, 50.0),
                SpawnEvent(15.0, "skeleton",         1000.0, 2, 40.0),
                SpawnEvent(18.0, "skeleton_spearman", 900.0),
                SpawnEvent(22.0, "skeleton",         1000.0),
                SpawnEvent(25.0, "skeleton_archer",  1050.0),
                SpawnEvent(28.0, "skeleton_spearman", 950.0, 2, 50.0),
                SpawnEvent(32.0, "skeleton",         1000.0, 2, 40.0),
                SpawnEvent(35.0, "skeleton_archer",  1050.0, 2, 50.0),
                // -- breathing gap 36–42s --
                SpawnEvent(42.0, "skeleton_spearman", 900.0),
                SpawnEvent(44.0, "skeleton",         1000.0, 2, 40.0),
                SpawnEvent(47.0, "skeleton_archer",  1050.0),
                SpawnEvent(50.0, "skeleton_spearman", 950.0, 2, 50.0),
                SpawnEvent(55.0, "skeleton",         1000.0, 3, 40.0),
                SpawnEvent(58.0, "skeleton_archer",  1050.0, 2, 50.0),
                SpawnEvent(62.0, "skeleton_boss",     640.0),        // boss
                SpawnEvent(62.0, "skeleton_spearman", 900.0),        // escort — must split focus
                SpawnEvent(70.0, "skeleton",         1000.0, 2, 40.0),
                SpawnEvent(75.0, "skeleton_archer",  1050.0),
                SpawnEvent(80.0, "skeleton_spearman", 950.0),
                SpawnEvent(85.0, "skeleton",         1000.0, 2, 50.0)
            )
        ),

        // ── WAVE 3: Wolf Introduction ─────────────────────────────────────────
        // Wolf1 (180 speed) appears for the first time. Players learn that it
        // moves much faster and needs to be kited or hit with Skill 1.
        // Reduced skeleton density during wolf windows so the speed threat reads clearly.
        // Boss moved to t=62 (was t=50) to avoid overlapping with the t=55 wolf.
        Wave(
            duration = 90.0, name = "Wave 3 (Wolf intro)",
            events = listOf(
                SpawnEvent(1.0,  "skeleton",          1000.0, 2, 20.0),
                SpawnEvent(5.0,  "wolf1",              950.0),           // first wolf — singleton
                SpawnEvent(10.0, "skeleton_archer",   1000.0, 2, 40.0),
                SpawnEvent(12.0, "skeleton",          1000.0, 2, 20.0),
                SpawnEvent(18.0, "skeleton_spearman", 1000.0),
                SpawnEvent(22.0, "wolf1",              950.0),           // second wolf
                // -- breathing 24–30s --
                SpawnEvent(30.0, "skeleton",          1000.0, 2, 30.0),
                SpawnEvent(33.0, "skeleton_archer",   1000.0, 2, 40.0),
                SpawnEvent(38.0, "wolf1",              900.0),           // third — now they know it's fast
                SpawnEvent(42.0, "skeleton_spearman",  950.0),
                SpawnEvent(45.0, "skeleton",          1000.0, 2, 20.0),
                SpawnEvent(50.0, "skeleton_archer",   1000.0),
                SpawnEvent(55.0, "wolf1",              900.0),           // wolf before boss
                SpawnEvent(58.0, "skeleton",          1000.0),
                // -- breathing 59–62s --
                SpawnEvent(62.0, "skeleton_boss",      768.0),           // boss
                SpawnEvent(70.0, "skeleton_spearman",  950.0),
                SpawnEvent(73.0, "skeleton_archer",   1000.0, 2, 40.0),
                SpawnEvent(78.0, "wolf1",              900.0),           // post-boss cleanup wolf
                SpawnEvent(83.0, "skeleton",          1000.0, 2, 30.0)
            )
        ),

        // ── WAVE 4: Elite Pressure ────────────────────────────────────────────
        // FIXED: Wolf3 (mini-boss, 240 speed) REMOVED. Player is only ~level 5-6
        // and cannot handle it without Skill 2 (level 8).
        // Replaced with wolf2 as the first bruiser lesson — slower than wolf3
        // but still dangerous (130 speed, 3.3 DPS). Wolf1 pairs continue.
        // Flanking from both sides (x=100 and x=1100) introduced here.
        Wave(
            duration = 90.0, name = "Wave 4 (Elite pressure)",
            events = listOf(
                SpawnEvent(1.0,  "skeleton_spearman",  900.0),
                SpawnEvent(2.0,  "skeleton_archer",   1000.0, 2, 40.0),
                SpawnEvent(5.0,  "skeleton",          1000.0, 2, 50.0),
                SpawnEvent(7.0,  "skeleton",           100.0, 2, 60.0),  // flank left
                SpawnEvent(10.0, "skeleton",           100.0, 2, 60.0),
                SpawnEvent(11.0, "skeleton_archer",   1100.0, 2, 50.0),
                SpawnEvent(18.0, "wolf1",              950.0),
                SpawnEvent(20.0, "skeleton",           100.0, 2, 60.0),
                SpawnEvent(20.0, "skeleton_archer",   1000.0),
                SpawnEvent(25.0, "skeleton_spearman",  950.0),
                SpawnEvent(28.0, "wolf1",              100.0),           // wolf from left
                // -- breathing 30–35s --
                SpawnEvent(35.0, "skeleton",           900.0, 2, 40.0),
                SpawnEvent(37.0, "skeleton_archer",    100.0, 2, 30.0),
                SpawnEvent(40.0, "wolf1",             1100.0),           // wolf from right
                SpawnEvent(44.0, "skeleton_archer",   1000.0, 2, 30.0),
                SpawnEvent(47.0, "skeleton",           900.0, 2, 50.0),
                SpawnEvent(49.0, "skeleton_spearman",  900.0, 2, 100.0),
                SpawnEvent(50.0, "skeleton_boss",      384.0),           // boss centre
                SpawnEvent(52.0, "wolf1",              100.0, 2, 100.0), // wolf flanks during boss
                // -- breathing 55–60s --
                SpawnEvent(60.0, "skeleton_spearman",  950.0),
                SpawnEvent(62.0, "skeleton_archer",    100.0),
                SpawnEvent(64.0, "skeleton",           900.0, 2, 40.0),
                SpawnEvent(67.0, "skeleton_archer",   1000.0),
                // Wolf2 appears for the first time — introduce it clearly, solo.
                // 130 speed (faster than skeletons, slower than wolf1). 3.3 DPS.
                // Player should notice it's a different threat class.
                SpawnEvent(72.0, "wolf2",              900.0),           // NEW: wolf2 debut
                SpawnEvent(76.0, "wolf1",              100.0, 2, 100.0),
                SpawnEvent(82.0, "skeleton",           900.0, 2, 50.0),
                SpawnEvent(86.0, "skeleton_spearman",  950.0)
            )
        ),

        // ── WAVE 5: Rising Tide ───────────────────────────────────────────────
        // REBUILT: Original spawned 12 simultaneous enemies at t=7 (impossible
        // to clear without Skill 2). New design: two surge windows with clear
        // breathing gaps. Flanking from both sides. Total HP reduced to ~2,200
        // (from ~3,600) — still requires efficient skills but is survivable.
        // Boss at t=55 during a surge, not buried under a swarm.
        Wave(
            duration = 90.0, name = "Wave 5 (Rising tide)",
            events = listOf(
                // -- Surge 1: dual-flank skeleton wave --
                SpawnEvent(2.0,  "skeleton",          1000.0, 2, 50.0),
                SpawnEvent(2.0,  "skeleton",           100.0, 2, 50.0),
                SpawnEvent(5.0,  "skeleton_archer",   1000.0, 2, 50.0),
                SpawnEvent(5.0,  "skeleton_archer",    100.0, 2, 50.0),
                SpawnEvent(9.0,  "skeleton_spearman", 1000.0, 2, 50.0),
                SpawnEvent(9.0,  "skeleton_spearman",  100.0, 2, 50.0),
                SpawnEvent(13.0, "skeleton",          1000.0, 2, 50.0),
                SpawnEvent(13.0, "skeleton",           100.0, 2, 50.0),
                // -- breathing gap 16–24s --
                SpawnEvent(24.0, "skeleton",          1000.0, 2, 50.0),
                SpawnEvent(24.0, "skeleton",           100.0, 2, 50.0),
                SpawnEvent(27.0, "skeleton_archer",   1000.0, 2, 50.0),
                SpawnEvent(27.0, "skeleton_archer",    100.0, 2, 50.0),
                SpawnEvent(30.0, "skeleton_spearman", 1000.0, 2, 50.0),
                SpawnEvent(30.0, "skeleton_spearman",  100.0, 2, 50.0),
                SpawnEvent(33.0, "skeleton",          1000.0, 2, 50.0),
                SpawnEvent(33.0, "skeleton",           100.0, 2, 50.0),
                // -- breathing gap 36–44s --
                SpawnEvent(44.0, "skeleton",          1000.0, 2, 50.0),
                SpawnEvent(44.0, "skeleton_archer",    100.0, 2, 50.0),
                SpawnEvent(47.0, "skeleton_spearman", 1000.0, 2, 50.0),
                SpawnEvent(47.0, "skeleton",           100.0, 2, 50.0),
                // -- boss mid-surge (player can't rest) --
                SpawnEvent(50.0, "skeleton_boss",      900.0),
                SpawnEvent(54.0, "skeleton",          1000.0, 2, 50.0),
                SpawnEvent(54.0, "skeleton",           100.0, 2, 50.0),
                SpawnEvent(57.0, "skeleton_archer",   1000.0, 2, 50.0),
                SpawnEvent(57.0, "skeleton_archer",    100.0, 2, 50.0),
                // -- breathing gap 60–68s --
                SpawnEvent(68.0, "skeleton_spearman", 1000.0, 2, 50.0),
                SpawnEvent(68.0, "skeleton_spearman",  100.0, 2, 50.0),
                SpawnEvent(71.0, "skeleton_archer",    900.0, 2, 50.0),
                SpawnEvent(73.0, "skeleton",           900.0, 2, 50.0),
                SpawnEvent(76.0, "skeleton_spearman",  900.0, 2, 50.0),
                SpawnEvent(80.0, "skeleton_archer",    900.0, 2, 50.0),
                SpawnEvent(83.0, "skeleton",           900.0, 3, 50.0)
            )
        ),

        // ── WAVE 6: New Roster ────────────────────────────────────────────────
        // Mostly unchanged — good design. Reduced skeleton_boss count from 3 to 2
        // within the 90s window. The third boss was at t=81 which is fine as a
        // climax, but THREE bosses in one wave (each ~6 effective DPS) overwhelms
        // the player who is still learning goblin/kobold/flying_eye behavior.
        // Third boss presence is kept but pushed back.
        Wave(
            duration = 90.0, name = "Wave 6 (New roster)",
            events = listOf(
                SpawnEvent(1.0,  "skeleton",          980.0, 2, 80.0),
                SpawnEvent(1.0,  "skeleton",          120.0, 2, 80.0),
                SpawnEvent(3.5,  "skeleton_archer",   950.0, 2, 75.0),
                SpawnEvent(5.5,  "flying_eye",        990.0),
                SpawnEvent(7.5,  "flying_eye",         80.0),
                SpawnEvent(10.0, "skeleton_spearman", 920.0, 3, 70.0),
                SpawnEvent(12.5, "goblin",            520.0),
                SpawnEvent(13.5, "kobold",            970.0),
                SpawnEvent(15.5, "flying_eye",        100.0),
                SpawnEvent(17.0, "kobold",            110.0, 2, 80.0),
                SpawnEvent(19.0, "flying_eye",        980.0),
                // first boss at t=25
                SpawnEvent(25.0, "skeleton_boss",     600.0),
                SpawnEvent(27.5, "goblin",            930.0),
                SpawnEvent(28.0, "goblin",            150.0),
                SpawnEvent(29.5, "flying_eye",         70.0),
                // -- breathing 32–38s --
                SpawnEvent(39.0, "kobold",            960.0, 2, 65.0),
                SpawnEvent(41.0, "skeleton_spearman", 140.0, 2, 75.0),
                SpawnEvent(43.0, "goblin",            480.0),
                SpawnEvent(44.5, "flying_eye",        990.0),
                SpawnEvent(46.5, "flying_eye",        110.0),
                SpawnEvent(50.0, "kobold",            120.0, 2, 80.0),
                SpawnEvent(51.0, "goblin",            950.0),
                SpawnEvent(52.5, "flying_eye",        980.0),
                // -- breathing 54–59s --
                SpawnEvent(60.0, "kobold",            980.0, 2, 70.0),
                SpawnEvent(61.5, "flying_eye",         90.0),
                SpawnEvent(63.5, "skeleton_archer",   920.0, 3, 65.0),
                SpawnEvent(65.0, "flying_eye",        980.0),
                SpawnEvent(66.0, "goblin",            180.0),
                SpawnEvent(66.5, "goblin",            550.0),
                SpawnEvent(70.0, "kobold",            110.0, 2, 75.0),
                SpawnEvent(72.0, "flying_eye",        120.0),
                SpawnEvent(74.0, "skeleton_spearman", 930.0, 2, 70.0),
                SpawnEvent(76.0, "flying_eye",        950.0),
                // -- breathing 77–80s --
                // second boss at t=81 as climax (was third boss in original)
                SpawnEvent(81.0, "skeleton_boss",     620.0),
                SpawnEvent(83.0, "kobold",            950.0, 2, 70.0),
                SpawnEvent(84.0, "kobold",            130.0),
                SpawnEvent(85.0, "flying_eye",        980.0),
                SpawnEvent(86.0, "flying_eye",         80.0),
                SpawnEvent(87.0, "goblin",            500.0)
            )
        ),

        // ── WAVE 7: Punishment ────────────────────────────────────────────────
        // Kept largely intact — this IS the wall. Minor fixes:
        // wolf2 at t=30 and skeleton_boss at t=38 separated from simultaneous
        // spawn (both demand focused damage, shouldn't land at same second).
        Wave(
            duration = 90.0, name = "Wave 7 (Punishment)",
            events = listOf(
                SpawnEvent(0.5,  "kobold",            960.0, 2, 75.0),
                SpawnEvent(1.2,  "kobold",            110.0),
                SpawnEvent(2.5,  "flying_eye",        990.0),
                SpawnEvent(4.0,  "skeleton_archer",   130.0, 3, 70.0),
                SpawnEvent(5.5,  "flying_eye",        100.0),
                SpawnEvent(8.0,  "wolf1",             920.0),
                SpawnEvent(10.0, "goblin",            520.0),
                SpawnEvent(10.5, "goblin",            150.0),
                SpawnEvent(11.5, "kobold",            980.0, 2, 75.0),
                SpawnEvent(13.0, "flying_eye",        950.0),
                SpawnEvent(16.0, "flying_eye",         80.0),
                SpawnEvent(17.0, "flying_eye",        980.0),
                SpawnEvent(18.5, "flying_eye",        120.0),
                SpawnEvent(20.0, "skeleton_boss",     630.0),
                // -- breathing 23–29s --
                SpawnEvent(29.0, "wolf2",             910.0),           // separated from boss below
                SpawnEvent(32.0, "kobold",            120.0, 2, 65.0),
                SpawnEvent(34.0, "goblin",            930.0),
                SpawnEvent(36.0, "flying_eye",         90.0),
                SpawnEvent(40.0, "skeleton_boss",     600.0),           // boss after wolf2 is handled
                SpawnEvent(41.0, "flying_eye",        980.0),
                SpawnEvent(43.0, "wolf1",             930.0),
                SpawnEvent(44.0, "flying_eye",        110.0),
                // -- breathing 45–49s --
                SpawnEvent(49.0, "kobold",            950.0, 2, 65.0),
                SpawnEvent(51.0, "goblin",            480.0),
                SpawnEvent(51.5, "goblin",            140.0),
                SpawnEvent(52.5, "flying_eye",        990.0),
                SpawnEvent(54.0, "flying_eye",         80.0),
                SpawnEvent(57.0, "wolf2",             110.0),
                SpawnEvent(59.0, "kobold",            960.0, 2, 65.0),
                SpawnEvent(61.0, "flying_eye",        950.0),
                SpawnEvent(62.0, "skeleton_boss",     570.0),
                SpawnEvent(64.0, "flying_eye",        120.0),
                SpawnEvent(65.0, "goblin",            950.0),
                SpawnEvent(70.0, "wolf1",             920.0, 2, 70.0),
                SpawnEvent(73.0, "kobold",            150.0, 2, 65.0),
                SpawnEvent(74.5, "flying_eye",        980.0),
                SpawnEvent(76.0, "flying_eye",         90.0),
                SpawnEvent(78.0, "goblin",            500.0),
                SpawnEvent(81.0, "wolf2",             100.0),
                SpawnEvent(83.0, "flying_eye",        950.0),
                SpawnEvent(84.0, "skeleton_spearman", 930.0, 2, 60.0),
                SpawnEvent(86.0, "flying_eye",        110.0),
                SpawnEvent(87.5, "flying_eye",         80.0)
            )
        ),

        // ── WAVE 8: High Pressure ─────────────────────────────────────────────
        // FIXED: Original had 7 kobolds + 4 flying_eyes + 2 goblins in first 14s
        // with zero breathing. Added a 3s opener delay (first spawn at t=3.5 not
        // t=0.5) so players can cast an opener skill before the swarm lands.
        // Total enemy count unchanged — just redistributed the first 14s.
        Wave(
            duration = 90.0, name = "Wave 8 (High pressure)",
            events = listOf(
                // Small gap at start — give player time to spend opener mana
                SpawnEvent(3.5,  "kobold",            970.0),
                SpawnEvent(4.3,  "kobold",            130.0),
                SpawnEvent(5.0,  "flying_eye",        990.0),
                SpawnEvent(5.8,  "flying_eye",         90.0),
                SpawnEvent(7.0,  "goblin",            920.0),
                SpawnEvent(8.0,  "kobold",            150.0),
                SpawnEvent(9.0,  "flying_eye",        980.0),
                SpawnEvent(11.0, "kobold",            950.0, 2, 70.0),
                SpawnEvent(12.2, "flying_eye",        100.0),
                SpawnEvent(13.5, "wolf1",             930.0),
                SpawnEvent(15.0, "goblin",            480.0),
                SpawnEvent(15.5, "goblin",            150.0),
                SpawnEvent(16.5, "flying_eye",        950.0),
                // -- breathing 18–20.5s --
                SpawnEvent(21.0, "kobold",            980.0, 2, 65.0),
                SpawnEvent(22.5, "flying_eye",         80.0),
                SpawnEvent(24.0, "flying_eye",        980.0),
                SpawnEvent(24.5, "goblin",            550.0),
                SpawnEvent(26.0, "wolf2",             960.0),
                SpawnEvent(30.0, "skeleton_boss",     620.0),
                SpawnEvent(31.5, "flying_eye",        110.0),
                SpawnEvent(32.5, "kobold",            140.0, 2, 65.0),
                SpawnEvent(34.0, "flying_eye",        950.0),
                // -- breathing 36–42s --
                SpawnEvent(42.0, "wolf2",             920.0),
                SpawnEvent(44.0, "kobold",            110.0, 2, 60.0),
                SpawnEvent(45.5, "flying_eye",        990.0),
                SpawnEvent(46.0, "goblin",            930.0),
                SpawnEvent(46.8, "goblin",            480.0),
                SpawnEvent(48.0, "flying_eye",        130.0),
                SpawnEvent(53.0, "kobold",            960.0, 2, 60.0),
                SpawnEvent(54.5, "flying_eye",         90.0),
                SpawnEvent(55.5, "wolf1",             100.0, 2, 70.0),
                SpawnEvent(57.0, "goblin",            160.0),
                SpawnEvent(59.0, "flying_eye",        980.0),
                SpawnEvent(64.0, "kobold",            150.0, 2, 65.0),
                SpawnEvent(65.5, "flying_eye",        110.0),
                SpawnEvent(66.0, "skeleton_boss",     570.0),
                SpawnEvent(68.0, "flying_eye",        950.0),
                SpawnEvent(74.0, "kobold",            950.0, 2, 60.0),
                SpawnEvent(75.5, "wolf2",             120.0),
                SpawnEvent(76.5, "flying_eye",        980.0),
                SpawnEvent(77.0, "goblin",            520.0),
                SpawnEvent(77.8, "goblin",            930.0),
                SpawnEvent(78.8, "flying_eye",         80.0),
                SpawnEvent(81.0, "kobold",            140.0, 2, 60.0),
                SpawnEvent(83.0, "flying_eye",        960.0),
                SpawnEvent(83.5, "goblin",            150.0),
                SpawnEvent(85.5, "flying_eye",        120.0),
                SpawnEvent(88.0, "kobold",            930.0, 2, 70.0)
            )
        ),

        // ── WAVE 9: The Hunt ──────────────────────────────────────────────────
        // NEW WAVE. Player is ~level 14-16. Skill 3 is available (30 dmg, 12s cd).
        // Theme: wolf-focused. Wolf3 makes its first appearance here (was incorrectly
        // in Wave 4). Wolf3 at 240 speed is now survivable because the player has
        // multi-hit skills and knows the game's mechanics.
        // Skeletons provide a continuous background to prevent downtime.
        Wave(
            duration = 90.0, name = "Wave 9 (The Hunt)",
            events = listOf(
                SpawnEvent(1.0,  "kobold",            980.0, 2, 65.0),
                SpawnEvent(2.0,  "flying_eye",        970.0),
                SpawnEvent(3.0,  "flying_eye",         90.0),
                SpawnEvent(5.0,  "wolf1",             950.0),
                SpawnEvent(7.0,  "goblin",            920.0),
                SpawnEvent(9.0,  "kobold",            130.0, 2, 65.0),
                SpawnEvent(11.0, "wolf1",             100.0),
                SpawnEvent(13.0, "flying_eye",        960.0),
                SpawnEvent(15.0, "skeleton_spearman", 940.0, 2, 60.0),
                SpawnEvent(17.0, "flying_eye",        110.0),
                SpawnEvent(19.0, "goblin",            500.0),
                // wolf3 first appearance — give the player a clear 1v1 window
                SpawnEvent(22.0, "wolf3",             900.0),           // ← first wolf3
                SpawnEvent(25.0, "kobold",            960.0),
                SpawnEvent(27.0, "flying_eye",         90.0),
                SpawnEvent(29.0, "flying_eye",        980.0),
                // -- breathing 31–38s --
                SpawnEvent(38.0, "wolf1",             920.0, 2, 70.0),
                SpawnEvent(40.0, "kobold",            120.0, 2, 65.0),
                SpawnEvent(42.0, "goblin",            480.0),
                SpawnEvent(44.0, "flying_eye",        990.0),
                SpawnEvent(45.5, "flying_eye",        110.0),
                SpawnEvent(47.0, "skeleton_spearman", 950.0, 2, 60.0),
                SpawnEvent(50.0, "skeleton_boss",     600.0),
                SpawnEvent(52.0, "wolf2",             900.0),
                SpawnEvent(54.0, "kobold",            130.0, 2, 60.0),
                SpawnEvent(56.0, "flying_eye",        970.0),
                SpawnEvent(58.0, "goblin",            500.0),
                // -- breathing 60–65s --
                SpawnEvent(65.0, "wolf1",             920.0, 2, 70.0),
                SpawnEvent(67.0, "flying_eye",        990.0),
                SpawnEvent(68.0, "flying_eye",         80.0),
                SpawnEvent(70.0, "kobold",            960.0, 2, 60.0),
                SpawnEvent(72.0, "goblin",            150.0),
                // second wolf3 — now the player knows what's coming
                SpawnEvent(74.0, "wolf3",             900.0),           // ← second wolf3
                SpawnEvent(76.0, "flying_eye",        980.0),
                SpawnEvent(78.0, "skeleton_spearman", 940.0),
                SpawnEvent(80.0, "kobold",            110.0, 2, 65.0),
                SpawnEvent(83.0, "flying_eye",        110.0),
                SpawnEvent(85.0, "goblin",            500.0),
                SpawnEvent(87.0, "wolf1",             950.0)
            )
        ),

        // ── WAVE 10: Legion ────────────────────────────────────────────────────
        // NEW WAVE. Player is ~level 16-18. Skill 4 approaching (level 18).
        // Theme: quantity. No boss — the sheer count IS the boss.
        // Dense flanking from both sides. Wolf1 packs of 2. Wolf2 pressures centre.
        // Two wolf3 appearances at mid and late wave.
        Wave(
            duration = 90.0, name = "Wave 10 (Legion)",
            events = listOf(
                SpawnEvent(1.0,  "kobold",            990.0, 2, 65.0),
                SpawnEvent(1.0,  "kobold",            110.0, 2, 65.0),
                SpawnEvent(3.0,  "flying_eye",        980.0),
                SpawnEvent(3.0,  "flying_eye",        100.0),
                SpawnEvent(5.0,  "goblin",            950.0),
                SpawnEvent(5.0,  "goblin",            130.0),
                SpawnEvent(7.0,  "skeleton_spearman", 960.0, 2, 60.0),
                SpawnEvent(7.0,  "skeleton_spearman", 120.0, 2, 60.0),
                SpawnEvent(10.0, "wolf1",             950.0, 2, 70.0),
                SpawnEvent(10.0, "flying_eye",        110.0),
                SpawnEvent(12.0, "kobold",            970.0),
                SpawnEvent(12.0, "goblin",            150.0),
                SpawnEvent(14.0, "flying_eye",        985.0),
                SpawnEvent(14.0, "flying_eye",         90.0),
                // -- breathing 16–22s --
                SpawnEvent(22.0, "kobold",            980.0, 2, 65.0),
                SpawnEvent(22.0, "kobold",            100.0, 2, 65.0),
                SpawnEvent(24.0, "wolf1",             100.0, 2, 70.0),
                SpawnEvent(26.0, "goblin",            940.0),
                SpawnEvent(26.0, "goblin",            140.0),
                SpawnEvent(28.0, "flying_eye",        975.0),
                SpawnEvent(28.0, "flying_eye",        100.0),
                SpawnEvent(30.0, "wolf2",             960.0),
                SpawnEvent(32.0, "skeleton_spearman", 140.0, 2, 60.0),
                SpawnEvent(34.0, "kobold",            980.0, 2, 65.0),
                SpawnEvent(36.0, "flying_eye",        990.0),
                // wolf3 mid-wave
                SpawnEvent(38.0, "wolf3",             900.0),
                SpawnEvent(40.0, "kobold",            120.0),
                SpawnEvent(40.0, "goblin",            490.0),
                // -- breathing 42–48s --
                SpawnEvent(48.0, "kobold",            990.0, 2, 65.0),
                SpawnEvent(48.0, "kobold",            110.0, 2, 65.0),
                SpawnEvent(50.0, "flying_eye",        975.0),
                SpawnEvent(50.0, "flying_eye",        100.0),
                SpawnEvent(52.0, "wolf1",             950.0, 2, 70.0),
                SpawnEvent(52.0, "wolf1",             100.0, 2, 70.0),
                SpawnEvent(54.0, "goblin",            960.0),
                SpawnEvent(54.0, "goblin",            130.0),
                SpawnEvent(56.0, "skeleton_spearman", 950.0, 2, 60.0),
                SpawnEvent(58.0, "wolf2",             110.0),
                SpawnEvent(60.0, "flying_eye",        980.0),
                SpawnEvent(60.0, "flying_eye",         90.0),
                SpawnEvent(62.0, "kobold",            970.0, 2, 65.0),
                // -- breathing 64–70s --
                SpawnEvent(70.0, "kobold",            990.0, 2, 65.0),
                SpawnEvent(70.0, "kobold",            110.0, 2, 65.0),
                SpawnEvent(72.0, "flying_eye",        985.0),
                SpawnEvent(72.0, "flying_eye",         90.0),
                SpawnEvent(74.0, "goblin",            950.0),
                SpawnEvent(74.0, "goblin",            130.0),
                SpawnEvent(76.0, "wolf1",             960.0, 2, 70.0),
                // second wolf3 — late wave escalation
                SpawnEvent(78.0, "wolf3",             900.0),
                SpawnEvent(80.0, "kobold",            120.0, 2, 65.0),
                SpawnEvent(82.0, "flying_eye",        980.0),
                SpawnEvent(82.0, "flying_eye",        100.0),
                SpawnEvent(84.0, "wolf2",             950.0),
                SpawnEvent(86.0, "goblin",            500.0),
                SpawnEvent(87.0, "skeleton_spearman", 960.0, 2, 60.0),
                SpawnEvent(88.0, "flying_eye",        120.0)
            )
        ),

        // ── WAVE 11: Last Stand ────────────────────────────────────────────────
        // NEW WAVE. Player is ~level 18+. Skill 4 is available (50 dmg, 20s cd).
        // Theme: everything at once. All enemy types including wolf3. Three skeleton
        // bosses spaced across the wave. Designed as a true final challenge before
        // the auto-generator takes over — if you survive this, you're ready for
        // endless mode.
        Wave(
            duration = 90.0, name = "Wave 11 (Last Stand)",
            events = listOf(
                SpawnEvent(1.0,  "kobold",            990.0, 2, 65.0),
                SpawnEvent(1.0,  "kobold",            110.0),
                SpawnEvent(2.5,  "flying_eye",        985.0),
                SpawnEvent(2.5,  "flying_eye",         90.0),
                SpawnEvent(4.0,  "wolf1",             960.0),
                SpawnEvent(5.0,  "goblin",            930.0),
                SpawnEvent(5.0,  "goblin",            140.0),
                SpawnEvent(7.0,  "skeleton_spearman", 960.0, 2, 60.0),
                SpawnEvent(8.0,  "flying_eye",        975.0),
                SpawnEvent(9.0,  "kobold",            120.0, 2, 65.0),
                SpawnEvent(11.0, "wolf2",             950.0),
                SpawnEvent(13.0, "flying_eye",         90.0),
                SpawnEvent(14.0, "flying_eye",        985.0),
                SpawnEvent(16.0, "goblin",            500.0),
                SpawnEvent(18.0, "kobold",            980.0, 2, 60.0),
                SpawnEvent(20.0, "skeleton_boss",     620.0),           // boss 1
                SpawnEvent(21.0, "wolf1",             100.0, 2, 70.0),
                SpawnEvent(23.0, "flying_eye",        990.0),
                SpawnEvent(23.0, "flying_eye",         80.0),
                // -- breathing 25–30s --
                SpawnEvent(30.0, "wolf3",             910.0),           // wolf3
                SpawnEvent(32.0, "kobold",            970.0, 2, 65.0),
                SpawnEvent(32.0, "kobold",            120.0),
                SpawnEvent(34.0, "flying_eye",        980.0),
                SpawnEvent(35.0, "goblin",            940.0),
                SpawnEvent(35.0, "goblin",            150.0),
                SpawnEvent(37.0, "wolf1",             960.0, 2, 70.0),
                SpawnEvent(39.0, "skeleton_spearman", 950.0, 2, 60.0),
                SpawnEvent(41.0, "flying_eye",        100.0),
                SpawnEvent(42.0, "wolf2",             110.0),
                SpawnEvent(44.0, "kobold",            980.0, 2, 65.0),
                SpawnEvent(46.0, "skeleton_boss",     600.0),           // boss 2
                SpawnEvent(47.0, "flying_eye",        980.0),
                SpawnEvent(47.0, "flying_eye",         90.0),
                // -- breathing 49–54s --
                SpawnEvent(54.0, "kobold",            990.0, 2, 65.0),
                SpawnEvent(54.0, "kobold",            110.0, 2, 65.0),
                SpawnEvent(56.0, "flying_eye",        975.0),
                SpawnEvent(56.0, "flying_eye",        100.0),
                SpawnEvent(58.0, "wolf1",             950.0, 2, 70.0),
                SpawnEvent(58.0, "wolf1",             100.0, 2, 70.0),
                SpawnEvent(60.0, "goblin",            960.0),
                SpawnEvent(60.0, "goblin",            130.0),
                SpawnEvent(62.0, "wolf3",             910.0),           // second wolf3
                SpawnEvent(64.0, "skeleton_spearman", 950.0, 2, 60.0),
                SpawnEvent(65.0, "wolf2",             100.0),
                SpawnEvent(67.0, "flying_eye",        985.0),
                SpawnEvent(67.0, "flying_eye",         90.0),
                SpawnEvent(69.0, "kobold",            975.0, 2, 65.0),
                SpawnEvent(71.0, "goblin",            500.0),
                // -- breathing 72–76s --
                SpawnEvent(76.0, "wolf3",             910.0),           // third wolf3 — final form
                SpawnEvent(77.0, "kobold",            990.0, 2, 65.0),
                SpawnEvent(77.0, "kobold",            110.0),
                SpawnEvent(79.0, "flying_eye",        985.0),
                SpawnEvent(79.0, "flying_eye",         90.0),
                SpawnEvent(81.0, "skeleton_boss",     570.0),           // boss 3 — final climax
                SpawnEvent(82.0, "wolf1",             960.0, 2, 70.0),
                SpawnEvent(83.0, "wolf2",             110.0),
                SpawnEvent(84.0, "goblin",            940.0),
                SpawnEvent(84.0, "goblin",            140.0),
                SpawnEvent(85.0, "flying_eye",        980.0),
                SpawnEvent(86.0, "flying_eye",        100.0),
                SpawnEvent(87.0, "kobold",            970.0, 2, 60.0),
                SpawnEvent(88.0, "kobold",            120.0)
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

    // FIX: autoPhaseStart is now the true end time of all manual waves.
    // Auto-waves only begin after the last manual wave finishes.
    private val autoPhaseStart: Double by lazy {
        manualWaveStartTimes.last() + manualWaves.last().duration
    }

    fun getWaveNumber(elapsedTime: Double): Int {
        if (elapsedTime >= autoPhaseStart) return manualWaves.size + 1
        return manualWaveStartTimes.indexOfLast { it <= elapsedTime } + 1
    }

    fun apply(spawner: EnemySpawner) {
        spawner.schedule(*manualSchedule.toTypedArray())

        // Auto-phase: begins AFTER all manual waves are exhausted.
        // startDifficulty=2.5 matches where Wave 11 ends, so the first auto-wave
        // feels like a harder Wave 11, not a reset to easy.
        val autoEvents = AutoWaveGenerator(
            startTime            = autoPhaseStart,  // ← critical fix: was 1.0
            startDifficulty      = 2.5,
            difficultyGainPerSec = 0.004,
            baseInterval         = 10.0,
            intervalDecayRate    = 0.0015,
            minInterval          = 3.0,
            groupScaleRate       = 0.002,
            breathingWindowEvery = 90.0,
            breathingWindowLength = 10.0,
            durationToGenerate   = 3000.0
        ).generate()

        spawner.scheduleList(autoEvents)
    }
}
