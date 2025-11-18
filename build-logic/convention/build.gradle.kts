import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `kotlin-dsl`
}

group = "com.sermilion.readability4k.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
  }
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.detekt.gradlePlugin)
  compileOnly(libs.spotless.gradlePlugin)
  implementation("org.jetbrains.compose:compose-gradle-plugin:1.9.3")
}

tasks {
  validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
  }
}

gradlePlugin {
  plugins {
    register("detekt") {
      id = "readability4k.detekt"
      implementationClass = "DetektConventionPlugin"
    }
    register("spotless") {
      id = "readability4k.spotless"
      implementationClass = "SpotlessConventionPlugin"
    }
    register("kmpLibrary") {
      id = "readability4k.kmp.library"
      implementationClass = "KmpLibraryConventionPlugin"
    }
    register("kmpCompose") {
      id = "readability4k.kmp.compose"
      implementationClass = "KmpComposeConventionPlugin"
    }
  }
}
