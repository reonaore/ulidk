package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.Consts
import io.github.reonaore.ulidk.internal.ULIDComponent
import io.github.reonaore.ulidk.internal.ULIDComponentFactory

/**
 * This class stands for timestamp part of ULID
 * @property value timestamp which is 48 bits
 */
internal class Timestamp : ULIDComponent {
    constructor(value: Long) : super(Companion, value)
    constructor(byteList: List<Long>) : super(Companion, byteList)
    constructor(binary: ByteArray) : super(Companion, binary)

    companion object : ULIDComponentFactory {
        override val bitMask = Consts.BIT_MASK_48
        override val bitSize = Consts.TIMESTAMP_BIT_SIZE
        override val base32StringLength = 10

        fun fromDecodedBytes(byteList: List<Long>) = Timestamp(byteList)
        fun fromBinary(bin: ByteArray) = Timestamp(bin)
    }
}
