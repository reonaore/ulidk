package io.github.reonaore.ulidk

import java.nio.ByteBuffer

/**
 * This class stands for the randomness part of ULID
 * @property binary entropy as binary
 * @property msb Most significant bits of the entropy (40bits)
 * @property lsb Least significant bits of the entropy (40bits)
 */
internal class Entropy(
    val msb: EntropyValue,
    val lsb: EntropyValue,
) {

    companion object {

        internal const val BYTE_SIZE = 10

        /**
         * This method is used to generate EntropyValue from bits list that is decoded from Base32 encoded string
         * @param byteList
         */
        fun fromDecodedBytes(byteList: List<Long>): Entropy {
            val msb = EntropyValue.fromDecodedBytes(byteList.subList(0, 8))
            val lsb = EntropyValue.fromDecodedBytes(byteList.subList(8, 16))
            return Entropy(msb, lsb)
        }

        /**
         * This method is used to generate Entropy binary
         * @param binary the length must be 80 bits
         */
        fun fromBinary(binary: ByteArray): Entropy {
            require(binary.size == BYTE_SIZE) {
                "Binary length must be $BYTE_SIZE"
            }
            return Entropy(
                msb = EntropyValue(binary.copyOfRange(0, EntropyValue.BYTE_SIZE)),
                lsb = EntropyValue(binary.copyOfRange(EntropyValue.BYTE_SIZE, BYTE_SIZE))
            )
        }
    }

    operator fun inc(): Entropy {
        val l = lsb.inc()
        val m = if (l.value == 0L) {
            msb.inc()
        } else msb
        return Entropy(m, l)
    }

    /**
     * Write the value as binary
     */
    internal fun write(buf: ByteBuffer) {
        msb.write(buf)
        lsb.write(buf)
    }

    val binary: ByteArray
        get() = ByteBuffer.allocate(BYTE_SIZE).apply {
            write(this)
            flip()
        }.array()

    /**
     * Write the value as binary
     */
    fun writeBase32(buf: ByteBuffer) {
        msb.writeBase32(buf)
        lsb.writeBase32(buf)
    }

    /**
     * @return true if the value has 80bits all high
     */
    fun isFull(): Boolean = msb.isFull() && lsb.isFull()
}

/**
 * This class stands for variable which has 40 bits
 * @property value this value has 40 bits
 */
internal class EntropyValue(value: Long) {

    val value = value and BIT_MASK

    companion object {
        private const val CHUNK_BITS = 40 // CHUNK_BYTES * BYTE_BITS
        private const val BIT_MASK = 0xffffffffff
        const val BYTE_SIZE = 5
        private const val BYTE_BITS = 8
        private const val BYTE_MASK = 0xffL
        private const val BASE32_MASK = 0x1fL

        @Throws(IllegalArgumentException::class)
        private fun parseBinary(binary: ByteArray): Long {
            require(binary.size == BYTE_SIZE) {
                "Binary length must be $BYTE_SIZE"
            }
            var v = 0L
            for (bin in binary) {
                v = v shl BYTE_BITS or (bin.toLong() and BYTE_MASK)
            }
            return v and BIT_MASK
        }

        /**
         * This method is used to generate EntropyValue from bits list that is decoded from Base32 encoded string
         * @param byteList
         */
        fun fromDecodedBytes(byteList: List<Long>): EntropyValue {
            var chunk = 0L
            // 35 = (chunk string length (8) - 1) * 5 bit
            (35 downTo 0 step ULID.BIT_NUM).forEachIndexed { index, shiftBits ->
                chunk = chunk or (byteList[index] shl shiftBits)
            }
            return EntropyValue(chunk)
        }

    }

    operator fun inc(): EntropyValue = EntropyValue(value + 1)

    constructor(binary: ByteArray) : this(parseBinary(binary))

    /**
     * Write the value as binary
     */
    fun write(buf: ByteBuffer) {
        for (shiftBits in 32 downTo 0 step BYTE_BITS) {
            buf.put(((value ushr shiftBits) and 0xff).toByte())
        }
    }

    /**
     * Write the value with Base32 encoded to buffer
     */
    fun writeBase32(buf: ByteBuffer) {
        for (shiftBits in CHUNK_BITS - ULID.BIT_NUM downTo 0 step ULID.BIT_NUM) {
            buf.put(ULID.toBase32[(value ushr shiftBits and BASE32_MASK).toInt()])
        }
    }

    /**
     * @return true if the value is 40bits all high
     */
    fun isFull(): Boolean = value == BIT_MASK
}
