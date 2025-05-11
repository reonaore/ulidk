plugins {
    kotlin("multiplatform") version "2.1.20"
    kotlin("plugin.allopen") version "2.1.20"
    id("org.jetbrains.dokka") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    id("io.kotest.multiplatform") version "5.8.1"
    id("maven-publish")
    signing
}

group = "io.github.reonaore"
version = "0.2.0-SNAPSHOT"

val isRelease = !version.toString().endsWith("SNAPSHOT")

repositories {
    mavenCentral()
}

val kotestVersion = "5.8.1"

kotlin {
    jvm {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
    js(IR) { // LEGACY or BOTH are unsupported
        browser() // to compile for the web
        nodejs() // to compile against node
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            }
        }
        commonTest {
            dependencies {
                implementation("io.kotest:kotest-property:${kotestVersion}")
                implementation("io.kotest:kotest-assertions-core:${kotestVersion}")
                implementation("io.kotest:kotest-framework-engine:${kotestVersion}")
            }
        }

        jvmTest {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
            }
        }
    }
}


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

publishing {
    publications {
        create<MavenPublication>("ulidk") {
            from(components["kotlin"])
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)

            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
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
