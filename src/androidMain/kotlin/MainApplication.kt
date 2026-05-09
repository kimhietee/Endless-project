package com.kimhietee.endless

import android.app.Application
import com.google.firebase.FirebaseApp

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            println("[Firebase] Android - Initialized via Application class")
        } catch (e: Exception) {
            println("[Firebase] Android - Init failed: ${e.message}")
        }
    }
}