package ulidk

import io.github.reonaore.ulidk.Entropy
import io.github.reonaore.ulidk.EntropyValue
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals

class EntropyTest {

    @Test
    fun binary() {
        val input = ByteArray(10) { index ->
            index.toByte()
        }
        val testee = Entropy.fromBinary(input)
        assertEquals(0x0001020304, testee.msb.value)
        assertEquals(0x0506070809, testee.lsb.value)

        assertEquals(input.toList(), testee.binary.toList())
    }

    @Test
    fun base32() {
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
        val got = Buffer()
        testee.writeBase32(got)
        assertEquals("0123456789ABCDEF", got.readString())
    }

    @Test
    fun incrementEntropyValue() {
        assertEquals(EntropyValue(2).value, EntropyValue(1).inc().value)
        assertEquals(EntropyValue(0).value, EntropyValue(0xffffffffff).inc().value)
    }

    @Test
    fun incrementBoundary() {
        Entropy(
            msb = EntropyValue(1),
            lsb = EntropyValue(2)
        ).inc().apply {
            assertEquals(EntropyValue(1).value, msb.value)
            assertEquals(EntropyValue(3).value, lsb.value)
        }
        Entropy(
            msb = EntropyValue(0xffffffffff),
            lsb = EntropyValue(0xffffffffff)
        ).inc().apply {
            assertEquals(EntropyValue(0).value, msb.value)
            assertEquals(EntropyValue(0).value, lsb.value)
        }
        Entropy(
            msb = EntropyValue(0x0),
            lsb = EntropyValue(0xffffffffff)
        ).inc().apply {
            assertEquals(EntropyValue(1).value, msb.value)
            assertEquals(EntropyValue(0).value, lsb.value)
        }
    }
}
