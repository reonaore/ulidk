package io.onare.ulidk

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ULIDTest : FunSpec({

    test("generate") {
        val ulid = ULID.randomULID().toString()
        val got = ULID.fromString(ulid).getOrThrow().toString()
        ulid shouldBe got
    }

    test("sortable") {
        val first = ULID.randomULID(timestamp = 0)
        val second = ULID.randomULID(timestamp = 1)
        val testee = mutableListOf<ULID>()

        testee.add(second)
        testee.add(first)
        val got = testee.sorted()
        got.first() shouldBe first
        got[1] shouldBe second
    }
})
