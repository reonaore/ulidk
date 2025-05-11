package ulidk

import io.github.reonaore.ulidk.Timestamp
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

class TimestampTest : FunSpec({
    test("binary") {
        val input = 0x010203040506
        val buf = Buffer()
        Timestamp(input).write(buf)
        val b = buf.readByteArray()
        b shouldBe ByteArray(6) { (it + 1).toByte() }
        val got = Timestamp.fromBinary(b)
        got.value shouldBe input
    }
})
