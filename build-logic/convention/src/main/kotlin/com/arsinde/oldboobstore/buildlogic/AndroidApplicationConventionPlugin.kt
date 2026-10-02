package com.arsinde.oldboobstore.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Android application module with the project's shared configuration.
 * Modules declare only `applicationId`, version codes and build-type specifics.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            namespace = defaultNamespace()
            compileSdk = AndroidSdk.COMPILE
            defaultConfig.targetSdk = AndroidSdk.TARGET
            defaultConfig.minSdk = AndroidSdk.MIN
            compileOptions.sourceCompatibility = JvmLevels.JAVA
            compileOptions.targetCompatibility = JvmLevels.JAVA
            buildFeatures.compose = true
        }

        configureJvmToolchain()
        configureTestDefaults()
    }
}