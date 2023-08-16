package io.github.reonaore.ulidk

import java.nio.ByteBuffer

/**
 * This class stands for timestamp part of ULID
 * @property value timestamp which is 48 bits
 */
internal class Timestamp(
    val value: Long
) {

    companion object {
        private const val BYTE_BITS = 8
        private const val BIT_MASK = 0xffffffffffff
        private const val BIT_SIZE = 48

        fun fromDecodedBytes(byteList: List<Long>): Timestamp {
            var timestamp = 0L
            // 45 = (timestamp string length (10) - 1) * 5 bit
            (45 downTo 0 step ULID.BIT_NUM).forEachIndexed { index, shiftBits ->
                timestamp = timestamp or ((byteList[index] shl shiftBits))
            }
            return Timestamp(timestamp and BIT_MASK)
        }

        fun fromBinary(bin: ByteArray): Timestamp {
            var timestamp = 0L
            (BIT_SIZE - BYTE_BITS downTo 0 step BYTE_BITS).forEachIndexed { index, shiftBits ->
                timestamp = timestamp or ((bin[index].toLong() and 0xff) shl shiftBits)
            }
            return Timestamp(timestamp)
        }
    }

    fun writeBase32(buf: ByteBuffer) {
        // 45 = (timestamp string length (10) - 1) * 5 bit
        for (shiftBits in 45 downTo 0 step ULID.BIT_NUM) {
            buf.put(ULID.toBase32[((value ushr shiftBits) and 0x1f).toInt()])
        }
    }

    fun write(buf: ByteBuffer) {
        for (shiftBites in 40 downTo 0 step BYTE_BITS) {
            buf.put(((value ushr shiftBites and 0xff).toByte()))
        }
    }
}