package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.decodeBase32Bytes
import io.github.reonaore.ulidk.internal.ULIDComponent
import io.github.reonaore.ulidk.internal.Consts
import kotlinx.io.Sink

/**
 * This class stands for timestamp part of ULID
 * @property value timestamp which is 48 bits
 */
internal class Timestamp(value: Long) : ULIDComponent(value, Consts.BIT_MASK_48) {
    override val base32StringLength = 10

    companion object {

        private const val BASE32_STRING_LENGTH = 10
        private val BIT_SIZE = Consts.TIMESTAMP_BIT_SIZE

        private fun decodeBytes(bytes: List<Long>): Long =
            decodeBase32Bytes(bytes, BASE32_STRING_LENGTH)

        private fun readBinary(bin: ByteArray): Long {
            var res = 0L
            val startBits = BIT_SIZE - 8
            (startBits downTo 0 step 8).forEachIndexed { index, shiftBits ->
                res = res or ((bin[index].toLong() and 0xFF) shl shiftBits)
            }
            return res
        }

        fun fromDecodedBytes(byteList: List<Long>): Timestamp {
            return Timestamp(decodeBytes(byteList))
        }

        fun fromBinary(bin: ByteArray): Timestamp {
            return Timestamp(readBinary(bin))
        }
    }
}
