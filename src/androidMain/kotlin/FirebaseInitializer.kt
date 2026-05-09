package managers

import android.content.Context
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

actual fun configureFirebase(): String? {
    return null // Android initializes via MainApplication
}