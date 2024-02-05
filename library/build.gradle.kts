plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    signing
}

val libName = "io.github.composegears.di"
val libVersion = "1.0.0"

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
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Leviathan")
            description.set("KMM DI library")
            url.set("https://github.com/ComposeGears/DI")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
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
            scm {
                url.set("https://github.com/ComposeGears/DI")
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("m2"))
        }
    }
}

signing {
    val pgpKey = project.properties["PGP_KEY"]?.toString()?.replace("|","\n")
    val pgpPas = project.properties["PGP_PAS"]?.toString()
    if (!pgpPas.isNullOrBlank() && !pgpKey.isNullOrBlank()) {
        useInMemoryPgpKeys(pgpKey, pgpPas)
        sign(publishing.publications)
    }
}