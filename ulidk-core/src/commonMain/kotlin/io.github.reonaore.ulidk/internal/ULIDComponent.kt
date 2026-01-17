package io.github.reonaore.ulidk.internal

import io.github.reonaore.ulidk.internal.Base32Encoder
import io.github.reonaore.ulidk.internal.BinaryReadWriter
import kotlinx.io.Sink

internal abstract class ULIDComponent(
    value: Long,
    protected val bitMask: Long
) : Base32Encoder, BinaryReadWriter {
    val value = value and bitMask
    abstract override val base32StringLength: Int
    init {
        require(value >= 0) { "Value must be non-negative" }
    }

    override val bitSize: Int
        get() = 64 - bitMask.countLeadingZeroBits()

    fun writeBase32(buf: Sink) = buf.writeBase32(value)
    fun write(buf: Sink) = buf.writeBinary(value)

    companion object {
        internal fun parseBinary(binary: ByteArray, bitSize: Int): Long {
            val expectedSize = bitSize / 8
            require(binary.size == expectedSize) { "Binary size must be $expectedSize for $bitSize bits" }
            var res = 0L
            val startBits = bitSize - 8
            (startBits downTo 0 step 8).forEachIndexed { index, shiftBits ->
                res = res or ((binary[index].toLong() and 0xFF) shl shiftBits)
            }
            return res
        }

        internal fun fromDecodedBytes(bytes: List<Long>, base32StringLength: Int): Long = decodeBase32Bytes(bytes, base32StringLength)
    }
}
