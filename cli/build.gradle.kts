plugins {
  kotlin("jvm")
  application
}

group = "com.sermilion"
version = "0.1.0"

application {
  mainClass.set("com.sermilion.readability4k.cli.MainKt")
}

dependencies {
  implementation(project(":library"))
  implementation(libs.ktor.clientCore)
  implementation(libs.ktor.clientCio)
  implementation(libs.kotlinx.coroutines.core)
}

tasks.named<JavaExec>("run") {
  standardInput = System.`in`
}
