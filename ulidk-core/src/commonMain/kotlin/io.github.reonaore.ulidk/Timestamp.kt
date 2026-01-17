package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.Base32Encoder
import io.github.reonaore.ulidk.internal.BinaryReadWriter
import io.github.reonaore.ulidk.internal.ULIDComponent
import io.github.reonaore.ulidk.internal.ULIDConstants
import kotlinx.io.Sink

/**
 * This class stands for timestamp part of ULID
 * @property value timestamp which is 48 bits
 */
internal class Timestamp(value: Long) : ULIDComponent(value, ULIDConstants.BIT_MASK_48) {
    override val base32StringLength = 10

    companion object : Base32Encoder, BinaryReadWriter {
        override val base32StringLength = 10
        override val bitSize = ULIDConstants.TIMESTAMP_BIT_SIZE

        fun fromDecodedBytes(byteList: List<Long>): Timestamp {
            return Timestamp(decodeBytes(byteList))
        }

        fun fromBinary(bin: ByteArray): Timestamp {
            return Timestamp(readBinary(bin))
        }
    }
}
