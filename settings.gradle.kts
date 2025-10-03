rootProject.name = "LeviathanApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("plugins")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("leviathan") {
            from(files("gradle/leviathan.toml"))
        }
    }
}

include(":leviathan")
include(":leviathan-compose")