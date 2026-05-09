package managers

import android.content.Context
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

actual fun configureFirebase(): String? {
    return try {
        // Reflection-based hack to get the current application context on Android
        // without needing a custom Application class or ContentProvider in the manifest.
        // This is very reliable for KorGE/KMP where manifest merging can be tricky.
        val context = Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as? Context
        
        if (context != null) {
            println("[Firebase] Android - initializing Firebase manually with reflection context")
            Firebase.initialize(context)
            println("[Firebase] Android - Firebase initialized successfully")
            null
        } else {
            println("[Firebase] Android - Could not get application context via reflection")
            "Could not get application context"
        }
    } catch (e: Exception) {
        val error = "Android Firebase init failed: ${e.message?.take(100)}"
        println("[Firebase] $error")
        println("[Firebase] Exception class: ${e::class.simpleName}")
        error
    }
}