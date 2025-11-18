import com.sermilion.readability4k.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.library")
        apply("org.jetbrains.kotlin.multiplatform")
        apply("readability4k.detekt")
      }

      extensions.configure<KotlinMultiplatformExtension> {
        configureKotlinMultiplatform(this)
      }
    }
  }
}
