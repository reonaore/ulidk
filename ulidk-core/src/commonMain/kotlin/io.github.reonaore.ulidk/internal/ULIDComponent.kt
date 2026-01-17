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
}