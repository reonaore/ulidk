package io.github.reonaore.ulidk

import kotlinx.io.Sink

/**
 * This class stands for timestamp part of ULID
 * @property value timestamp which is 48 bits
 */
internal class Timestamp(
    value: Long
) {
    val value = value and BIT_MASK

    companion object : Base32Encoder, BinaryReadWriter {
        override val base32StringLength = 10
        override val bitSize = 48
        private const val BIT_MASK = 0xffffffffffff

        fun fromDecodedBytes(byteList: List<Long>): Timestamp {
            return Timestamp(decodeBytes(byteList))
        }

        fun fromBinary(bin: ByteArray): Timestamp {
            return Timestamp(readBinary(bin))
        }
    }

    fun writeBase32(buf: Sink) = buf.writeBase32(value)

    fun write(buf: Sink) = buf.writeBinary(value)
}
