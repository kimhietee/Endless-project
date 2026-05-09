package managers

import android.app.Application
import com.google.firebase.FirebaseApp

actual fun configureFirebase(): String? {
    return try {
        FirebaseApp.getInstance()
        println("[Firebase] Android - Already initialized")
        null
    } catch (e: Exception) {
        println("[Firebase] Android - Not yet initialized: ${e.message}")
        null // Let it auto-initialize via ContentProvider
    }
}