import com.sermilion.readability4k.configureKotlinMultiplatformCompose
import com.sermilion.readability4k.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.library")
        apply("org.jetbrains.kotlin.multiplatform")
        apply("org.jetbrains.compose")
        apply("org.jetbrains.kotlin.plugin.compose")
        apply("readability4k.android.lint")
        apply("readability4k.detekt")
      }

      extensions.configure<KotlinMultiplatformExtension> {
        configureKotlinMultiplatformCompose(this)
      }

      extensions.configure<ComposeExtension> {
      }

      dependencies {
        add("commonMainImplementation", libs.findLibrary("kotlinx.collections.immutable").get())
        add("commonMainImplementation", libs.findLibrary("jetbrains.lifecycle.runtime.compose").get())
      }
    }
  }
}
