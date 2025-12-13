pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // или PREFER_SETTINGS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmartAlarm"
include(":app")