plugins {
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    id("maven-publish")
    signing
}

group = "io.github.reonaore"
version = "0.2.0"

val isRelease = !version.toString().endsWith("SNAPSHOT")

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser()
        nodejs()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.property)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.runner.junit5.jvm)
            }
        }
    }
}

// Dokka tasks
val dokkaHtmlJar by tasks.registering(Jar::class) {
    dependsOn("dokkaHtml")
    from(layout.buildDirectory.dir("dokka/html"))
    archiveClassifier.set("html-docs")
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    dependsOn("dokkaJavadoc")
    from(layout.buildDirectory.dir("dokka/javadoc"))
    archiveClassifier.set("javadoc")
}

// Publishing
publishing {
    publications {
        create<MavenPublication>("ulidk") {
            from(components["kotlin"])
            artifact(dokkaHtmlJar)
            artifact(dokkaJavadocJar)

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set("${project.group}:${project.name}")
                description.set("ULID implementation in Kotlin")
                url.set("https://github.com/reonaore/ulidk")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                developers {
                    developer {
                        id.set("onare")
                        name.set("Leona Shiode")
                        email.set("reona.ookikunaru@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/reonaore/ulidk.git")
                    developerConnection.set("scm:git:ssh://github.com/reonaore/ulidk.git")
                    url.set("https://github.com/reonaore/ulidk")
                }
            }
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            name = "OSSRH"
            url = if (!isRelease) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["ulidk"])
}
