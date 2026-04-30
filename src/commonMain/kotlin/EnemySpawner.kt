import korlibs.korge.view.Container

/**
 * Represents a single scheduled enemy spawn event.
 * 
 * @param time      Exact time (in seconds from game start) when enemy should spawn
 * @param enemyType Type string ("skeleton", "wolf1", etc.)
 * @param x         X coordinate where enemy spawns
 * @param count     Number of enemies to spawn at this time
 * @param offsetX   X offset between spawned enemies so they do not overlap
 */
data class SpawnEvent(
    val time: Double,
    val enemyType: String,
    val x: Double,
    val count: Int = 1,
    val offsetX: Double = 0.0
)

/**
 * Spawner that manages enemy creation and lifecycle.
 * 
 * Features:
 * - Tracks elapsed time
 * - Spawns enemies at exact times from a schedule
 * - Uses EnemyFactory to create enemies
 * - Adds enemies to a container
 * - Updates all enemies each frame
 * - Removes dead enemies automatically
 * 
 * Usage:
 *   val spawner = EnemySpawner(container)
 *   
 *   // Schedule some enemies
 *   spawner.schedule(
 *       SpawnEvent(5.0, "skeleton", 800.0),
 *       SpawnEvent(7.0, "wolf1", 850.0)
 *   )
 *   
 *   // In main game loop updater:
 *   addUpdater {
 *       spawner.update(
 *           dt = dt.seconds,
 *           playerX = player.x,
 *           targets = listOf(player)
 *       )
 *   }
 */
class EnemySpawner(
    private val container: Container
) {
    private var elapsedTime = 0.0
    private val spawnSchedule = mutableListOf<SpawnEvent>()
    private val enemies = mutableListOf<Enemy>()
    private var nextSpawnIndex = 0

    // Queue of pending spawns — GameScene drains this each frame
    val pendingSpawns = mutableListOf<SpawnEvent>()

    fun schedule(vararg events: SpawnEvent) {
        spawnSchedule.addAll(events)
    }

    fun clear() {
        for (enemy in enemies) { enemy.removeFromParent() }
        spawnSchedule.clear()
        enemies.clear()
        pendingSpawns.clear()
        nextSpawnIndex = 0
        elapsedTime = 0.0
    }

    fun resetTime() {
        elapsedTime = 0.0
        nextSpawnIndex = 0
    }

    // Non-suspend, called directly from addUpdater
    fun update(
        dt: Double,
        playerX: Double,
        targets: List<Damageable>
    ) {
        elapsedTime += dt

        // Enqueue events whose time has come — does NOT spawn yet
        while (nextSpawnIndex < spawnSchedule.size) {
            val event = spawnSchedule[nextSpawnIndex]
            if (elapsedTime >= event.time) {
                pendingSpawns.add(event)
                nextSpawnIndex++
            } else {
                break
            }
        }

        // Update living enemies
        for (enemy in enemies) {
            enemy.update(dt, playerX, targets, container)
        }

        // Remove dead enemies
        val toRemove = enemies.filter { it.shouldRemove }
        for (enemy in toRemove) {
            enemies.remove(enemy)
            enemy.removeFromParent()
        }
    }

    // Called by GameScene after it has created enemies from pendingSpawns
    fun addEnemy(enemy: Enemy) {
        enemies.add(enemy)
    }

    fun getEnemies(): List<Enemy> = enemies.toList()
    fun getEnemyCount(): Int = enemies.size
    fun getElapsedTime(): Double = elapsedTime
    fun getScheduledEventCount(): Int = spawnSchedule.size
}
