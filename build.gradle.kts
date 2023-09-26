// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.android.library") version "8.1.1" apply false
}

val androidCoreKtxVersion by extra { "1.12.0" }
val mapsUtilsKtxVersion by extra { "3.4.0" }
val koinVersion by extra { "3.5.0" }
