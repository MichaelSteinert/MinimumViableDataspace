rootProject.name = "mvd"

include(":launchers:connector")
include(":launchers:registrationservice")
include(":system-tests")
include(":extensions:refresh-catalog")
include(":extensions:policies")
include(":extensions:trusted-participants-whitelist")

pluginManagement {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {

    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        mavenLocal()
    }
}