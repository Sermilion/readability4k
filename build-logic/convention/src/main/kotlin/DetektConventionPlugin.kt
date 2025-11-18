import com.sermilion.readability4k.configureDetekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class DetektConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("io.gitlab.arturbosch.detekt")
      }
      configureDetekt()
    }
  }
}
