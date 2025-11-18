plugins {
  alias(libs.plugins.readability4k.kmp.library)
}

kotlin {
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
