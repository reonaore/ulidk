rootProject.name = "ulidk"

include(
    ":ulidk-bench",
    ":ulidk-core",
)

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
