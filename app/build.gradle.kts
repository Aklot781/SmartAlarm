plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.smartalarm"
    compileSdk = 34  // Можно обновить до 34, 35 или 36

    defaultConfig {
        applicationId = "com.example.smartalarm"
        minSdk = 24
        targetSdk = 34  // Обновите до 34
        versionCode = 1
        versionName = "1.0"

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
    // Ядро AndroidX (обновлено до 1.12.0)
    implementation("androidx.core:core-ktx:1.12.0")

    // AppCompat для обратной совместимости
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Material Design компоненты
    implementation("com.google.android.material:material:1.11.0")

    // Activity KTX для удобной работы с Activity
    implementation("androidx.activity:activity-ktx:1.8.")
}