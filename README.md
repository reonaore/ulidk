# What?

[ULID](https://github.com/ulid/spec) implementation in Kotin

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