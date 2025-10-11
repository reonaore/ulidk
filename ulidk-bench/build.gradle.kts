plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.kotlin.allopen)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                api(project(":ulidk-core"))
                implementation(libs.kotlinx.benchmark.runtime)
            }
        }
    }
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    targets {
        register("jvm")
    }
}
