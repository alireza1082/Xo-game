import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        maven(url = "https://maven.myket.ir")
        maven(url = "https://maven.devneeds.ir")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        maven(url = "https://maven.myket.ir")
        maven(url = "https://maven.devneeds.ir")
        google()
        mavenCentral()
    }
}

rootProject.name = "Xo"
include(":app")
