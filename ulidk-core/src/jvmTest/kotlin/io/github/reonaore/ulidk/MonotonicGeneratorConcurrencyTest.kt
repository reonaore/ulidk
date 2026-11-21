package io.github.reonaore.ulidk

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class MonotonicGeneratorConcurrencyTest {
    @Test
    fun sharedGeneratorProducesUniqueIds() {
        val seed = ULID.fromString("01BX5ZZKBKACTAV9WEVGEMMVRY")
        val generator = ULID.MonotonicGenerator(seed)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(8)

        try {
            val futures = List(8) {
                executor.submit(Callable {
                    start.await()
                    List(500) { generator().toString() }
                })
            }
            start.countDown()

            val ids = futures.flatMap { it.get(30, TimeUnit.SECONDS) }
            assertEquals(ids.size, ids.toSet().size)
            assertEquals(ids.size, ids.map(ULID::fromString).count { it.timestamp() == seed.timestamp() })
        } finally {
            executor.shutdownNow()
        }
    }
}
