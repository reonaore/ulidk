package io.github.reonaore.ulidk

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
) : Comparable<Entropy> {

    companion object {

        internal const val BYTE_SIZE = 10

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

    override fun compareTo(other: Entropy): Int {
        val msbComp = msb.compareTo(other.msb)
        if (msbComp != 0) return msbComp
        return lsb.compareTo(other.lsb)
    }
}

/**
 * This class stands for variable which has 40 bits
 * @property value this value has 40 bits
 */
internal class EntropyValue(value: Long) : Comparable<EntropyValue> {

    val value = value and BIT_MASK

    companion object : Base32Encoder, BinaryReadWriter {
        override val bitSize = 40
        override val base32StringLength = 8
        private const val BIT_MASK = 0xffffffffffL
        const val BYTE_SIZE = 5

        @Throws(IllegalArgumentException::class)
        private fun parseBinary(binary: ByteArray): Long {
            require(binary.size == BYTE_SIZE) {
                "Binary length must be $BYTE_SIZE"
            }
            return readBinary(binary)
        }
    }

    operator fun inc() = EntropyValue(value + 1)

    constructor(byteList: List<Long>) : this(decodeBytes(byteList))
    constructor(binary: ByteArray) : this(parseBinary(binary))

    /**
     * Write the value as binary
     */
    fun write(buf: Sink) = buf.writeBinary(value)

    /**
     * Write the value with Base32 encoded to buffer
     */
    fun writeBase32(buf: Sink) = buf.writeBase32(value)

    /**
     * @return true if the value is 40bits all high
     */
    fun isFull() = value == BIT_MASK

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EntropyValue) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun compareTo(other: EntropyValue): Int = value.compareTo(other.value)
}
