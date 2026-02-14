# Repository Guidelines

## Project Structure & Module Organization
This is a Kotlin Multiplatform project with two Gradle modules:
- `ulidk-core/`: main library code and tests.
  - Sources: `ulidk-core/src/commonMain/kotlin/io.github.reonaore.ulidk/`
  - Tests: `ulidk-core/src/commonTest/kotlin/io/github/reonaore/ulidk/`
- `ulidk-bench/`: benchmark suite (kotlinx-benchmark/JMH-style targets).

Top-level build configuration lives in `build.gradle.kts`, `settings.gradle.kts`, and `gradle/libs.versions.toml`.

## Build, Test, and Development Commands
Use the Gradle wrapper from repo root:
- `./gradlew build`: full project build (same baseline as CI build workflow).
- `./gradlew test`: run test suites.
- `./gradlew koverXmlReport`: generate coverage XML used by CI/Codecov.
- `./gradlew dokkaGenerate`: build API docs (`ulidk-core/build/dokka/html`).
- `./gradlew benchmark`: run benchmarks from `ulidk-bench`.

## Coding Style & Naming Conventions
- Language: Kotlin (`.kt`) with Kotlin DSL (`.kts`) for build scripts.
- Follow Kotlin defaults: 4-space indentation, trailing commas where already used, expressive immutable `val` by default.
- Keep package names lowercase and aligned with module paths (for example `io.github.reonaore.ulidk`).
- Type/class names use `UpperCamelCase`; functions and properties use `lowerCamelCase`; constants use `UPPER_SNAKE_CASE`.
- Run static analysis when changing core logic: `./gradlew detekt`.

## Testing Guidelines
- Framework: `kotlin.test` in `commonTest`.
- Test files should be named `*Test.kt` (for example `ULIDTest.kt`).
- Prefer focused tests by behavior (`decodeInvalidStringLength`, `monotonicEdgeCase`) and include edge cases for timestamp/entropy boundaries.
- Validate coverage changes with `./gradlew koverXmlReport` when touching core algorithms.

## Commit & Pull Request Guidelines
- Commit style in history is mostly Conventional Commit-like: `chore: ...`, `chore(deps): ...`, `fix: ...`, `refactor: ...`.
- Keep subject lines short and imperative; use a scope when useful.
- PRs should include:
  - clear problem/solution summary,
  - linked issue (if applicable),
  - test evidence (commands run and results),
  - benchmark impact when performance-sensitive code changes.
- Ensure GitHub Actions workflows pass (`build`, coverage, and docs where relevant).
