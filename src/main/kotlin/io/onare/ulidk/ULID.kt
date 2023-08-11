package io.onare.ulidk

import java.nio.ByteBuffer
import java.security.SecureRandom

class ULID(
    val timeStamp: Long,
    val entropy: ByteArray,
) : Comparable<ULID> {
    companion object {
        private const val STRING_LENGTH = 26
        private const val BASE32_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
        private val toBase32 = BASE32_ALPHABET.toList().map { it.code.toByte() }
        private val base32LookUp = BASE32_ALPHABET.withIndex().associateBy({ it.value }, { it.index.toLong() })
        private const val BYTE_BITS = 8
        private const val BIT_NUM = 5
        private const val BIT_MASK = 0x1fL

        // private const val CHUNK_BYTES = 5
        private const val CHUNK_BITS = 40 // CHUNK_BYTES * BYTE_BITS
        private const val ENTROPY_BYTES = 10
        private const val ENTROPY_CHUNK_NUM = 2 // (ENTROPY_BYTES * BYTE_BITS) / CHUNK_BITS
        private val random = SecureRandom()
        fun randomULID(timestamp: Long = System.currentTimeMillis()): ULID {
            val entropy = ByteArray(ENTROPY_BYTES)
            random.nextBytes(entropy)
            return ULID(timestamp, entropy)
        }

        fun fromString(str: String): Result<ULID> {
            if (str.length != STRING_LENGTH) {
                return Result.failure(IllegalArgumentException("String length must be $STRING_LENGTH"))
            }
            val byteList = str.toList().map {
                base32LookUp[it]?.and(BIT_MASK)
                    ?: return Result.failure(IllegalArgumentException("Input string has some invalid chars"))
            }
            var timestamp = 0L
            var index = 0
            // 45 = (timestamp string length (10) - 1) * 5 bit
            for (shiftBits in 45 downTo 0 step BIT_NUM) {
                timestamp = timestamp or ((byteList[index++]) shl shiftBits)
            }
            val entropy = ByteArray(ENTROPY_BYTES)
            val entropyBuf = ByteBuffer.wrap(entropy)

            for (i in 0 until ENTROPY_CHUNK_NUM) {
                var chunk = 0L
                // 35 = (chunk string length (8) - 1) * 5 bit
                for (shiftBits in 35 downTo 0 step BIT_NUM) {
                    chunk = chunk or (byteList[index++] shl shiftBits)
                }
                // 32 = (chunk bytes (5) - 1) * 8 bit
                for (shiftBits in 32 downTo 0 step BYTE_BITS) {
                    entropyBuf.put(((chunk ushr shiftBits) and 0xff).toByte())
                }
            }
            return Result.success(ULID(timestamp, entropy))
        }
    }

    private fun generateString(): String {
        val b = ByteArray(STRING_LENGTH)
        val buf = ByteBuffer.wrap(b)
        val rand = ByteBuffer.wrap(entropy)

        // 45 = (valid bit size of timestamp(48) => 50 bits - 5bits
        for (shiftBits in 45 downTo 0 step BIT_NUM) {
            buf.put(toBase32[((timeStamp ushr shiftBits) and 0x1f).toInt()])
        }
        for (i in 0 until ENTROPY_CHUNK_NUM) {
            var chunk: Long = 0
            for (shiftBits in CHUNK_BITS - BYTE_BITS downTo 0 step BYTE_BITS) {
                chunk = chunk or (((rand.get().toLong()) and 0xff) shl shiftBits)
            }
            for (shiftBits in CHUNK_BITS - BIT_NUM downTo 0 step BIT_NUM) {
                buf.put(toBase32[(chunk ushr shiftBits and 0x1f).toInt()])
            }
        }
        return String(b)
    }

    private val str: String by lazy { generateString() }

    override fun toString() = str
    override fun compareTo(other: ULID): Int {
        return str.compareTo(other.str)
    }
}
