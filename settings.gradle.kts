enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
rootProject.name = "YangDevExt"

include(":app")

include(":coroutines")
include(":moshi")
include(":normal")
include(":fragment")
include(":vm:core")
include(":vm:ext")
include(":entity:paging")
