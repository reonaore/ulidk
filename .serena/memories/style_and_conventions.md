# Code Style and Conventions for ulidk

## General
- Follow official Kotlin coding conventions.
- Use `kotlin.code.style=official` in gradle.properties.

## Project Specific
- Package: `io.github.reonaore.ulidk`
- Internal classes for implementation details (e.g., `Timestamp`, `Entropy`, `Base32Encoder`).
- Platform-specific implementations via `expect`/`actual`.
- Use `@OptIn` for experimental APIs (e.g., `ExperimentalTime`, `ExperimentalUuidApi`).
- Lazy properties for caching binary and string representations.
- Custom Base32 with Crockford alphabet (no I,L,O,U) for string encoding.

## Naming
- Classes: PascalCase (e.g., `ULID`, `ULIDMonotonicGenerator`).
- Functions: camelCase (e.g., `randomULID`, `fromString`).
- Variables: camelCase.

## Documentation
- Use kDoc for public APIs.
- See [kDoc](https://reonaore.github.io/ulidk/) for examples.

## Imports
- Import specific classes/functions.
- Avoid wildcard imports.

## Error Handling
- Use `Result` for operations that can fail (e.g., `fromString` returns `Result<ULID>`).