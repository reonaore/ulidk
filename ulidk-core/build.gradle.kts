import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.publish)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    signing
}

group = "io.github.reonaore"
version = "0.2.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
    js {
        browser()
        nodejs()
        binaries.library()
    }
    iosArm64()
    macosArm64()
    linuxX64()

    androidLibrary {
        namespace = "io.github.reonaore.ulidk"
        compileSdk = 33
        minSdk = 24
        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(
                        org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
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
                implementation(kotlin("test"))
            }
        }
    }
}

mavenPublishing {
    coordinates(project.group.toString(), project.name, project.version.toString())
    publishToMavenCentral()
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
