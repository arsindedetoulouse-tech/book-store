pluginManagement {
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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Old Boob Store"
include(":app")
include(":core:designsystem")
include(":core:model")
include(":core:navigation")
include(":core:network")
include(":feature:booklist")

// Convention plugins: shared build configuration declared once, applied per module.
includeBuild("build-logic")
