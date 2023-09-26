

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "de.p72b.mocklation.parser"
    compileSdk = 33

    defaultConfig {
        minSdk = 24

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
val androidCoreKtxVersion: String by rootProject.extra
val mapsUtilsKtxVersion: String by rootProject.extra
val koinVersion: String by rootProject.extra

dependencies {
    implementation(project(":data"))

    implementation("androidx.core:core-ktx:$androidCoreKtxVersion")
    implementation("com.google.maps.android:maps-utils-ktx:$mapsUtilsKtxVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")
}