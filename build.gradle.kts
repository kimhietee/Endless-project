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
    add("commonMainImplementation", "dev.gitlive:firebase-auth:1.13.0")
    add("commonMainImplementation", "dev.gitlive:firebase-firestore:1.13.0")
    add("commonMainImplementation", "dev.gitlive:firebase-analytics:1.13.0")
}

// FIX 5: Verify and copy google-services.json to Android subproject before build
// The com.google.gms.google-services plugin looks for google-services.json in the Android app module
tasks.register("copyGoogleServices") {
    description = "Copy google-services.json to Android subproject if needed"
    doLast {
        val sourceFile = File(project.projectDir, "src/androidMain/google-services.json")
        
        if (!sourceFile.exists()) {
            println("[Build] ⚠️  WARNING: google-services.json not found at ${sourceFile.absolutePath}")
            println("[Build] Firebase Android features will not work without this file")
            return@doLast
        }
        
        // KorGE generates the Android subproject at build time
        // The google-services.json needs to be in: build/korge/android/app/google-services.json
        val androidAppDir = File(project.buildDir, "korge/android/app")
        if (androidAppDir.exists()) {
            val destFile = File(androidAppDir, "google-services.json")
            sourceFile.copyTo(destFile, overwrite = true)
            println("[Build] ✓ Copied google-services.json to Android app module")
        }
    }
}

// Apply Google Services plugin to the Android project once it is created by KorGE
subprojects {
    afterEvaluate {
        if (name == "android" || project.plugins.hasPlugin("com.android.application")) {
            apply(plugin = "com.google.gms.google-services")
            
            // Ensure google-services.json is in the right place for this project
            val googleServicesFile = File(project.projectDir, "google-services.json")
            if (!googleServicesFile.exists()) {
                // Try to copy from common location
                val sourceFile = File(rootProject.projectDir, "src/androidMain/google-services.json")
                if (sourceFile.exists()) {
                    sourceFile.copyTo(googleServicesFile, overwrite = true)
                    println("[Build] ✓ Copied google-services.json to Android subproject: ${googleServicesFile.absolutePath}")
                }
            }
        }
    }
}