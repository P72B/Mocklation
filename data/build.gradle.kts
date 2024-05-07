plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrains.kotlinx.json)
}

val androidMinSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra
val javaTarget: String by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "de.p72b.mocklation.data"
    compileSdk = androidCompileSdk

    defaultConfig {
        minSdk = androidMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaTarget
    }
}

dependencies {
    implementation(libs.kotlinx.json)
}