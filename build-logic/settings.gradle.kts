@file:Suppress("UnstableApiUsage")

// The convention plugins live in an included build so they are compiled against
// the AGP and Kotlin plugin APIs once, then reused by every module.
dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            // Share the project's single catalog instead of duplicating versions here.
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include(":convention")