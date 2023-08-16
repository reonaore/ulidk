package io.github.reonaore.ulidk

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.ByteBuffer

class TimestampTest : FunSpec({
    test("binary") {
        val input = 0x010203040506
        val buf = ByteBuffer.allocate(6)
        Timestamp(input).write(buf)
        buf.array() shouldBe ByteArray(6) { (it + 1).toByte() }
        val got = Timestamp.fromBinary(buf.array())
        got.value shouldBe input
    }
})
