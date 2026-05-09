package managers

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Data class representing the user's highest achievements.
 */
@Serializable
data class UserHighScore(
    val score: Double = 0.0,
    val timeSurvived: Double = 0.0,
    val wavesCleared: Int = 0,
    val totalKills: Int = 0
)

/**
 * Manages the high score system and Firestore integration.
 * 
 * FIX 3: Uses AuthManager.isInDemoMode() to check firebase availability with clear [DESKTOP] logging.
 * FIX 4: Accepts init(scope) for proper coroutine scope management instead of fire-and-forget.
 */
object ScoreManager {
    private var gameScope: CoroutineScope? = null
    
    private val firestore by lazy {
        try {
            Firebase.firestore
        } catch (e: Exception) {
            println("[Firestore] Firebase.firestore initialization failed: ${e.message}")
            null
        }
    }

    /**
     * Initialize ScoreManager with the game's coroutine scope.
     * FIX 4: Call this once from your GameScene or game initialization code.
     * 
     * Example in GameScene.kt:
     *   ScoreManager.init(CoroutineScope(Dispatchers.Main))
     */
    fun init(scope: CoroutineScope) {
        gameScope = scope
        println("[ScoreManager] Initialized with game scope")
    }

    /**
     * Evaluates the current game results and updates Firestore if a new high score is achieved.
     * 
     * FIX 3: Now uses AuthManager.isInDemoMode() instead of firestore == null check.
     * FIX 4: Uses stored gameScope for proper lifecycle management. Logs all failures visibly.
     * 
     * @param currentScore The score achieved in the current run.
     * @param timeSurvived Time in seconds the player survived.
     * @param wavesCleared Total number of waves cleared.
     * @param kills Total number of enemies defeated.
     */
    fun onGameEnd(currentScore: Double, timeSurvived: Double, wavesCleared: Int, kills: Int) {
        // Only track high scores for logged-in users
        if (AuthManager.isGuest()) {
            println("[ScoreManager] Guest session ended. Score not saved.")
            return
        }

        val uid = AuthManager.userId() ?: return

        // FIX 3: Use isInDemoMode() with clear [DESKTOP] label instead of silent firestore == null
        if (AuthManager.isInDemoMode()) {
            println("[DESKTOP] Firestore unavailable - score not saved. Run on Android to test.")
            return
        }

        // FIX 4: Use stored game scope or fallback to Default dispatcher
        val scope = gameScope ?: CoroutineScope(Dispatchers.Default)
        
        scope.launch {
            if (firestore == null) {
                println("[ScoreManager] ✗ Firestore initialization failed - score write skipped")
                return@launch
            }
            try {
                val userDoc = firestore!!.collection("users").document(uid)
                val snapshot = userDoc.get()
                
                val best = if (snapshot.exists) {
                    snapshot.data<UserHighScore>()
                } else {
                    UserHighScore()
                }

                if (currentScore > best.score) {
                    val newBest = UserHighScore(
                        score = currentScore,
                        timeSurvived = maxOf(timeSurvived, best.timeSurvived),
                        wavesCleared = maxOf(wavesCleared, best.wavesCleared),
                        totalKills = maxOf(kills, best.totalKills)
                    )
                    userDoc.set(newBest)
                    println("[ScoreManager] ✓ New High Score saved to Firestore! Score: $currentScore")
                } else {
                    println("[ScoreManager] Score update: Best remains ${best.score}")
                }
            } catch (e: Exception) {
                // FIX 4: Log all failures visibly instead of silent skip
                println("[ScoreManager] ✗ ERROR saving score to Firestore: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Fetches the current user's high score data from Firestore.
     * 
     * FIX 3: Now uses AuthManager.isInDemoMode() with clear [DESKTOP] label.
     * FIX 4: Visible error logging for fetch failures.
     */
    suspend fun getHighScore(): UserHighScore {
        if (AuthManager.isGuest()) {
            println("[ScoreManager] Guest - no high score available")
            return UserHighScore()
        }
        
        val uid = AuthManager.userId() ?: return UserHighScore()
        
        // FIX 3: Use isInDemoMode() with clear [DESKTOP] label instead of silent firestore == null
        if (AuthManager.isInDemoMode()) {
            println("[DESKTOP] Firestore unavailable - high score not fetched. Run on Android to test.")
            return UserHighScore()
        }
        
        if (firestore == null) {
            println("[ScoreManager] ✗ Firestore not initialized - high score fetch skipped")
            return UserHighScore()
        }
        
        return try {
            val snapshot = firestore!!.collection("users").document(uid).get()
            if (snapshot.exists) {
                val score = snapshot.data<UserHighScore>()
                println("[ScoreManager] ✓ High score fetched: ${score.score}")
                score
            } else {
                println("[ScoreManager] No high score found for user $uid")
                UserHighScore()
            }
        } catch (e: Exception) {
            // FIX 4: Log all failures visibly instead of silent skip
            println("[ScoreManager] ✗ ERROR fetching high score: ${e.message}")
            e.printStackTrace()
            UserHighScore()
        }
    }
}