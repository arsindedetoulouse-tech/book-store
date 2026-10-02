// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Validates module layering rules after all projects are evaluated. Fails the
    // build when a :feature: module depends on another :feature:, or a :core: module
    // depends on anything other than :core:model.
    id("oldboobstore.verify.module.layers")

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // Applied to each module by the oldboobstore.detekt convention plugin; declared
    // here so its classes are on the build classpath for that plugin to resolve.
    alias(libs.plugins.detekt) apply false
}