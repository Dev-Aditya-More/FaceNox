rootProject.name = "FaceNox"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":sharedUI")
include(":androidApp")
include(":desktopApp")
