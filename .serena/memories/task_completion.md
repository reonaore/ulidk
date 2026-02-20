# What to Do When a Task is Completed

After making changes to the code:
1. Run `./gradlew build` to ensure it compiles across all platforms.
2. Run `./gradlew allTests` to verify tests pass.
3. If performance-critical changes, run `./gradlew benchmark` to check performance.
4. Check code coverage with `./gradlew koverHtmlReport` if needed.
5. Run `./gradlew detekt` for static analysis (if configured).
6. Generate docs with `./gradlew dokkaHtml` if public API changed.
7. Commit changes with descriptive message.
8. Push and check CI (GitHub Actions).

Ensure changes work on all target platforms: JVM, JS (browser/node), iOS, macOS, Linux, Android, WASM.
