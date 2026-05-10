package managers

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

/**
 * Manages Firebase Authentication state.
 * Tracks if the current player is a guest or a logged-in user.
 * On Desktop/JVM where Firebase is unavailable, returns clear error messages instead of faking success.
 */
object AuthManager {
    
    private var isFirebaseAvailable = false
    
    // Lazy init catches errors when Firebase isn't available
    private val auth by lazy {
        try {
            val authInstance = Firebase.auth
            isFirebaseAvailable = true
            println("[Auth] Firebase Authentication initialized successfully")
            authInstance
        } catch (e: Exception) {
            isFirebaseAvailable = false
            println("[Auth] Firebase.auth not available: ${e.message}")
            println("[Auth] Running on Desktop/JVM - Firebase features disabled")
            null
        }
    }

    /**
     * Returns true if Firebase is not available (e.g., on Desktop/JVM).
     * Use this to skip Firebase-dependent operations gracefully.
     */
    fun isInDemoMode(): Boolean {
        // Force lazy init of auth by referencing it
        auth // just reference to trigger lazy initialization
        return !isFirebaseAvailable
    }

    /** Returns true if there is an active Firebase user session. On Desktop, always false. */
    fun isLoggedIn(): Boolean = auth?.currentUser != null

    /** Returns true if the player is playing as a guest (no active session). */
    fun isGuest(): Boolean = !isLoggedIn()

    /** Returns the unique Firebase UID for the logged-in user, or null if guest or Desktop. */
    fun userId(): String? = auth?.currentUser?.uid

    /** Returns the display name or email if available. On Desktop, returns "Guest". */
    fun userLabel(): String {
        if (auth != null && auth!!.currentUser != null) {
            return auth!!.currentUser?.displayName ?: auth!!.currentUser?.email ?: "User"
        }
        return "Guest"
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
                // No fake demo - return clear error to UI
                val desktopError = "Firebase not available on Desktop. Test on Android."
                println("[Auth] $desktopError")
                desktopError
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
                val desktopError = "Firebase not available on Desktop. Test on Android."
                println("[Auth] $desktopError")
                desktopError
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
            println("[Auth] Logout error: ${e.message}")
        }
    }
    
    private fun mapFirebaseError(e: Exception): String {
        val msg = e.message ?: ""
        println("[Auth] Authentication failed: $msg")
        return when {
            msg.contains("INVALID_LOGIN_CREDENTIALS") || msg.contains("INVALID_PASSWORD") || msg.contains("user-not-found") -> "Wrong credentials"
            msg.contains("email-already-in-use") -> "This account is already registered. Please log in instead."
            msg.contains("invalid-email") -> "Invalid email"
            msg.contains("weak-password") -> "Weak password"
            msg.contains("network") -> "Network error"
            msg.contains("user-not-found") -> "Account not found."
            else -> "Auth failed: ${msg.take(30)}"
        }
    }
}