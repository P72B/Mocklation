import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetbrains.kotlinx.json)
}

val androidMinSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra
val androidVersionCode: Int by rootProject.extra
val appVersionName: String by rootProject.extra
val javaTarget: String by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

val key: String = gradleLocalProperties(rootDir).getProperty("GOOGLE_MAPS_API_KEY")

android {
    namespace = "de.p72b.mocklation"
    compileSdk = androidCompileSdk

    defaultConfig {
        applicationId = "de.p72b.mocklation"
        minSdk = androidMinSdk
        targetSdk = androidCompileSdk
        versionCode = androidVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            resValue("string", "google_maps_key", key)
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "google_maps_key", key)
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    kotlinOptions {
        jvmTarget = javaTarget
    }
}

dependencies {
    implementation(project(":parser"))
    implementation(project(":data"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
    //implementation(libs.androidx.compose.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.google.maps.compose)
    implementation(libs.kotlinx.json)

    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.utils)
    ksp(libs.androidx.room.compiler)
}