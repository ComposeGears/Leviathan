import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.m2p)
}

version = leviathan.versions.leviathan.get()

kotlin {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }

    jvm()
    androidLibrary {
        namespace = "com.composegears.leviathan.compose"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget = JvmTarget.JVM_1_8
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.leviathan)

            implementation(compose.foundation)
            implementation(compose.runtime)
            implementation(libs.lifecycle.viewmodel.compose)
        }
    }
}

m2p {
    description = "Leviathan Compose integration"
}