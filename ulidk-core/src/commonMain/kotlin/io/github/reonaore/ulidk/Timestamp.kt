package io.github.reonaore.ulidk

import kotlinx.io.Sink

/**
 * This class stands for timestamp part of ULID
 * @property value timestamp which is 48 bits
 */
internal class Timestamp(
    value: Long
) : Comparable<Timestamp> {
    init {
        require(value in 0..BIT_MASK) { "Timestamp value must be between 0 and $BIT_MASK, but was $value" }
    }

    val value = value

    companion object : Base32Encoder, BinaryReadWriter {
        override val base32StringLength = 10
        override val bitSize = 48
        private const val BIT_MASK = 0xffffffffffffL

        @Suppress("MagicNumber")
        fun fromDecodedBytes(byteList: List<Long>): Timestamp {
            require(byteList.isNotEmpty() && byteList[0] <= 7L) {
                "Timestamp overflow: first 5-bit value cannot exceed 7, but was ${byteList.getOrNull(0)}"
            }
            return Timestamp(decodeBytes(byteList))
        }

        fun fromBinary(bin: ByteArray): Timestamp {
            return Timestamp(readBinary(bin))
        }
    }

    fun writeBase32(buf: Sink) = buf.writeBase32(value)

    fun write(buf: Sink) = buf.writeBinary(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Timestamp) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun compareTo(other: Timestamp): Int = value.compareTo(other.value)
}
