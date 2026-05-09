package com.kimhietee.endless

import android.app.Application
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Firebase.initialize(this)
            println("[Firebase] Android - Initialized successfully via dev.gitlive")
        } catch (e: Exception) {
            println("[Firebase] Android - Init failed: ${e.message}")
        }
    }
}