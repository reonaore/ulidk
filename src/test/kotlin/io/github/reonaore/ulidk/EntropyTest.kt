package io.github.reonaore.ulidk

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.ByteBuffer

class EntropyTest : FunSpec({
    test("binary") {
        val input = ByteArray(10) { index ->
            index.toByte()
        }
        val testee = Entropy.fromBinary(input)
        testee.msb.value shouldBe 0x0001020304
        testee.lsb.value shouldBe 0x0506070809

        testee.binary shouldBe input
    }
    test("base32") {
        val input = listOf(
            0x00,
            0x44,
            0x32,
            0x14,
            0xc7,
            0x42,
            0x54,
            0xb6,
            0x35,
            0xcf,
        ).map { it.toByte() }.toByteArray()
        val testee = Entropy.fromBinary(input)
        val got = ByteBuffer.allocate(16)
        testee.writeBase32(got)
        got.flip()
        String(got.array()) shouldBe "0123456789ABCDEF"
    }

    context("increment") {
        test("entropy value") {
            EntropyValue(1).inc().value shouldBe EntropyValue(2).value
            EntropyValue(0xffffffffff).inc().value shouldBe EntropyValue(0).value
        }
        test("entropy") {
            Entropy(
                msb = EntropyValue(1),
                lsb = EntropyValue(2)
            ).inc().apply {
                msb.value shouldBe EntropyValue(1).value
                lsb.value shouldBe EntropyValue(3).value
            }
            Entropy(
                msb = EntropyValue(0xffffffffff),
                lsb = EntropyValue(0xffffffffff)
            ).inc().apply {
                msb.value shouldBe EntropyValue(0).value
                lsb.value shouldBe EntropyValue(0).value
            }
            Entropy(
                msb = EntropyValue(0x0),
                lsb = EntropyValue(0xffffffffff)
            ).inc().apply {
                msb.value shouldBe EntropyValue(1).value
                lsb.value shouldBe EntropyValue(0).value
            }
        }
    }
})
