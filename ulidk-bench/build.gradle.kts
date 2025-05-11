plugins {
    kotlin("jvm") version "2.1.20"
    id("me.champeau.jmh") version "0.7.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ulidk-core")) // MPPモジュールを参照
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
}
