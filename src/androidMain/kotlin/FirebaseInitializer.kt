package managers

actual fun configureFirebase(): String? {
    return try {
        val app = dev.gitlive.firebase.Firebase.app
        println("[Firebase] Android - Firebase initialized successfully")
        null
    } catch (e: Exception) {
        val error = e.message ?: "Unknown error"
        println("[Firebase] Android - Firebase init failed: $error")
        error
    }
}