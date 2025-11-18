package com.sermilion.readability4k

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

val Project.libs
  get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Workaround to get [TaskProvider] by task name if it already exists in given [Project]
 * or register it otherwise
 * [Github - Introduce TaskContainer.maybeNamed](https://github.com/gradle/gradle/issues/8057)
 */
inline fun <reified T : Task> Project.registerMaybe(
  taskName: String,
  configuration: Action<in T> = Action {},
): TaskProvider<T> {
  if (taskName in tasks.names) {
    return tasks.named(taskName, T::class, configuration)
  }
  return tasks.register(taskName, T::class, configuration)
}