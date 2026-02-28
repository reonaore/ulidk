# AI Coding Assistant Instructions for ulidk

## Project Overview
ulidk is a Kotlin Multiplatform implementation of the ULID (Universally Unique Lexicographically Sortable Identifier) specification. It generates 128-bit identifiers with 48-bit timestamps and 80-bit entropy, encoded in Base32.

## Architecture
- **Core Components**: `ULID` class composed of `Timestamp` (48-bit unix milliseconds) and `Entropy` (80-bit randomness)
- **Encoding**: Custom Base32 with Crockford alphabet (no I,L,O,U) for string representation
- **Platforms**: JVM, JavaScript (browser/node), iOS, macOS, Linux, Android, WASM JS/WASI
- **Key Classes**:
  - `ULID`: Main class with companion factory methods
  - `ULIDMonotonicGenerator`: For monotonic ULID generation
  - Internal: `Timestamp`, `Entropy`, `Base32Encoder/Decoder`

## Build & Development
- **Build System**: Gradle with Kotlin DSL, version catalog (`gradle/libs.versions.toml`)
- **Test**: `./gradlew test` (runs across all platforms)
- **Benchmark**: `./gradlew benchmark` (JMH on JVM)
- **Publish**: `./gradlew publish` to Maven Central
- **Dependencies**: kotlinx-io, kotlinx-datetime, kotlinx-serialization, kotlincrypto

## Code Patterns
- **ULID Generation**: `ULID.randomULID(timestamp = Clock.System.now().toEpochMilliseconds())`
- **String Decoding**: `ULID.fromString("01H7PN3EH10123456789ABCDEF")`
- **Monotonic ULIDs**: `val gen = ULIDMonotonicGenerator(); gen()`
- **Serialization**: Built-in Kotlinx.serialization support
- **UUID Compatibility**: `ULID.fromUUID(uuid)` and `ulid.toUUID()`
- **Lazy Properties**: Binary and string representations cached with `by lazy`

## Conventions
- Internal classes for implementation details (`Timestamp`, `Entropy`, `Base32Encoder`)
- Platform-specific implementations via `expect`/`actual` (though SecureRandom uses common kotlincrypto)
- `@OptIn` for experimental APIs (ExperimentalTime, ExperimentalUuidApi)
- Package: `io.github.reonaore.ulidk`

## Key Files
- `ulidk-core/src/commonMain/kotlin/io/github/reonaore/ulidk/ULID.kt`: Main ULID class
- `ulidk-core/src/commonMain/kotlin/io/github/reonaore/ulidk/Base32Encoder.kt`: Base32 encoding logic
- `ulidk-core/build.gradle.kts`: Multiplatform configuration
- `gradle/libs.versions.toml`: Dependency versions

## Testing
- Common tests in `ulidk-core/src/commonTest/`
- Platform-specific test validation
- Benchmark tests in `ulidk-bench/`

When contributing, ensure changes work across all target platforms and maintain ULID spec compliance.
