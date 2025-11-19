plugins {
  kotlin("jvm")
  application
}

group = "com.sermilion"
version = "1.0.0"

application {
  mainClass.set("com.sermilion.readability4k.cli.MainKt")
}

dependencies {
  implementation(project(":library"))
  implementation("io.ktor:ktor-client-core:3.0.3")
  implementation("io.ktor:ktor-client-cio:3.0.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}

tasks.named<JavaExec>("run") {
  standardInput = System.`in`
}
