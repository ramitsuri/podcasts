plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.ramitsuri.podcasts.android"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.ramitsuri.podcasts.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 52
        versionName = "5.2"
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../signing/android-debug.keystore")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            // Uncomment if testing
            // signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.widget)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.compose.icons.extended)
    implementation(libs.compose.lifecycle.viewmodel)
    implementation(libs.compose.lifecycle.runtime)
    implementation(libs.coil)
    implementation(libs.coroutines.guava)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.html.converter)
    implementation(libs.kotlin.datetime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.android.compose)
    implementation(libs.koin.workmanager)
    implementation(libs.kotlin.datetime)
    implementation(libs.media3.common)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.workmanager)
    implementation(libs.media3.session)
    implementation(libs.reorderable)
    implementation(libs.splash)
    implementation(libs.work)
    implementation(libs.composables)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    debugImplementation(libs.compose.ui.tooling)
}

detekt {
    source.setFrom("./")
    config.setFrom("../detekt-config.yml")
    buildUponDefaultConfig = true
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        exclude { element -> element.file.toString().contains("generated/") }
        exclude { element -> element.file.toString().contains("build/") }
    }
}
