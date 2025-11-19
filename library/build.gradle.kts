plugins {
    alias(libs.plugins.readability4k.kmp.library)
    `maven-publish`
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        publishLibraryVariantsGroupedByFlavor = true

        mavenPublication {
            artifactId = "readability4k-android"
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        languageSettings.optIn("kotlinx.coroutines.FlowPreview")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ksoup)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.framework.datatest)
            implementation(kotlin("test"))
        }

        androidUnitTest.dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.runner.junit5.jvm)
        }

        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5.jvm)
            implementation(libs.mockk)
        }
    }
}

android {
    namespace = "com.sermilion.readability4k"

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

group = "com.sermilion"
version = "0.1.0"

publishing {
    repositories {
        mavenLocal()
    }
    publications.withType<MavenPublication> {
        artifactId =
            when (name) {
                "kotlinMultiplatform" -> "readability4k"
                "androidRelease" -> "readability4k-android"
                "jvm" -> "readability4k-jvm"
                "iosArm64" -> "readability4k-iosarm64"
                "iosSimulatorArm64" -> "readability4k-iossimulatorarm64"
                "iosX64" -> "readability4k-iosx64"
                else -> artifactId
            }
    }
}
