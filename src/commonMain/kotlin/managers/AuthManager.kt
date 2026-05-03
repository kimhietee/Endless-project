package managers

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

/**
 * Manages Firebase Authentication state.
 * Tracks if the current player is a guest or a logged-in user.
 */
object AuthManager {
    private val auth = Firebase.auth

    /** Returns true if there is an active Firebase user session. */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    /** Returns true if the player is playing as a guest (no active session). */
    fun isGuest(): Boolean = auth.currentUser == null

    /** Returns the unique Firebase UID for the logged-in user, or null if guest. */
    fun userId(): String? = auth.currentUser?.uid

    /** Returns the display name or email if available. */
    fun userLabel(): String = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Guest"

    /** Attempts to sign in with email and password. */
    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password)
            true
        } catch (e: Exception) {
            println("Sign in failed: ${e.message}")
            false
        }
    }

    /** Attempts to create a new user with email and password. */
    suspend fun signUp(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password)
            true
        } catch (e: Exception) {
            println("Sign up failed: ${e.message}")
            false
        }
    }

    /** Signs out the current user. */
    suspend fun logout() {
        auth.signOut()
    }
}
