import org.jreleaser.model.Active

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    id("maven-publish")
    alias(libs.plugins.jreleaser)
    signing
}

group = "io.github.reonaore"
version = "0.2.0-SNAPSHOT"

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
    dependsOn("dokkaGenerate")
    from(layout.buildDirectory.dir("dokka/html"))
    archiveClassifier = "html-docs"
}

jreleaser {
    signing {
        active = Active.ALWAYS
        armored = true
    }
    deploy {
        active = Active.RELEASE
        maven {
            mavenCentral {
                register("release-deploy") {
                    active = Active.RELEASE
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
            nexus2 {
                register("snapshot-deploy") {
                    active = Active.SNAPSHOT
                    snapshotUrl = "https://central.sonatype.com/repository/maven-snapshots/"
                    applyMavenCentralRules = true
                    snapshotSupported = true
                    closeRepository = true
                    releaseRepository = true
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}

// Publishing
publishing {
    publications {
        create<MavenPublication>("ulidk") {
            from(components["kotlin"])
            artifact(dokkaHtmlJar)

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
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["ulidk"])
}
