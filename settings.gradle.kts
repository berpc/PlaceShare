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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PlaceShare"
include(":app")

include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:ui")
include(":feature")
include(":feature:home")
include(":feature:places")
include(":feature:explore")
include(":feature:visits")
include(":feature:profile")
