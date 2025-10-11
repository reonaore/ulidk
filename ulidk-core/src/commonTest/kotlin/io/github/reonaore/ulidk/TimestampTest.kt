package ulidk

import io.github.reonaore.ulidk.Timestamp
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class TimestampTest {
    @Test
    fun binary() {
        val input = 0x010203040506
        val buf = Buffer()
        Timestamp(input).write(buf)
        val b = buf.readByteArray()
        assertEquals(ByteArray(6) { (it + 1).toByte() }.toList(), b.toList())
        val got = Timestamp.fromBinary(b)
        assertEquals(input, got.value)
    }
}
