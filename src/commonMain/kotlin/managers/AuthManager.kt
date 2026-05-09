package managers

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

/**
 * Manages Firebase Authentication state.
 * Tracks if the current player is a guest or a logged-in user.
 * Supports both Firebase (mobile/web) and REST fallback (desktop).
 */
object AuthManager {
    
    private var demoUserId: String? = null
    private var demoEmail: String? = null
    
    // Lazy init catches errors when Firebase isn't available
    private val auth by lazy {
        try {
            Firebase.auth
        } catch (e: Exception) {
            println("Firebase.auth init failed (will use demo mode): ${e.message}")
            null
        }
    }

    /** Returns true if there is an active Firebase user session or demo session. */
    fun isLoggedIn(): Boolean = auth?.currentUser != null || demoUserId != null

    /** Returns true if the player is playing as a guest (no active session). */
    fun isGuest(): Boolean = !isLoggedIn()

    /** Returns the unique Firebase UID for the logged-in user, or null if guest. */
    fun userId(): String? = auth?.currentUser?.uid ?: demoUserId

    /** Returns the display name or email if available. */
    fun userLabel(): String {
        if (auth != null && auth!!.currentUser != null) {
            return auth!!.currentUser?.displayName ?: auth!!.currentUser?.email ?: "User"
        }
        return demoEmail ?: "Guest"
    }

    /** Attempts to sign in with email and password. Returns null on success, error message on failure. */
    suspend fun signIn(email: String, password: String): String? {
        if (email.isEmpty() || password.isEmpty()) return "Invalid email or password"
        if (!email.contains("@")) return "Invalid email"
        
        return try {
            if (auth != null) {
                auth!!.signInWithEmailAndPassword(email, password)
                null
            } else {
                // Demo mode for JVM/Desktop
                if (password.length < 6) return "Weak password"
                demoUserId = "demo_${System.currentTimeMillis()}"
                demoEmail = email
                println("[DEMO MODE] Signed in as $email")
                null
            }
        } catch (e: Exception) {
            mapFirebaseError(e)
        }
    }

    /** Attempts to create a new user with email and password. Returns null on success, error message on failure. */
    suspend fun signUp(email: String, password: String): String? {
        if (email.isEmpty() || password.isEmpty()) return "Invalid email or password"
        if (!email.contains("@")) return "Invalid email"
        if (password.length < 6) return "Weak password"
        
        return try {
            if (auth != null) {
                auth!!.createUserWithEmailAndPassword(email, password)
                null
            } else {
                // Demo mode for JVM/Desktop
                demoUserId = "demo_${System.currentTimeMillis()}"
                demoEmail = email
                println("[DEMO MODE] Signed up as $email")
                null
            }
        } catch (e: Exception) {
            mapFirebaseError(e)
        }
    }

    /** Signs out the current user. */
    suspend fun logout() {
        try {
            if (auth != null) {
                auth!!.signOut()
            }
        } catch (e: Exception) {
            println("Logout error: ${e.message}")
        }
        demoUserId = null
        demoEmail = null
    }
    
    private fun mapFirebaseError(e: Exception): String {
        val msg = e.message ?: ""
        println("Auth failed: $msg")
        return when {
            msg.contains("INVALID_LOGIN_CREDENTIALS") || msg.contains("INVALID_PASSWORD") || msg.contains("user-not-found") -> "Wrong credentials"
            msg.contains("email-already-in-use") -> "Email already in use"
            msg.contains("invalid-email") -> "Invalid email"
            msg.contains("weak-password") -> "Weak password"
            msg.contains("network") -> "Network error"
            else -> "Auth failed: ${msg.take(30)}"
        }
    }
}
