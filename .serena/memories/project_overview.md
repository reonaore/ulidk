# ulidk Project Overview

## Purpose
ulidk is a Kotlin Multiplatform implementation of the ULID (Universally Unique Lexicographically Sortable Identifier) specification. It generates 128-bit identifiers with 48-bit timestamps and 80-bit entropy, encoded in Base32.

## Architecture
- **Core Components**: `ULID` class composed of `Timestamp` (48-bit unix milliseconds) and `Entropy` (80-bit randomness).
- **Encoding**: Custom Base32 with Crockford alphabet for string representation.
- **Platforms**: JVM, JavaScript (browser/node), iOS, macOS, Linux, Android, WASM JS/WASI.
- **Key Classes**:
  - `ULID`: Main class with companion factory methods.
  - `ULIDMonotonicGenerator`: For monotonic ULID generation.
  - Internal: `Timestamp`, `Entropy`, `Base32Encoder/Decoder`.

## Build & Development
- **Build System**: Gradle with Kotlin DSL, version catalog (`gradle/libs.versions.toml`).
- **Test**: `./gradlew test` (runs across all platforms).
- **Benchmark**: `./gradlew benchmark` (JMH on JVM).
- **Publish**: `./gradlew publish` to Maven Central.
- **Dependencies**: kotlinx-io, kotlinx-datetime, kotlinx-serialization, kotlincrypto.

## Codebase Structure
- `ulidk-core/`: Main library code.
  - `src/commonMain/`: Shared code.
  - `src/commonTest/`: Shared tests.
- `ulidk-bench/`: Benchmark code.
- Root: Build scripts, configs.

## Key Files
- `ulidk-core/src/commonMain/kotlin/io/github/reonaore/ulidk/ULID.kt`: Main ULID class.
- `ulidk-core/src/commonMain/kotlin/io/github/reonaore/ulidk/Base32Encoder.kt`: Base32 encoding logic.
- `ulidk-core/build.gradle.kts`: Multiplatform configuration.
- `gradle/libs.versions.toml`: Dependency versions.