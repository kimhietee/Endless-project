/**
 * EXAMPLE: How to use the EnemySpawner system in a Scene.
 * 
 * This shows:
 * 1. Creating a spawner
 * 2. Scheduling spawn events
 * 3. Updating the spawner each frame
 * 4. Accessing spawned enemies for display logic
 * 
 * Copy this pattern into your GameScene or similar.
 */

// ============================================================
// EXAMPLE SCENE SETUP
// ============================================================

/*

// Inside your GameScene or similar class:

class GameScene : Scene() {
    
    private val player = ... // Your player character
    private val enemyContainer = container()
    private val spawner = EnemySpawner(enemyContainer)
    
    override suspend fun Container.sceneInit() {
        // Load game assets
        GameAssets.load()
        
        // -------------------------------------------------------
        // SCHEDULE ENEMIES
        // -------------------------------------------------------
        // Times are in seconds since game start
        spawner.schedule(
            // Start with some basic skeletons
            SpawnEvent(5.0, "skeleton", 850.0),
            SpawnEvent(7.0, "skeleton", 900.0),
            SpawnEvent(8.5, "skeleton", 800.0),
            
            // Add spearmen
            SpawnEvent(15.0, "skeleton_spearman", 900.0),
            SpawnEvent(18.0, "skeleton_spearman", 800.0),
            
            // Add archers from far away
            SpawnEvent(25.0, "skeleton_archer", 950.0),
            SpawnEvent(28.0, "skeleton_archer", 750.0),
            
            // Add wolves
            SpawnEvent(35.0, "wolf1", 900.0),
            SpawnEvent(38.0, "wolf2", 850.0),
            SpawnEvent(40.0, "wolf3", 800.0),
            
            // Mixed waves
            SpawnEvent(50.0, "skeleton", 900.0),
            SpawnEvent(50.5, "wolf1", 850.0),
            SpawnEvent(51.0, "skeleton_archer", 950.0),
            SpawnEvent(52.0, "skeleton_spearman", 800.0)
        )
        
        // Example: spawn three skeletons at the same time with horizontal spacing
        spawner.schedule(
            SpawnEvent(time = 60.0, enemyType = "skeleton", x = 880.0, count = 3, offsetX = 50.0)
        )
        
        // -------------------------------------------------------
        // MAIN GAME LOOP
        // -------------------------------------------------------
        addUpdater {
            // Update the spawner
            // This spawns enemies at the right time, updates their AI,
            // and removes dead enemies
            spawner.update(
                dt = dt.seconds,
                playerX = player.x,
                targets = listOf(player)
            )
            
            // Optional: access the enemies for UI or other logic
            val enemyCount = spawner.getEnemyCount()
            val elapsedSeconds = spawner.getElapsedTime()
            println("Enemies: $enemyCount | Time: $elapsedSeconds")
        }
    }
}


// ============================================================
// WAVE-BASED SPAWNING (ALTERNATIVE)
// ============================================================

// If you want to spawn waves dynamically instead of hardcoding times:

class WaveManager(val spawner: EnemySpawner) {
    
    private var waveNumber = 0
    
    fun startWave(delayFromNow: Double = 0.0) {
        val startTime = spawner.getElapsedTime() + delayFromNow
        
        when (waveNumber) {
            0 -> {
                // Wave 1: Basic skeletons
                spawner.schedule(
                    SpawnEvent(startTime,      "skeleton", 850.0),
                    SpawnEvent(startTime + 2.0, "skeleton", 900.0),
                    SpawnEvent(startTime + 4.0, "skeleton", 800.0)
                )
            }
            1 -> {
                // Wave 2: Mixed
                spawner.schedule(
                    SpawnEvent(startTime,      "skeleton_spearman", 900.0),
                    SpawnEvent(startTime + 2.0, "skeleton_archer", 950.0),
                    SpawnEvent(startTime + 4.0, "skeleton", 800.0)
                )
            }
            2 -> {
                // Wave 3: Wolves
                spawner.schedule(
                    SpawnEvent(startTime,      "wolf1", 850.0),
                    SpawnEvent(startTime + 1.5, "wolf2", 900.0),
                    SpawnEvent(startTime + 3.0, "wolf3", 800.0)
                )
            }
        }
        
        waveNumber++
    }
    
    /**
     * Check if all enemies are defeated, returns true if ready for next wave
     */
    fun isWaveComplete(): Boolean = spawner.getEnemyCount() == 0
}


// ============================================================
// DIFFICULTY SCALING (ANOTHER ALTERNATIVE)
// ============================================================

// Modify enemy stats based on difficulty level:

class DifficultyScaling {
    companion object {
        fun applyDifficulty(config: EnemyConfig, difficulty: Int): EnemyConfig {
            val healthMultiplier = 1.0 + (difficulty * 0.15)
            val damageMultiplier = 1.0 + (difficulty * 0.10)
            
            return config.copy(
                maxHealth = (config.maxHealth * healthMultiplier),
                attackDisplayConfig = config.attackDisplayConfig.copy(
                    damage = config.attackDisplayConfig.damage * damageMultiplier
                )
            )
        }
    }
}

// Usage in spawner:
// val difficulty = 2
// val config = EnemyConfigs.skeleton(attackFrames)
// val scaledConfig = DifficultyScaling.applyDifficulty(config, difficulty)


// ============================================================
// POSITION STRATEGIES (WHERE ENEMIES SPAWN)
// ============================================================

// Common spawn positions:
object SpawnPositions {
    const val FAR_RIGHT = 950.0
    const val RIGHT = 900.0
    const val CENTER = 850.0
    const val LEFT = 800.0
    const val FAR_LEFT = 750.0
}

// Usage:
// SpawnEvent(10.0, "skeleton", SpawnPositions.RIGHT)


 */
