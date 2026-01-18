![build](https://github.com/reonaore/ulidk/actions/workflows/build.yaml/badge.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![codecov](https://codecov.io/gh/reonaore/ulidk/graph/badge.svg?token=27FPKJM9IU)](https://codecov.io/gh/reonaore/ulidk)

# What?

[ULID](https://github.com/ulid/spec) implementation in Kotin.

See [kDoc](https://reonaore.github.io/ulidk/) to see the specification.

# Install

## Kotlin DSL

Add maven repository.

```kotlin
repositories {
    mavenCentral()
}
```

If you want to use snapshots, add another repository.

```kotlin
repositories {
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}
```

Add dependency.

```kotlin
 implementation("io.github.reonaore:ulidk:0.1.0")
```

# Usage

To generate a ULID, call `randomULID()`.

```kotlin
import io.github.reonaore.ulidk.ULID
import io.github.reonaore.ulidk.ULIDMonotonicGenerator

val ulid: ULID = ULID.randomULID()
val ulidString = ulid.toString() // e.g. 01ARZ3NDEKTSV4RRFFQ69G5FAV
val ulidBinary = ulid.binary // 16 bytes binary of the ULID
```

## Decode from string

```kotlin
import io.github.reonaore.ulidk.ULID
import io.github.reonaore.ulidk.ULIDMonotonicGenerator

val ulid: ULID = ULID.fromString("01ARZ3NDEKTSV4RRFFQ69G5FAV").getOrThrow()
val timestamp: Long = ulid.timestamp() // 48bit unix time of the ULID
val entropy: ByteArray = ulid.entropy() // randomness of the ULID
```

## Monotonic ULIDs

```kotlin

import io.github.reonaore.ulidk.ULID
import io.github.reonaore.ulidk.ULIDMonotonicGenerator

val ulidGen = ULIDMonotonicGenerator(ULID.randomULID(150000))

// Strict ordering for the same timestamp, by incrementing the least-significant random bit by 1
ulidGen() // 000XAL6S41ACTAV9WEVGEMMVR8
ulidGen() // 000XAL6S41ACTAV9WEVGEMMVR9
ulidGen() // 000XAL6S41ACTAV9WEVGEMMVRA
ulidGen() // 000XAL6S41ACTAV9WEVGEMMVRB
ulidGen() // 000XAL6S41ACTAV9WEVGEMMVRC

// Even if a lower timestamp is passed (or generated), it will preserve sort order
ulidGen(100000) // 000XAL6S41ACTAV9WEVGEMMVRD
```

## UUID compatibility

```kotlin
import io.github.reonaore.ulidk.ULID
import io.github.reonaore.ulidk.ULIDMonotonicGenerator
import java.util.*

val uuid = UUID.randomUUID()
val ulid = ULID.fromUUID(uuid)
assert(uuid.toString() == ulid.toUUID().toString())
```

## Test

```shell
./gradlew test
```

## Benchmark

```shell
./gradlew benchmark
```

### result

on MacBook Air M1 2020, 8GB memory

```text
Benchmark                                Mode  Cnt        Score         Error  Units
TestBenchmark.decodeThroughput          thrpt    5  2870482.675 ±   28929.819  ops/s
TestBenchmark.generationThroughput      thrpt    5  2222172.000 ±  337147.404  ops/s
TestBenchmark.monoGenerationThroughput  thrpt    5  7301193.431 ± 2717363.333  ops/s
TestBenchmark.uuidGenerationThroughput  thrpt    5  1997510.100 ± 2425175.330  ops/s
TestBenchmark.decodeAverage              avgt    5      526.944 ±     926.595  ns/op
TestBenchmark.generationAverage          avgt    5      550.906 ±     568.759  ns/op
TestBenchmark.monoGenerationAverage      avgt    5       33.213 ±       1.528  ns/op
TestBenchmark.uuidGenerationAverage      avgt    5      337.830 ±      91.290  ns/op
```
