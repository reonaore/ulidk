# What?

[ULID](https://github.com/ulid/spec) implementation in Kotin.

See [kDoc](https://reonaore.github.io/ulidk/) to see the specification.

# Install

TODO

# Usage

To generate a ULID, call `randomULID()`.

```kotlin
import io.onare.ulidk.ULID

val ulid: ULID = ULID.randomULID()
val ulidString = ulid.toString() // e.g. 01ARZ3NDEKTSV4RRFFQ69G5FAV
val ulidBinary = ulid.binary // 16 bytes binary of the ULID
```

## Decode from string

```kotlin
import io.onare.ulidk.ULID

val ulid: ULID = ULID.fromString("01ARZ3NDEKTSV4RRFFQ69G5FAV").getOrThrow()
val timestamp: Long = ulid.timestamp() // 48bit unix time of the ULID
val entropy: ByteArray = ulid.entropy() // randomness of the ULID
```

## Monotonic ULIDs

```kotlin

import io.onare.ulidk.ULID

val ulidGen = ULID.MonotonicGenerator(ULID.randomULID(150000))

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
import io.onare.ulidk.ULID
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
./gradlew jmh 
```

### result

on MacBook Air M1 2020, 8GB memory

```text
Benchmark                                Mode  Cnt         Score        Error  Units
TestBenchmark.decodeThroughput          thrpt   10   3303345.521 ± 149622.901  ops/s
TestBenchmark.generationThroughput      thrpt   10   3933690.923 ± 269409.409  ops/s
TestBenchmark.monoGenerationThroughput  thrpt   10  17405324.773 ± 716794.007  ops/s
TestBenchmark.uuidGenerationThroughput  thrpt   10   3327425.975 ±  77265.741  ops/s
TestBenchmark.decodeAverage              avgt   10       303.135 ±     13.585  ns/op
TestBenchmark.generationAverage          avgt   10       252.532 ±     16.077  ns/op
TestBenchmark.monoGenerationAverage      avgt   10        33.047 ±      2.946  ns/op
TestBenchmark.uuidGenerationAverage      avgt   10       309.880 ±     21.506  ns/op
```