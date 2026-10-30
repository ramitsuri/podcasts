import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.build.konfig)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(11)
    jvm()

    android {
        namespace = "com.ramitsuri.podcasts"
        compileSdk = 37
        minSdk = 26

        withJava()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project.dependencies.enforcedPlatform(libs.firebase.bom.get()))
            implementation(libs.androidx.lifecycle)
            implementation(libs.coil)
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
            implementation(libs.koin.core)
            implementation(libs.ktor.core)
            implementation(libs.ktor.content.negotation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.logging)
            implementation(libs.kotlin.serialization)
            implementation(libs.kotlin.datetime)
            implementation(libs.sqldelight.common)
            implementation(libs.touchlab.log)
            implementation(libs.androidx.navigation3.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.mock)
            implementation(libs.coroutines.test)
        }
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
