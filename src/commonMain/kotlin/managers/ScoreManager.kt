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
 */
object ScoreManager {
    private val firestore = Firebase.firestore

    /**
     * Evaluates the current game results and updates Firestore if a new high score is achieved.
     * 
     * @param currentScore The score achieved in the current run.
     * @param timeSurvived Time in seconds the player survived.
     * @param wavesCleared Total number of waves cleared.
     * @param kills Total number of enemies defeated.
     */
    fun onGameEnd(currentScore: Double, timeSurvived: Double, wavesCleared: Int, kills: Int) {
        // Only track high scores for logged-in users
        if (AuthManager.isGuest()) {
            println("Guest session ended. Score not saved.")
            return
        }

        val uid = AuthManager.userId() ?: return

        // Use a background coroutine to handle Firestore interaction
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val userDoc = firestore.collection("users").document(uid)
                val snapshot = userDoc.get()
                
                val best = if (snapshot.exists) {
                    snapshot.data<UserHighScore>()
                } else {
                    UserHighScore()
                }

                // Logic: If the current score is higher than the best recorded score,
                // we update the document with the new bests for all metrics.
                // Alternatively, we could update each metric independently if it's a new record for that metric.
                // The prompt says "If it's a new personal high score, automatically update".
                
                if (currentScore > best.score) {
                    val newBest = UserHighScore(
                        score = currentScore,
                        timeSurvived = maxOf(timeSurvived, best.timeSurvived),
                        wavesCleared = maxOf(wavesCleared, best.wavesCleared),
                        totalKills = maxOf(kills, best.totalKills)
                    )
                    userDoc.set(newBest)
                    println("Cloud Save: New High Score reached! ($currentScore)")
                } else {
                    println("Game ended. Best score remains ${best.score}")
                }
            } catch (e: Exception) {
                println("Error saving score to Firebase: ${e.message}")
            }
        }
    }

    /**
     * Fetches the current user's high score data from Firestore.
     */
    suspend fun getHighScore(): UserHighScore {
        if (AuthManager.isGuest()) return UserHighScore()
        val uid = AuthManager.userId() ?: return UserHighScore()
        
        return try {
            val snapshot = firestore.collection("users").document(uid).get()
            if (snapshot.exists) {
                snapshot.data<UserHighScore>()
            } else {
                UserHighScore()
            }
        } catch (e: Exception) {
            println("Error fetching high score: ${e.message}")
            UserHighScore()
        }
    }
}
