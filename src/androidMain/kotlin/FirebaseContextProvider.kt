package com.kimhietee.endless

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class FirebaseContextProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val ctx = context?.applicationContext ?: return false
        try {
            Firebase.initialize(ctx)
            println("[Firebase] Initialized via ContentProvider")
        } catch (e: Exception) {
            println("[Firebase] Init failed: ${e.message}")
        }
        return true
    }
    override fun query(uri: Uri, p: Array<String>?, s: String?, sa: Array<String>?, so: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, sa: Array<String>?): Int = 0
    override fun update(uri: Uri, v: ContentValues?, s: String?, sa: Array<String>?): Int = 0
}