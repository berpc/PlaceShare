import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
}

val localProps = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val cloudNameVal = localProps.getProperty("cloudinary.cloudName", localProps.getProperty("CLOUDINARY_CLOUD_NAME", ""))
val uploadPresetVal = localProps.getProperty("cloudinary.uploadPreset", localProps.getProperty("CLOUDINARY_UPLOAD_PRESET", ""))

android {
    namespace = "com.ramirezpenayo.placeshare.core.network"
    compileSdk = 37

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "cloudName", "\"$cloudNameVal\"")
        buildConfigField("String", "uploadPreset", "\"$uploadPresetVal\"")
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudNameVal\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$uploadPresetVal\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
}
