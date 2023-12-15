

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val androidMinSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra
val javaTarget: String by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "de.p72b.mocklation.parser"
    compileSdk = androidCompileSdk

    defaultConfig {
        minSdk = androidMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.google.maps.utils)
    implementation(libs.koin.compose)
}