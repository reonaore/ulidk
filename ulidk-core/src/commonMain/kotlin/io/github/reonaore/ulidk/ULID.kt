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
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
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

            val digits = decodeBase32(str)
            return ULID(
                timestamp = Timestamp.fromBase32Digits(digits.subList(0, 10)),
                entropy = Entropy.fromBase32Digits(digits.subList(10, 26)),
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
     * Generates ULIDs in strictly increasing order, including when called concurrently.
     * Calls without a timestamp keep the most recently used timestamp; pass a later timestamp
     * to start a new millisecond with fresh entropy.
     * @constructor creates its instance from an ULID.
     */
    @OptIn(ExperimentalAtomicApi::class)
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

        private class State(val timestamp: Long, val entropy: Entropy)

        private val state = AtomicReference(State(ulid.timestamp.value, ulid.entropy))

        /**
         * Generate a new monotonic ULID.
         * @param timestamp requested timestamp. By default, uses the generator's latest timestamp.
         * A lower timestamp is ignored to preserve ordering.
         * @return monotonic ULID
         * @throws IllegalStateException when entropy is overflowed
         */
        operator fun invoke(timestamp: Long = state.load().timestamp): ULID {
            val requestedTimestamp = Timestamp(timestamp).value
            while (true) {
                val current = state.load()
                val next = if (requestedTimestamp > current.timestamp) {
                    State(requestedTimestamp, Entropy.fromBinary(random.nextBytes(Entropy.BYTE_SIZE)))
                } else {
                    check(!current.entropy.isFull()) {
                        "Entropy will be overflowed"
                    }
                    State(current.timestamp, current.entropy.inc())
                }
                if (state.compareAndSet(current, next)) {
                    return ULID(Timestamp(next.timestamp), next.entropy)
                }
            }
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

    private val binaryValue: ByteArray by lazy {
        with(Buffer()) {
            timestamp.write(this)
            entropy.write(this)
            readByteArray()
        }
    }

    /**
     * The 16-byte binary representation of this ULID.
     *
     * A defensive copy is returned so callers cannot mutate this ULID's cached representation.
     */
    val binaryBytes: ByteArray
        get() = binaryValue.copyOf()

    val binary: Source
        get() = Buffer().apply {
            write(binaryValue)
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
        val timestampComparison = timestamp.value.compareTo(other.timestamp.value)
        val entropyMsbComparison = entropy.msb.value.compareTo(other.entropy.msb.value)
        return when {
            timestampComparison != 0 -> timestampComparison
            entropyMsbComparison != 0 -> entropyMsbComparison
            else -> entropy.lsb.value.compareTo(other.entropy.lsb.value)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ULID) return false
        return timestamp.value == other.timestamp.value &&
            entropy.msb.value == other.entropy.msb.value &&
            entropy.lsb.value == other.entropy.lsb.value
    }

    override fun hashCode(): Int {
        var result = timestamp.value.hashCode()
        result = 31 * result + entropy.msb.value.hashCode()
        result = 31 * result + entropy.lsb.value.hashCode()
        return result
    }

    @OptIn(ExperimentalUuidApi::class)
    @Suppress("MagicNumber")
    fun toUUID(): Uuid {
        val mostSignificantBits = (timestamp.value shl 16) or (entropy.msb.value ushr 24)
        val leastSignificantBits = ((entropy.msb.value and 0xFFFFFFL) shl 40) or entropy.lsb.value
        return Uuid.fromLongs(mostSignificantBits, leastSignificantBits)
    }

}
