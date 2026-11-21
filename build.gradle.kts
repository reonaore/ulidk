plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
}

tasks.register("test") {
    group = "verification"
    description = "Runs tests for all supported targets."
    dependsOn(":ulidk-core:allTests")
}
