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
        maven {
            url = uri("https://bluefrogrobotics.jfrog.io/artifactory/bluefrogrobotics-libs-release-local/")
            credentials {
                username = providers.gradleProperty("buddySDKUser").get()
                password = providers.gradleProperty("buddySDKPasswd").get()
            }
        }
    }
}

rootProject.name = "BuddyTemplate"
include(":app")
