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

    fun isInDemoMode(): Boolean {
        auth
        return !isFirebaseAvailable
    }

    fun isLoggedIn(): Boolean = auth?.currentUser != null
    fun isGuest(): Boolean = !isLoggedIn()
    fun userId(): String? = auth?.currentUser?.uid

    fun userLabel(): String {
        if (auth != null && auth!!.currentUser != null) {
            return auth!!.currentUser?.displayName ?: auth!!.currentUser?.email ?: "User"
        }
        return "Guest"
    }

    /**
     * Attempts to sign in with email and password.
     * Returns null on success, or a user-friendly error message on failure.
     */
    suspend fun signIn(email: String, password: String): String? {
        if (email.isEmpty() && password.isEmpty()) return "Please enter your email and password."
        if (email.isEmpty()) return "Please enter your email."
        if (password.isEmpty()) return "Please enter your password."
        if (!email.contains("@")) return "Please enter a valid email address."

        return try {
            if (auth != null) {
                auth!!.signInWithEmailAndPassword(email, password)
                null
            } else {
                val desktopError = "Firebase not available on Desktop. Test on Android."
                println("[Auth] $desktopError")
                desktopError
            }
        } catch (e: Exception) {
            mapSignInError(e)
        }
    }

    /**
     * Attempts to create a new user with email and password.
     * Returns null on success, or a user-friendly error message on failure.
     */
    suspend fun signUp(email: String, password: String): String? {
        if (email.isEmpty() && password.isEmpty()) return "Please enter your email and password."
        if (email.isEmpty()) return "Please enter your email."
        if (password.isEmpty()) return "Please enter your password."
        if (!email.contains("@")) return "Please enter a valid email address."
        if (password.length < 6) return "Password must be at least 6 characters."

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
            mapSignUpError(e)
        }
    }

    suspend fun logout() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            println("[Auth] Logout error: ${e.message}")
        }
    }

    // ── Error mapping for SIGN IN ────────────────────────────────────────────
    // Keep it simple: never reveal which field is wrong (security best practice),
    // but do give the "account not found" hint so the user knows to sign up.
    private fun mapSignInError(e: Exception): String {
        val msg = e.message ?: ""
        println("[Auth] Sign-in failed: $msg")
        return when {
            // Account does not exist at all
            msg.contains("user-not-found", ignoreCase = true) ||
            msg.contains("USER_NOT_FOUND", ignoreCase = true) ->
                "No account found with that email. Please sign up first."

            // Wrong password — intentionally vague for security
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("INVALID_PASSWORD", ignoreCase = true) ||
            msg.contains("wrong-password", ignoreCase = true) ->
                "Wrong email or password. Please try again."

            msg.contains("invalid-email", ignoreCase = true) ->
                "Please enter a valid email address."

            msg.contains("user-disabled", ignoreCase = true) ->
                "This account has been disabled. Contact support."

            msg.contains("too-many-requests", ignoreCase = true) ->
                "Too many attempts. Please wait a moment and try again."

            msg.contains("network", ignoreCase = true) ->
                "Network error. Check your connection and try again."

            else -> "Sign in failed. Please check your details and try again."
        }
    }

    // ── Error mapping for SIGN UP ────────────────────────────────────────────
    private fun mapSignUpError(e: Exception): String {
        val msg = e.message ?: ""
        println("[Auth] Sign-up failed: $msg")
        return when {
            // Account already exists — tell them to log in instead
            msg.contains("email-already-in-use", ignoreCase = true) ||
            msg.contains("EMAIL_EXISTS", ignoreCase = true) ->
                "An account with this email already exists. Please log in."

            msg.contains("invalid-email", ignoreCase = true) ->
                "Please enter a valid email address."

            msg.contains("weak-password", ignoreCase = true) ->
                "Password is too weak. Use at least 6 characters."

            msg.contains("too-many-requests", ignoreCase = true) ->
                "Too many attempts. Please wait a moment and try again."

            msg.contains("network", ignoreCase = true) ->
                "Network error. Check your connection and try again."

            else -> "Sign up failed. Please try again."
        }
    }
}