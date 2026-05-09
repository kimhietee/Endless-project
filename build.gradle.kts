import korlibs.korge.gradle.*
import korlibs.korge.gradle.Orientation
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

    // korge-ui is built-in with the KorGE plugin, no need for a separate dependency
    //add("commonMainApi", project(":korge-dragonbones"))
}

// Apply Google Services plugin to the Android project once it is created by KorGE
subprojects {
    afterEvaluate {
        if (name == "android" || project.plugins.hasPlugin("com.android.application")) {
            apply(plugin = "com.google.gms.google-services")
        }
    }
}

