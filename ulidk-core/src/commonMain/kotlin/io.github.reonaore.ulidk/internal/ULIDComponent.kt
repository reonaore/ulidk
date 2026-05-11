package io.github.reonaore.ulidk.internal

import kotlinx.io.Sink

internal abstract class ULIDComponent(factory: ULIDComponentFactory, rawValue: Long) :
    Base32Encoder, BinaryReadWriter {
    val value = rawValue and factory.bitMask
    override val base32StringLength = factory.base32StringLength
    override val bitSize = factory.bitSize

    init {
        require(rawValue >= 0) { "Value must be non-negative" }
    }

    fun writeBase32(buf: Sink) = buf.writeBase32(value)
    fun write(buf: Sink) = buf.writeBinary(value)

    protected constructor(factory: ULIDComponentFactory, byteList: List<Long>) : this(
        factory,
        factory.fromDecodedBytes(
            byteList,
            factory.base32StringLength
        )
    )

    protected constructor(factory: ULIDComponentFactory, binary: ByteArray) : this(
        factory,
        factory.parseBinary(binary, factory.bitSize),
    )
}
