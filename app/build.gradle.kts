plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.replex.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.replex.tv"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.leanback:leanback:1.0.0")

    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}