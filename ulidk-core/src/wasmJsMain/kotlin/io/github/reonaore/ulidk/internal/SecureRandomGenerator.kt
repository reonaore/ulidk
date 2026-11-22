package io.github.reonaore.ulidk.internal

import kotlin.uuid.Uuid

actual fun getSecureRandomGenerator(): SecureRandomGenerator = object : SecureRandomGenerator {
    override fun nextBytes(length: Int): ByteArray {
        val result = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val bytes = Uuid.random().toByteArray()
            // UUID version and variant bits occupy bytes 6 and 8; use only unmodified bytes.
            for (index in 0..5) {
                if (offset == length) break
                result[offset++] = bytes[index]
            }
            for (index in 10..15) {
                if (offset == length) break
                result[offset++] = bytes[index]
            }
        }
        return result
    }
}
