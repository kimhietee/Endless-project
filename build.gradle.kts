import korlibs.korge.gradle.*
import korlibs.korge.gradle.Orientation
import java.io.File

plugins {
    alias(libs.plugins.korge)
    id("com.google.gms.google-services") version "4.4.4" apply false
}

korge {
    id = "com.kimhietee.endless"

    // To enable all targets at once
    //targetAll()

    // To enable targets based on properties/environment variables
    //targetDefault()

    // To selectively enable targets
    androidMinSdk = 23
    androidTargetSdk = 34
    androidCompileSdk = 34

    targetJvm()
    targetJs()
    targetWasmJs()
    targetDesktop()
    targetIos()
    targetAndroid()

    serializationJson()

    orientation = Orientation.LANDSCAPE
}

dependencies {
    add("commonMainApi", project(":deps"))
    add("commonMainImplementation", "dev.gitlive:firebase-auth:1.10.4")
    add("commonMainImplementation", "dev.gitlive:firebase-firestore:1.10.4")
    add("commonMainImplementation", "dev.gitlive:firebase-analytics:1.10.4")
}

// FIX 5: Copy google-services.json to correct locations for KorGE 6 Android builds
// In KorGE 6, the Android target uses the root project and :deps subproject.
// google-services.json must be at the root project dir for the Google Services plugin.
tasks.register("copyGoogleServices") {
    description = "Copy google-services.json to project root and deps subproject for Android builds"
    doLast {
        val sourceFile = File(project.projectDir, "src/androidMain/google-services.json")
        
        if (!sourceFile.exists()) {
            println("[Build] ⚠️  WARNING: google-services.json not found at ${sourceFile.absolutePath}")
            println("[Build] Firebase Android features will not work without this file")
            return@doLast
        }
        
        // Copy to project root (where Google Services plugin looks by default)
        val rootDest = File(project.projectDir, "google-services.json")
        sourceFile.copyTo(rootDest, overwrite = true)
        println("[Build] ✓ Copied google-services.json to project root: ${rootDest.absolutePath}")
        
        // Copy to deps subproject (which has the Android library plugin)
        val depsDest = File(project.projectDir, "deps/google-services.json")
        sourceFile.copyTo(depsDest, overwrite = true)
        println("[Build] ✓ Copied google-services.json to deps: ${depsDest.absolutePath}")
    }
}

// Ensure google-services.json is copied before any Android-related tasks
tasks.matching { it.name.contains("Android") || it.name.contains("android") }.configureEach {
    dependsOn("copyGoogleServices")
}

// Also copy at configuration time so it's available immediately
val sourceGoogleServices = File(project.projectDir, "src/androidMain/google-services.json")
if (sourceGoogleServices.exists()) {
    val rootDest = File(project.projectDir, "google-services.json")
    if (!rootDest.exists() || sourceGoogleServices.readText() != rootDest.readText()) {
        sourceGoogleServices.copyTo(rootDest, overwrite = true)
        println("[Build] ✓ (config-time) Copied google-services.json to project root")
    }
    val depsDest = File(project.projectDir, "deps/google-services.json")
    if (!depsDest.exists() || sourceGoogleServices.readText() != depsDest.readText()) {
        sourceGoogleServices.copyTo(depsDest, overwrite = true)
        println("[Build] ✓ (config-time) Copied google-services.json to deps/")
    }
}
