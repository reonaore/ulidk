plugins {
    java
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.allopen") version "1.9.0"
    id("org.jetbrains.dokka") version "1.8.20"
    id("me.champeau.jmh") version "0.7.1"
}

repositories {
    mavenCentral()
}

val kotestVersion = "5.6.2"

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.5")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

