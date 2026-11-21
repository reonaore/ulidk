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
) {

    companion object {

        internal const val BYTE_SIZE = 10

        /**
         * Build entropy from its 16 decoded Base32 digits.
         * @param digits decoded Base32 digits
         */
        @Suppress("MagicNumber")
        fun fromBase32Digits(digits: List<Long>): Entropy {
            require(digits.size == 16) { "Expected 16 Base32 digits" }
            val msb = EntropyValue(digits.subList(0, 8))
            val lsb = EntropyValue(digits.subList(8, 16))
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
internal class EntropyValue(value: Long) {

    val value = value and BIT_MASK

    companion object {
        private const val BASE32_LENGTH = 8
        private const val BIT_MASK = 0xffffffffff
        const val BYTE_SIZE = 5
    }

    operator fun inc() = EntropyValue(value + 1)

    constructor(digits: List<Long>) : this(decodeBase32Digits(digits, BASE32_LENGTH))
    constructor(binary: ByteArray) : this(readBinary(binary, BYTE_SIZE))

    /**
     * Write the value as binary
     */
    fun write(buf: Sink) = buf.writeBinary(value, BYTE_SIZE)

    /**
     * Write the value with Base32 encoded to buffer
     */
    fun writeBase32(buf: Sink) = buf.writeBase32(value, BASE32_LENGTH)

    /**
     * @return true if the value is 40bits all high
     */
    fun isFull() = value == BIT_MASK

    override fun equals(other: Any?): Boolean = other is EntropyValue && value == other.value

    override fun hashCode(): Int = value.hashCode()
}
