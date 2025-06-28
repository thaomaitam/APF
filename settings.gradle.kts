enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Cho thư viện của Rikka và các thư viện khác trên Jitpack
        maven("https://jitpack.io")
        // Cho API của Xposed
        maven("https://api.xposed.info/")
    }
}

rootProject.name = "APF"
include(":app", ":xposed")