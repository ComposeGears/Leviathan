plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

val libName = "com.compose.gears.di.leviathan"
val libVersion = "0.0.1"

group = libName
version = libVersion

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = libName
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Leviathan")
            description.set("KMM DI library")
            url.set("https://github.com/ComposeGears/DI")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("vkatz")
                    name.set("Viachaslau Katsuba")
                    url.set("https://github.com/vkatz")
                }
                developer {
                    id.set("egorikftp")
                    name.set("Yahor Urbanovich")
                    url.set("https://github.com/egorikftp")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("m2"))
        }
    }
}