plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.allopen") version "1.9.0"
    id("org.jetbrains.dokka") version "1.8.20"
    id("me.champeau.jmh") version "0.7.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    id("maven-publish")
    signing
}

group = "io.github.reonaore"
version = "0.0.1-SNAPSHOT"

val isRelease = !version.toString().endsWith("SNAPSHOT")

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

val dokkaHtmlJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

val dokkaJavadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("ulidk") {
            from(components["kotlin"])
            artifact(sourcesJar)
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
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
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
