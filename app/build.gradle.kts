plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.doma.assist"
    compileSdk = 35

    kotlinOptions {\n        jvmTarget = "1.8"\n    }\n\n    defaultConfig {
        applicationId = "com.doma.assist"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

