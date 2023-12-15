// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.android.library") version "8.1.1" apply false
}

val androidMinSdk by extra { 26 }
val appVersionName by extra { "2.0.0" }
val androidVersionCode by extra { 503 }
val androidCompileSdk by extra { 34 }
val javaTarget by extra { "17" }
val javaVersion by extra { JavaVersion.VERSION_17 }
