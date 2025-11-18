package com.sermilion.readability4k

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinMultiplatform(
  extension: KotlinMultiplatformExtension
) {
  extensions.configure<LibraryExtension> {
    compileSdk = findVersion("compileSdk").toInt()
    defaultConfig {
      minSdk = findVersion("minSdk").toInt()
    }
    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
    }
  }

  extension.apply {
    compilerOptions {
      freeCompilerArgs.addAll(
        listOf(
          "-opt-in=kotlin.RequiresOptIn",
          "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
          "-opt-in=kotlinx.coroutines.FlowPreview",
          "-opt-in=kotlin.ExperimentalMultiplatform",
          "-Xexpect-actual-classes",
        )
      )
    }

    androidTarget {
      compilations.all {
        compileTaskProvider.configure {
          compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
          }
        }
      }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets.apply {
      commonMain {
        dependencies {
        }
      }

      commonTest {
        dependencies {
        }
      }
    }
  }

  configureKotlin()
}

internal fun Project.configureKotlinMultiplatformCompose(
  extension: KotlinMultiplatformExtension
) {
  extensions.configure<LibraryExtension> {
    compileSdk = findVersion("compileSdk").toInt()
    defaultConfig {
      minSdk = findVersion("minSdk").toInt()
    }
    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
    }
  }

  extension.apply {
    compilerOptions {
      freeCompilerArgs.addAll(
        listOf(
          "-opt-in=kotlin.RequiresOptIn",
          "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
          "-opt-in=kotlinx.coroutines.FlowPreview",
          "-opt-in=kotlin.ExperimentalMultiplatform",
          "-Xexpect-actual-classes",
        )
      )
    }

    androidTarget {
      compilations.all {
        compileTaskProvider.configure {
          compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
          }
        }
      }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets.apply {
      commonMain {
        dependencies {
        }
      }

      androidMain {
        dependencies {
        }
      }

      iosMain {
        dependencies {
        }
      }
    }
  }

  configureKotlin()
}

private fun Project.configureKotlin() {
  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
      val warningsAsErrors: String? by project
      allWarningsAsErrors.set(warningsAsErrors.toBoolean())
      freeCompilerArgs.addAll(
        listOf(
          "-opt-in=kotlin.RequiresOptIn",
          "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
          "-opt-in=kotlinx.coroutines.FlowPreview",
          "-opt-in=kotlin.ExperimentalMultiplatform",
          "-Xexpect-actual-classes",
        )
      )
    }
  }
}

private fun Project.findVersion(alias: String): String {
  return libs.findVersion(alias).get().requiredVersion
}
