package managers

import android.app.Application
import com.google.firebase.FirebaseApp

actual fun configureFirebase(): String? {
    return try {
        // Firebase on Android initializes automatically via google-services.json
        // This just confirms it's available
        val app = FirebaseApp.getInstance()
        println("[Firebase] Android - Firebase initialized: ${app.name}")
        null
    } catch (e: Exception) {
        println("[Firebase] Android - Firebase not initialized yet: ${e.message}")
        // FirebaseApp.initializeApp() requires a Context on Android
        // It is auto-initialized via the manifest on Android - this is expected
        null
    }
}