import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Your ClipParse API key lives in the gitignored secrets.properties.
val secretsFile = rootProject.file("secrets.properties")
if (!secretsFile.exists()) {
    throw GradleException(
        "secrets.properties is missing. Copy secrets.example.properties to secrets.properties " +
            "and paste your ClipParse API key (free at https://clipparse.com/get-free-key).",
    )
}
val secrets = Properties().apply { secretsFile.inputStream().use { load(it) } }

android {
    namespace = "com.example.recipeimport"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.recipeimport"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "CLIPPARSE_API_KEY", "\"${secrets.getProperty("CLIPPARSE_API_KEY").orEmpty()}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Signed with the debug key so the release build installs directly.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.clipparse.sdk)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Loads the recipe's thumbnail.
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
