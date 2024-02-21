import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.build.konfig)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.okhttp)
            implementation(libs.ktor.client.android)
        }

        commonMain.dependencies {
            implementation(libs.ktor.core)
            implementation(libs.ktor.content.negotation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.logging)
            implementation(libs.kotlin.serialization)
            implementation(libs.kotlin.datetime)
            implementation(libs.touchlab.log)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.ramitsuri.podcasts"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
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
    }
}

buildkonfig {
    packageName = "com.ramitsuri.podcasts.build"

    defaultConfigs {
        buildConfigField(
            type = STRING,
            name = "PODCAST_INDEX_KEY",
            value = getSecretProperty("PODCAST_INDEX_KEY"),
            nullable = false,
            const = true,
        )
        buildConfigField(
            type = STRING,
            name = "PODCAST_INDEX_SECRET",
            value = getSecretProperty("PODCAST_INDEX_SECRET"),
            nullable = false,
            const = true,
        )
    }
}

fun getSecretProperty(
    key: String,
    defaultValue: String? = null,
): String? {
    val file = "secret.properties"
    return getProperty(file, key, defaultValue)
}

fun getProperty(
    fileName: String,
    key: String,
    defaultValue: String? = null,
): String? {
    return if (file(rootProject.file(fileName)).exists()) {
        val properties = Properties()
        properties.load(FileInputStream(file(rootProject.file(fileName))))
        properties.getProperty(key, defaultValue)
    } else {
        defaultValue
    }
}
