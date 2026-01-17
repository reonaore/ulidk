package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.Consts
import io.github.reonaore.ulidk.internal.ULIDComponent
import io.github.reonaore.ulidk.internal.decodeBase32Bytes

/**
 * This class stands for timestamp part of ULID
 * @property value timestamp which is 48 bits
 */
internal class Timestamp(value: Long) : ULIDComponent(value, Consts.BIT_MASK_48) {
    override val base32StringLength = BASE32_STRING_LENGTH

    companion object {

        private const val BASE32_STRING_LENGTH = 10

        fun fromDecodedBytes(byteList: List<Long>): Timestamp {
            val decoded = decodeBase32Bytes(byteList, BASE32_STRING_LENGTH)
            return Timestamp(decoded)
        }

        fun fromBinary(bin: ByteArray): Timestamp {
            return Timestamp(parseBinary(bin, Consts.TIMESTAMP_BIT_SIZE))
        }
    }
}
