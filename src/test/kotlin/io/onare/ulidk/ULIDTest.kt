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

    test("decode") {
        val testee = ULID.fromString("01H7PN3EH10123456789ABCDEF").getOrThrow()
        testee.timestamp() shouldBe 1691903703585L
        testee.entropy() shouldBe listOf(
            0x00,
            0x44,
            0x32,
            0x14,
            0xc7,
            0x42,
            0x54,
            0xB6,
            0x35,
            0xcf,
        ).map { it.toByte() }
    }

    test("decode max value") {
        val input = "7ZZZZZZZZZZZZZZZZZZZZZZZZZ"
        val got = ULID.fromString(input).getOrThrow().toString()

        got shouldBe input
    }
    test("decode overflow value") {
        val input = "8ZZZZZZZZZZZZZZZZZZZZZZZZZ"
        val got = ULID.fromString(input).getOrThrow().toString()

        got shouldBe "0ZZZZZZZZZZZZZZZZZZZZZZZZZ"
    }

    test("binary order") {
        val entropy = ByteArray(10) {
            (it + 7).toByte()
        }.let { Entropy.fromBinary(it) }
        val timestamp = Timestamp(0x010203040506)
        val testee = ULID(timestamp, entropy)
        testee.binary shouldBe (1..16).map { it.toByte() }.toByteArray()
    }

})
