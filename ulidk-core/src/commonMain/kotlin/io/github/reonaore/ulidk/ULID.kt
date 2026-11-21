package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.SecureRandomGenerator
import io.github.reonaore.ulidk.internal.getSecureRandomGenerator
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ULID class
 * @property timestamp the timestamp of the ULID
 * @property entropy the randomness of the ULID
 * @property binary the binary of the ULID. It has 128 bits.
 */
@OptIn(ExperimentalTime::class)
@Serializable(with = ULID.Serializer::class)
class ULID internal constructor(
    private val timestamp: Timestamp,
    private val entropy: Entropy,
) : Comparable<ULID> {

    companion object {
        private const val BINARY_SIZE = 16
        private const val TIMESTAMP_BINARY_SIZE = 6
        private const val STRING_LENGTH = 26

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
            val validTimestamp = Timestamp(timestamp)
            val entropy = random.nextBytes(Entropy.BYTE_SIZE)
            return ULID(validTimestamp, Entropy.fromBinary(entropy))
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

            val byteList = Base32Decoder.decodeBase32(str)
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
            return with(Buffer()) {
                uuid.toLongs { mostSignificantBits, leastSignificantBits ->
                    writeLong(mostSignificantBits)
                    writeLong(leastSignificantBits)
                    fromBinary(readByteArray())
                }
            }
        }
    }

    /**
     * MonotonicGenerator generates new monotonic ULIDs.
     * @constructor creates its instance from an ULID.
     * @property timestamp timestamp of the base ULID.
     * @property entropy entropy of the base ULID. This is updated when a new ULID is generated.
     */
    class MonotonicGenerator private constructor(
        ulid: ULID,
        private val random: SecureRandomGenerator,
    ) {
        constructor(ulid: ULID = randomULID()) : this(ulid, ULID.random)

        internal companion object {
            fun withRandom(
                ulid: ULID,
                random: SecureRandomGenerator,
            ) = MonotonicGenerator(ulid, random)
        }

        private var timestamp = ulid.timestamp.value
        private var entropy = ulid.entropy

        /**
         * Generate a new monotonic ULID.
         * @param timestamp timestamp of newly generated ULID.
         * @return monotonic ULID
         * @throws IllegalStateException when entropy is overflowed
         */
        operator fun invoke(timestamp: Long = this.timestamp): ULID {
            val requestedTimestamp = Timestamp(timestamp).value
            if (requestedTimestamp > this.timestamp) {
                val nextEntropy = Entropy.fromBinary(random.nextBytes(Entropy.BYTE_SIZE))
                this.timestamp = requestedTimestamp
                entropy = nextEntropy
            } else {
                check(!entropy.isFull()) {
                    "Entropy will be overflowed"
                }
                entropy++
            }
            return ULID(timestamp = Timestamp(this.timestamp), entropy = entropy)
        }
    }

    object Serializer : KSerializer<ULID> {
        override val descriptor = PrimitiveSerialDescriptor(
            serialName = "io.github.reonaore.ulidk.ULID",
            kind = PrimitiveKind.STRING
        )

        override fun serialize(encoder: Encoder, value: ULID) {
            encoder.encodeString(value.str)
        }

        override fun deserialize(decoder: Decoder): ULID {
            return fromString(decoder.decodeString())
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

    private val binaryBytes: ByteArray by lazy {
        with(Buffer()) {
            timestamp.write(this)
            entropy.write(this)
            readByteArray()
        }
    }

    val binary: Source
        get() = Buffer().apply {
            write(binaryBytes)
        }

    private val str: String by lazy {
        with(Buffer()) {
            timestamp.writeBase32(this)
            entropy.writeBase32(this)
            readString()
        }
    }

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
        val source = binary
        return Uuid.fromLongs(source.readLong(), source.readLong())
    }

}
