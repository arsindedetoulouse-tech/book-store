package com.arsinde.oldboobstore.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Pure Kotlin (non-Android) module, used by `:core:model` so the domain model
 * carries no Android dependency.
 */
class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
                freeCompilerArgs.add("-Xjvm-default=all")
            }
        }
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JvmLevels.JAVA
            targetCompatibility = JvmLevels.JAVA
        }

        dependencies {
            add("testImplementation", "org.jetbrains.kotlin:kotlin-test")
            add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }

        configureJvmToolchain()
        configureTestDefaults()
    }
}