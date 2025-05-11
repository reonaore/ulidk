package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.SecureRandomGenerator
import io.github.reonaore.ulidk.internal.getSecureRandomGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
        private const val TIMESTAMP_BINARY_SIZE = 6
        private const val STRING_LENGTH = 26
        private const val BASE32_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
        internal val toBase32 = BASE32_ALPHABET.toList().map { it.code.toByte() }
        private val base32LookUp = BASE32_ALPHABET.withIndex().associateBy({ it.value }, { it.index.toLong() })
        internal const val BIT_NUM = 5
        private const val BIT_MASK = 0x1fL

        private val random = getSecureRandomGenerator()

        /**
         * Generates a new random ULID from timestamp(ms). If the timestamp omitted, current time will be used.
         * @return a new ULID with an entropy.
         * @param timestamp timestamp of the newly generated ULID.
         * @param random an instance of SecureRandom that is used to generate an entropy.
         */
        fun randomULID(
            timestamp: Long = Clock.System.now().toEpochMilliseconds(),
            random: SecureRandomGenerator = Companion.random
        ): ULID {
            val entropy = random.nextBytes(Entropy.BYTE_SIZE)
            return ULID(Timestamp(timestamp), Entropy.fromBinary(entropy))
        }

        /**
         * Decode ULID from Base32 encoded string.
         * @param str Base32 encoded string.
         * @return Decoded ULID if the string is valid
         * @throws IllegalArgumentException
         */
        @Suppress("MagicNumber")
        fun fromString(str: String): ULID {
            require(str.length == STRING_LENGTH) { "String length must be $STRING_LENGTH" }

            val byteList = str.toList().map {
                base32LookUp[it]?.and(BIT_MASK)
                    ?: throw IllegalArgumentException("Input string has some invalid chars")
            }
            return ULID(
                timestamp = Timestamp.fromDecodedBytes(byteList.subList(0, 10)),
                entropy = Entropy.fromDecodedBytes(byteList.subList(10, 26)),
            )
        }

        private fun fromBinary(bin: ByteArray): ULID {
            require(bin.size == BINARY_SIZE) { "Binary size must be $BINARY_SIZE" }
            val timestamp = Timestamp.fromBinary(bin.sliceArray(0 until TIMESTAMP_BINARY_SIZE))
            val entropy = Entropy.fromBinary(bin.sliceArray(TIMESTAMP_BINARY_SIZE until BINARY_SIZE))
            return ULID(timestamp, entropy)
        }

        /**
         * Generates ULID from UUID
         */
        @OptIn(ExperimentalUuidApi::class)
        fun fromUUID(uuid: Uuid): ULID {
            val buf = Buffer()
            return uuid.toLongs { mostSignificantBits, leastSignificantBits ->
                buf.writeLong(mostSignificantBits)
                buf.writeLong(leastSignificantBits)
                fromBinary(buf.readByteArray())
            }
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
     * @return timestamp as Instant
     */
    fun instant(): Instant = Instant.fromEpochMilliseconds(timestamp.value)

    /**
     * @return randomness of the ULID
     */
    fun entropy(): ByteArray = entropy.binary

    val binary
        get(): Source {
            val buf = Buffer()
            timestamp.write(buf)
            entropy.write(buf)
            return buf
        }


    private fun generateString(): String {
        return with(Buffer()) {
            timestamp.writeBase32(this)
            entropy.writeBase32(this)
            this.readString()
        }
    }

    private val str: String by lazy { generateString() }

    /**
     * @return Base32 encoded string
     */
    override fun toString() = str
    override fun compareTo(other: ULID): Int {
        return str.compareTo(other.str)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ULID) return false
        return str == other.str
    }

    override fun hashCode(): Int {
        return str.hashCode()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun toUUID(): Uuid {
        val buf = binary
        return Uuid.fromLongs(buf.readLong(), buf.readLong())
    }

}
