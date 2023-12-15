plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

val androidMinSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra
val androidVersionCode: Int by rootProject.extra
val appVersionName: String by rootProject.extra
val javaTarget: String by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

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
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        kotlinCompilerExtensionVersion = "1.5.3"
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
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.utils)
    ksp(libs.androidx.room.compiler)
}