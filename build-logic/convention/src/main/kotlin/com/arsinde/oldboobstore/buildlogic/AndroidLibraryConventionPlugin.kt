package com.arsinde.oldboobstore.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Android library module with the project's shared configuration.
 * Modules declare only their namespace (derived automatically when absent) and
 * their own dependencies.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            applyLibraryDefaults()
            // The plugins block runs before the module body, so a module that needs a
            // different namespace can still override this.
            namespace = defaultNamespace()
            buildFeatures.compose = true
        }

        configureJvmToolchain()
        configureTestDefaults()
    }
}