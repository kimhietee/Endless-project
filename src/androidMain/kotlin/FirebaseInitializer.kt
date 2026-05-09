package managers

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

actual fun configureFirebase(): String? {
    return try {
        // On Android, Firebase is auto-initialized by google-services.json
        // but we call Firebase.initialize() to ensure it's ready
        // dev.gitlive.firebase handles Android context automatically via ContentProvider
        println("[Firebase] Android mode - initializing Firebase")
        // Firebase.initialize is handled automatically on Android via google-services plugin
        // Just verify Firebase is accessible
        val auth = Firebase.auth
        println("[Firebase] Android Firebase initialized successfully, auth=$auth")
        null
    } catch (e: Exception) {
        val error = "Android Firebase init failed: ${e.message?.take(100)}"
        println("[Firebase] $error")
        println("[Firebase] Exception class: ${e::class.simpleName}")
        error
    }
}
