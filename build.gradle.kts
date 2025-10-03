import io.gitlab.arturbosch.detekt.DetektPlugin

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.m2p) apply false
}

detekt {
    config.from(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

allprojects {
    group = "io.github.composegears"

    apply<DetektPlugin>()
    detekt {
        buildUponDefaultConfig = true
        parallel = true
        autoCorrect = true
        source.from(
            files(
                "src/commonMain/kotlin",
                "src/jvmMain/kotlin",
                "src/desktopMain/kotlin",
                "src/androidMain/kotlin",
                "src/iosMain/kotlin",
                "src/wasmJsMain/kotlin",
            )
        )
    }

    dependencies {
        detektPlugins(rootProject.project.libs.detekt.compose)
        detektPlugins(rootProject.project.libs.detekt.formatting)
    }
}

// check ABI
tasks.register("checkAbi") {
    dependsOn(":leviathan:checkLegacyAbi")
    dependsOn(":leviathan-compose:checkLegacyAbi")
}

// update ABI
tasks.register("updateAbi") {
    dependsOn(":leviathan:updateLegacyAbi")
    dependsOn(":leviathan-compose:updateLegacyAbi")
}

createM2PTask()