plugins {
    // Android Gradle Plugin (AGP)
    id("com.android.application") version "8.13.0" apply false
    id("com.android.library") version "8.13.0" apply false // only if you add library modules later

    // Kotlin
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false

    // Firebase Google Services plugin
    id("com.google.gms.google-services") version "4.4.3" apply false
}