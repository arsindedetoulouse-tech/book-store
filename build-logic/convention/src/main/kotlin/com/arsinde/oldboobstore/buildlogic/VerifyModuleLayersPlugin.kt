package com.arsinde.oldboobstore.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * Enforces the module layering rules that `AGENTS.md` states but nothing checks.
 *
 * Gradle already rejects circular project dependencies at configuration time, so
 * a cycle detector would add nothing. What is missing is the layer rule, which is
 * not a cycle and therefore fails silently.
 *
 * Rules, per the coarse granularity agreed in design.md:
 *  - `:feature:X` may depend on `:core:*` and `:app`, never on another `:feature:`
 *  - `:core:X` may depend on `:core:model` only, or on nothing
 *
 * Violations are computed during evaluation and handed to the task as a plain
 * value. The task declares no inputs, so it always re-runs; that is deliberate,
 * because declaring the dependency graph as an input would couple this check to
 * configuration cache and isolated projects.
 */
class VerifyModuleLayersPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        require(rootProject == this) {
            "verifyModuleLayers must be applied to the root project"
        }

        // The root project has no lifecycle tasks of its own; `base` provides `check`.
        pluginManager.apply("base")

        val violationsReport = provider { report() }

        tasks.register(VERIFY_TASK_NAME) {
            group = "verification"
            description = "Fails when module dependencies violate the layering rules."
            // No inputs or outputs: this must re-evaluate on every run.
            doLast {
                val violations = violationsReport.get()
                if (violations.isNotEmpty()) {
                    throw GradleException(
                        buildString {
                            appendLine("Module layer violations:")
                            violations.forEach { appendLine("  - $it") }
                            appendLine()
                            appendLine("Rules:")
                            appendLine("  - a :feature: module must not depend on another :feature: module")
                            appendLine("  - a :core: module may depend only on :core:model")
                        },
                    )
                }
            }
        }

        tasks.named(CHECK_TASK_NAME) {
            dependsOn(VERIFY_TASK_NAME)
        }

        Unit
    }

    private fun Project.report(): List<String> = subprojects.flatMap { project ->
        val from = project.path

        project.configurations
            .flatMap { configuration ->
                configuration.dependencies
                    .filterIsInstance<org.gradle.api.artifacts.ProjectDependency>()
                    .map { it.path }
            }
            // Some configurations (lint, test fixtures) reference the module itself.
            .filter { to -> to != from }
            .distinct()
            .mapNotNull { to -> layerViolation(from, to) }
    }
}

private const val CHECK_TASK_NAME = "check"

internal const val VERIFY_TASK_NAME = "verifyModuleLayers"

/** The single definition of the layering rule, used by both the check and its test. */
internal fun layerViolation(from: String, to: String): String? {
    val fromGroup = from.split(":").filter { it.isNotEmpty() }.firstOrNull()
    return when {
        fromGroup == "feature" && to.split(":").contains("feature") ->
            "$from -> $to : a feature module must not depend on another feature module"

        fromGroup == "core" && to != ":core:model" ->
            "$from -> $to : a core module may depend only on :core:model"

        else -> null
    }
}