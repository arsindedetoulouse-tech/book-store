package com.arsinde.oldboobstore.buildlogic

import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Applies detekt with the project's shared configuration.
 *
 * detekt 2.0.0-alpha.6 is the only line compatible with AGP 9: earlier releases
 * require `android.builtInKotlin=false`, which reverts AGP 9's built-in Kotlin.
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(DetektPlugin::class.java)

        extensions.configure<DetektExtension> {
            toolVersion.set("2.0.0-alpha.6")
            config.setFrom(rootProject.rootDir.resolve("config/detekt/detekt.yml"))
            buildUponDefaultConfig.set(false)
        }
    }
}