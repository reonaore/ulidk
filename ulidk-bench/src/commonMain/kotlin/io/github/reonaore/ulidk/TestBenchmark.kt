package io.github.reonaore.ulidk

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import org.openjdk.jmh.annotations.Warmup
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Warmup(iterations = 5, time = 500, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@State(Scope.Benchmark)
class TestBenchmark {
    var ulidString = ""
    var uuidString = ""
    var ulid: ULID = ULID.randomULID()
    var monoULID = ULID.MonotonicGenerator(ulid)

    @Setup
    fun setup() {
        ulidString = ULID.randomULID().toString()
        ulid = ULID.randomULID()
        uuidString = Uuid.random().toString()
        monoULID = ULID.MonotonicGenerator(ulid)
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
    fun generationThroughput(): String {
        return ULID.randomULID().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
    fun generationAverage(): String {
        return ULID.randomULID().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
    fun monoGenerationThroughput(): String {
        return monoULID().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
    fun monoGenerationAverage(): String {
        return monoULID.toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
    fun decodeThroughput(): String {
        return ULID.fromString(ulidString).toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
    fun decodeAverage(): String {
        return ULID.fromString(ulidString).toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
    fun uuidGenerationThroughput(): String {
        return Uuid.random().toString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
    fun uuidGenerationAverage(): String {
        return Uuid.random().toString()
    }
}
