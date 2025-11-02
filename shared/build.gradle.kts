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
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvm()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.androidx.lifecycle)
            implementation(libs.firebase.config)
            implementation(libs.koin.android)
            implementation(libs.ktor.okhttp)
            implementation(libs.ktor.client.android)
            implementation(libs.sqldelight.android)
            implementation(libs.viewmodel)
        }

        jvmMain.dependencies {
            implementation(libs.koin.test)
            implementation(libs.ktor.test)
            implementation(libs.sqldelight.test)
        }

        commonMain.dependencies {
            implementation(libs.datastore.preferences)
            implementation(libs.coil)
            implementation(libs.koin.core)
            implementation(libs.ktor.core)
            implementation(libs.ktor.content.negotation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.logging)
            implementation(libs.kotlin.serialization)
            implementation(libs.kotlin.datetime)
            implementation(libs.sqldelight.common)
            implementation(libs.touchlab.log)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.mock)
            implementation(libs.coroutines.test)
        }
    }
}

android {
    namespace = "com.ramitsuri.podcasts"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    buildFeatures {
        buildConfig = true
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
        exclude { element -> element.file.toString().contains("generated/") }
        exclude { element -> element.file.toString().contains("build/") }
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

sqldelight {
    databases {
        create("PodcastsDatabase") {
            packageName.set("com.ramitsuri.podcasts")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
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
