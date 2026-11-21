plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
}

tasks.register("test") {
    group = "verification"
    description = "Runs all tests across all modules."
    dependsOn(subprojects.map { "${it.path}:allTests" })
}
