plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ulidk-core")) // MPP module reference
    implementation(libs.jmh.core)
    annotationProcessor(libs.jmh.generator)
}

jmh {
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
}
