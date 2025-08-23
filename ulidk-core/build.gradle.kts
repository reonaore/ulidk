plugins {
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.publish)
    signing
}

group = "io.github.reonaore"
version = "0.2.0"

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
                implementation(libs.kotlincrypto.random.crypto.rand)
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

mavenPublishing {
    coordinates(project.group.toString(), project.name, project.version.toString())
    signAllPublications()
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

signing {
    useGpgCmd()
}
