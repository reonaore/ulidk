# Suggested Commands for ulidk Development

## Building and Testing
- `./gradlew build`: Build the entire project across all platforms.
- `./gradlew allTests`: Run tests across all platforms.
- `./gradlew benchmark`: Run JMH benchmarks on JVM.

## Publishing
- `./gradlew publish`: Publish to Maven Central.

## Code Quality
- `./gradlew detekt`: Run Detekt static analysis (if configured).
- `./gradlew koverHtmlReport`: Generate code coverage report.

## Documentation
- `./gradlew dokkaHtml`: Generate HTML documentation.

## Utility Commands (macOS/Darwin)
- `ls -la`: List files with details.
- `cd <dir>`: Change directory.
- `grep -r "pattern" .`: Search for pattern in files.
- `find . -name "*.kt"`: Find Kotlin files.
- `git status`: Check git status.
- `git diff`: Show changes.
- `git add . && git commit -m "message"`: Stage and commit changes.
