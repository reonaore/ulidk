package io.github.reonaore.ulidk

import kotlin.test.Test
import kotlin.test.assertFailsWith

class CodecTest {
    @Test
    fun rejectsWrongBase32DigitCount() {
        assertFailsWith<IllegalArgumentException> {
            decodeBase32Digits(List(7) { 0L }, 8)
        }
    }

    @Test
    fun rejectsWrongBinaryByteCount() {
        assertFailsWith<IllegalArgumentException> {
            readBinary(ByteArray(4), 5)
        }
    }
}
