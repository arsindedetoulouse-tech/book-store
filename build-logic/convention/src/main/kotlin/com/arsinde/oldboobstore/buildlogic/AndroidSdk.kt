package com.arsinde.oldboobstore.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.withType

/**
 * Compile and target Android platform level shared by every module.
 * Kept here so the single-source-of-truth requirement holds: a module never
 * states an SDK level of its own.
 */
object AndroidSdk {
    const val COMPILE: Int = 37
    const val TARGET: Int = 37
    const val MIN: Int = 24
}

/** Java language and bytecode level shared by every module. */
object JvmLevels {
    val JAVA: JavaVersion = JavaVersion.VERSION_21
    const val TOOLCHAIN: Int = 21
}

/** Applies the declared JDK toolchain to a JVM-capable project. */
internal fun Project.configureJvmToolchain() {
    extensions.findByType(JavaPluginExtension::class.java)?.toolchain?.apply {
        languageVersion.set(JavaLanguageVersion.of(JvmLevels.TOOLCHAIN))
    }
}

/** Test defaults: readable failures without anyone reading a report by hand. */
internal fun Project.configureTestDefaults() {
    tasks.withType<Test>().configureEach {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            showStackTraces = true
        }
    }
}

/** Base package shared by every module. */
private const val BASE_PACKAGE = "com.arsinde.oldboobstore"

/**
 * Derives a namespace from the module path so modules do not repeat it.
 *
 * `:core:designsystem` becomes `com.arsinde.oldboobstore.core.designsystem`.
 * A top-level module such as `:app` gets the base package itself, so the
 * application namespace stays `com.arsinde.oldboobstore` rather than gaining a
 * redundant `.app` segment.
 */
fun Project.defaultNamespace(): String {
    val segments = path.removePrefix(":").split(":").filter { it.isNotEmpty() }
    return if (segments.size <= 1) BASE_PACKAGE else "$BASE_PACKAGE.${segments.joinToString(".")}"
}

/** SDK levels, language level and toolchain shared by every Android library. */
internal fun LibraryExtension.applyLibraryDefaults() {
    compileSdk = AndroidSdk.COMPILE
    defaultConfig.minSdk = AndroidSdk.MIN
    compileOptions.sourceCompatibility = JvmLevels.JAVA
    compileOptions.targetCompatibility = JvmLevels.JAVA
    testOptions.unitTests.isIncludeAndroidResources = true
}