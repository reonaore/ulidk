package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.decodeBase32Bytes
import io.github.reonaore.ulidk.internal.BinaryReadWriter
import io.github.reonaore.ulidk.internal.ULIDComponent
import io.github.reonaore.ulidk.internal.Consts
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.readByteArray

/**
 * This class stands for the randomness part of ULID
 * @property binary entropy as binary
 * @property msb Most significant bits of the entropy (40bits)
 * @property lsb Least significant bits of the entropy (40bits)
 */
internal data class Entropy(
    val msb: EntropyValue,
    val lsb: EntropyValue,
) {

    companion object {

        internal const val BYTE_SIZE = Consts.ENTROPY_BYTE_SIZE

        /**
         * This method is used to generate EntropyValue from bits list that is decoded from Base32 encoded string
         * @param byteList
         */
        @Suppress("MagicNumber")
        fun fromDecodedBytes(byteList: List<Long>): Entropy {
            val msb = EntropyValue(byteList.subList(0, 8))
            val lsb = EntropyValue(byteList.subList(8, 16))
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
        } else {
            msb
        }
        return Entropy(m, l)
    }

    /**
     * Write the value as binary
     */
    internal fun write(buf: Sink) {
        msb.write(buf)
        lsb.write(buf)
    }

    val binary: ByteArray
        get() = with(Buffer()) {
            write(this)
            readByteArray()
        }

    /**
     * Write the value as binary
     */
    fun writeBase32(buf: Sink) {
        msb.writeBase32(buf)
        lsb.writeBase32(buf)
    }

    /**
     * @return true if the value has 80bits all high
     */
    fun isFull() = msb.isFull() && lsb.isFull()
}

/**
 * This class stands for variable which has 40 bits
 * @property value this value has 40 bits
 */
internal class EntropyValue(value: Long) : ULIDComponent(value, Consts.BIT_MASK_40) {
    override val base32StringLength = 8

    companion object {
            val bitSize = Consts.ENTROPY_VALUE_BIT_SIZE
            val base32StringLength = 8
        const val BYTE_SIZE = Consts.ENTROPY_VALUE_BYTE_SIZE

        @Throws(IllegalArgumentException::class)
        private fun decodeBytes(bytes: List<Long>): Long =
            decodeBase32Bytes(bytes, base32StringLength)
        private fun parseBinary(binary: ByteArray): Long {
            require(binary.size == BYTE_SIZE) {
                "Binary length must be $BYTE_SIZE"
            }
            var res = 0L
            val startBits = bitSize - 8
            (startBits downTo 0 step 8).forEachIndexed { index, shiftBits ->
                res = res or ((binary[index].toLong() and 0xFF) shl shiftBits)
            }
            return res
        }
    }

    operator fun inc() = EntropyValue(value + 1)

    constructor(byteList: List<Long>) : this(Companion.decodeBytes(byteList))
    constructor(binary: ByteArray) : this(parseBinary(binary))

    /**
     * @return true if the value is 40bits all high
     */
    fun isFull() = value == Consts.BIT_MASK_40
}
