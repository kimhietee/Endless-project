package managers

actual fun configureFirebase(): String? {
    return try {
        com.google.firebase.FirebaseApp.getInstance()
        println("[Firebase] Android - Firebase initialized successfully")
        null
    } catch (e: IllegalStateException) {
        // FirebaseApp not initialized yet - this is normal, 
        // it auto-initializes via google-services.json
        println("[Firebase] Android - Firebase auto-initializing via google-services.json")
        null
    } catch (e: Exception) {
        val error = e.message ?: "Unknown error"
        println("[Firebase] Android - Firebase init failed: $error")
        error
    }
}