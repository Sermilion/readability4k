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

    jvm {
        mavenPublication {
            artifactId = "readability4k-jvm"
        }
    }

    iosArm64 {
        mavenPublication {
            artifactId = "readability4k-iosarm64"
        }
    }

    iosSimulatorArm64 {
        mavenPublication {
            artifactId = "readability4k-iossimulatorarm64"
        }
    }

    iosX64 {
        mavenPublication {
            artifactId = "readability4k-iosx64"
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
            implementation(kotlin("reflect"))
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
version = "0.2.0"

val emptyJar by tasks.registering(Jar::class) {
    archiveAppendix.set("empty")
}

publishing {
    repositories {
        mavenLocal()
    }
    publications.withType<MavenPublication> {
        if (name == "kotlinMultiplatform") {
            artifactId = "readability4k"
        }

        val publicationName = name
        if (publicationName in listOf("iosArm64", "iosSimulatorArm64", "iosX64")) {
            artifact(emptyJar)
        }
    }
}
