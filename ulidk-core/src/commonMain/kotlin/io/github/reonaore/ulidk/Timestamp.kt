package io.github.reonaore.ulidk

import kotlinx.io.Sink

/**
 * This class stands for timestamp part of ULID
 * @property value timestamp which is 48 bits
 */
internal class Timestamp(
    value: Long
) {
    val value = value.also {
        require(it in 0..BIT_MASK) {
            "Timestamp must be between 0 and $BIT_MASK"
        }
    }

    companion object {
        private const val BASE32_LENGTH = 10
        private const val BYTE_SIZE = 6
        private const val BIT_MASK = 0xffffffffffff

        fun fromBase32Digits(digits: List<Long>): Timestamp {
            return Timestamp(decodeBase32Digits(digits, BASE32_LENGTH))
        }

        fun fromBinary(bin: ByteArray): Timestamp {
            return Timestamp(readBinary(bin, BYTE_SIZE))
        }
    }

    fun writeBase32(buf: Sink) = buf.writeBase32(value, BASE32_LENGTH)

    fun write(buf: Sink) = buf.writeBinary(value, BYTE_SIZE)
}
