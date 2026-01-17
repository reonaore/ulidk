package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.Base32Decoder
import io.github.reonaore.ulidk.internal.ULIDConstants
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
    internal val timestampInternal: Timestamp get() = timestamp
    internal val entropyInternal: Entropy get() = entropy

    companion object {
        private const val TIMESTAMP_BINARY_SIZE = 6

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
            require(str.length == ULIDConstants.STRING_LENGTH) { "ULID string must be ${ULIDConstants.STRING_LENGTH} characters long, got ${str.length}" }

            val byteList = try {
                Base32Decoder.decodeBase32(str)
            } catch (e: IllegalArgumentException) {
                throw ULIDParseException("Invalid Base32 characters in ULID string: $str", e)
            }
            return ULID(
                timestamp = Timestamp.fromDecodedBytes(byteList.subList(0, 10)),
                entropy = Entropy.fromDecodedBytes(byteList.subList(10, 26)),
            )
        }

        private fun fromBinary(bin: ByteArray): ULID {
            require(bin.size == ULIDConstants.BINARY_SIZE) { "Binary size must be ${ULIDConstants.BINARY_SIZE}" }
            val timestamp = Timestamp.fromBinary(bin.sliceArray(0 until TIMESTAMP_BINARY_SIZE))
            val entropy = Entropy.fromBinary(bin.sliceArray(TIMESTAMP_BINARY_SIZE until ULIDConstants.BINARY_SIZE))
            return ULID(timestamp, entropy)
        }

        /**
         * Generates ULID from UUID
         */
        @OptIn(ExperimentalUuidApi::class)
        fun fromUUID(uuid: Uuid): ULID {
            val bin = ByteArray(16)
            uuid.toLongs { mostSig, leastSig ->
                for (i in 0..7) {
                    bin[i] = (mostSig shr (56 - i * 8)).toByte()
                    bin[i + 8] = (leastSig shr (56 - i * 8)).toByte()
                }
            }
            return fromBinary(bin)
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

    val binary: Source by lazy {
        with(Buffer()) {
            timestamp.write(this)
            entropy.write(this)
            this
        }
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
        return Uuid.fromLongs(binary.readLong(), binary.readLong())
    }

}
