import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.diffplug.spotless")

      configure<SpotlessExtension> {
        kotlin {
          target("**/*.kt")
          targetExclude("**/build/**/*.kt")
          ktlint("1.0.1")
            .editorConfigOverride(
              mapOf(
                "indent_size" to "2",
                "continuation_indent_size" to "2",
                "max_line_length" to "120",
                "insert_final_newline" to "true",
                "ktlint_standard_no-wildcard-imports" to "disabled",
              )
            )
          trimTrailingWhitespace()
          endWithNewline()
        }

        kotlinGradle {
          target("**/*.gradle.kts")
          targetExclude("**/build/**/*.kts")
          ktlint("1.0.1")
        }

        format("misc") {
          target("**/*.md", "**/.gitignore")
          trimTrailingWhitespace()
          endWithNewline()
        }
      }
    }
  }
}
