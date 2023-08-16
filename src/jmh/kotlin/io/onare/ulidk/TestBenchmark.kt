package io.onare.ulidk

import org.openjdk.jmh.annotations.*
import java.util.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(2)
@Warmup(iterations = 0)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
class TestBenchmark {
    var ulidString = ""
    var uuidString = ""
    final var ulid: ULID = ULID.randomULID()
    var monoULID = ULID.MonotonicGenerator(ulid)

    @Setup
    fun setup() {
        ulidString = ULID.randomULID().toString()
        ulid = ULID.randomULID()
        uuidString = UUID.randomUUID().toString()
        monoULID = ULID.MonotonicGenerator(ulid)
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun generationThroughput(): String {
        return ULID.randomULID().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun generationAverage(): String {
        return ULID.randomULID().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun monoGenerationThroughput(): String {
        return monoULID().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun monoGenerationAverage(): String {
        return monoULID.toString()
    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun decodeThroughput(): String {
        return ULID.fromString(ulidString).getOrThrow().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun decodeAverage(): String {
        return ULID.fromString(ulidString).getOrThrow().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun uuidGenerationThroughput(): String {
        return UUID.randomUUID().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun uuidGenerationAverage(): String {
        return UUID.randomUUID().toString()
    }
}