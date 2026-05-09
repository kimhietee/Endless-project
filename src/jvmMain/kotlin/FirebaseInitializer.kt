package managers

actual fun configureFirebase(): String? {
    // On JVM/Desktop, skip actual Firebase initialization
    // The AuthManager.kt will use demo mode fallback for authentication
    println("[Firebase] JVM/Desktop mode - using demo authentication")
    return null
}
