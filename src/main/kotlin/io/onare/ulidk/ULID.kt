package io.onare.ulidk

import java.nio.ByteBuffer
import java.security.SecureRandom

/**
 * ULID class
 * @property timestamp the timestamp of the ULID
 * @property entropy the randomness of the ULID
 * @property binary the binary of the ULID. It has 128 bits.
 */
class ULID internal constructor(
    private val timestamp: Timestamp,
    private val entropy: Entropy,
) : Comparable<ULID> {

    companion object {
        private const val BINARY_SIZE = 16
        private const val STRING_LENGTH = 26
        private const val BASE32_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
        internal val toBase32 = BASE32_ALPHABET.toList().map { it.code.toByte() }
        private val base32LookUp = BASE32_ALPHABET.withIndex().associateBy({ it.value }, { it.index.toLong() })
        internal const val BIT_NUM = 5
        private const val BIT_MASK = 0x1fL

        private val random = SecureRandom()

        /**
         * Generates a new random ULID from timestamp(ms). If the timestamp omitted, current time will be used.
         * @return a new ULID with an entropy.
         * @param timestamp timestamp of the newly generated ULID.
         * @param random an instance of SecureRandom that is used to generate an entropy.
         */
        fun randomULID(timestamp: Long = System.currentTimeMillis(), random: SecureRandom = Companion.random): ULID {
            val entropy = ByteArray(Entropy.BYTE_SIZE)
            random.nextBytes(entropy)
            return ULID(Timestamp(timestamp), Entropy.fromBinary(entropy))
        }

        /**
         * Decode ULID from Base32 encoded string.
         * @param str Base32 encoded string.
         * @return Decoded ULID if the string is valid
         */
        fun fromString(str: String): Result<ULID> {
            if (str.length != STRING_LENGTH) {
                return Result.failure(IllegalArgumentException("String length must be $STRING_LENGTH"))
            }
            val byteList = str.toList().map {
                base32LookUp[it]?.and(BIT_MASK)
                    ?: return Result.failure(IllegalArgumentException("Input string has some invalid chars"))
            }
            return Result.success(
                ULID(
                    timestamp = Timestamp.fromDecodedBytes(byteList.subList(0, 10)),
                    entropy = Entropy.fromDecodedBytes(byteList.subList(10, 26)),
                )
            )
        }
    }

    /**
     * MonotonicGenerator generates new monotonic ULIDs.
     * @constructor creates its instance from an ULID.
     * @property timestamp timestamp of the base ULID.
     * @property entropy entropy of the base ULID. This is updated when a new ULID is generated.
     */
    class MonotonicGenerator(ulid: ULID = randomULID()) {
        private var timestamp = ulid.timestamp.value
        private var entropy = ulid.entropy

        /**
         * Generate a new monotonic ULID.
         * @param timestamp timestamp of newly generated ULID.
         * @return monotonic ULID
         * @throws IllegalArgumentException when entropy is overflowed
         */
        operator fun invoke(timestamp: Long = this.timestamp): ULID {
            require(!entropy.isFull()) {
                throw IllegalStateException("Entropy will be overflowed")
            }
            return ULID(timestamp = Timestamp(timestamp), entropy = ++entropy)
        }
    }

    /**
     * @return unix time of the ULID
     */
    fun timestamp(): Long = timestamp.value

    /**
     * @return randomness of the ULID
     */
    fun entropy(): ByteArray = entropy.binary

    val binary
        get(): ByteArray =
            ByteBuffer.allocate(BINARY_SIZE).apply {
                timestamp.write(this)
                entropy.write(this)
            }.array()


    private fun generateString(): String {
        val b = ByteArray(STRING_LENGTH)
        val buf = ByteBuffer.wrap(b)
        timestamp.writeBase32(buf)
        entropy.writeBase32(buf)
        return String(b)
    }

    private val str: String by lazy { generateString() }

    /**
     * @return Base32 encoded string
     */
    override fun toString() = str
    override fun compareTo(other: ULID): Int {
        return str.compareTo(other.str)
    }
}
