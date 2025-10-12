package io.github.reonaore.ulidk

import kotlinx.io.Sink


internal const val BIT_NUM = 5
internal const val BIT_MASK = 0x1fL
internal const val BASE32_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
internal val toBase32 = BASE32_ALPHABET.toList().map { it.code.toByte() }
internal val base32LookUp = BASE32_ALPHABET.withIndex().associateBy({ it.value }, { it.index.toLong() })

internal interface Base32Encoder {
    val base32StringLength: Int

    val bits: Int
        get() = (base32StringLength - 1) * BIT_NUM

    fun Sink.writeBase32(value: Long) {
        for (shiftBits in bits downTo 0 step BIT_NUM) {
            writeByte(toBase32[((value ushr shiftBits) and BIT_MASK).toInt()])
        }
    }

    fun decodeBytes(bytes: List<Long>): Long {
        var res = 0L
        (bits downTo 0 step BIT_NUM).forEachIndexed { index, shiftBits ->
            res = res or ((bytes[index] and BIT_MASK) shl shiftBits)
        }
        return res
    }
}

internal object Base32Decoder {
    fun decodeBase32(str: String): List<Long> {
        return str.toList().map {
            base32LookUp[it]?.and(BIT_MASK)
                ?: throw IllegalArgumentException("Input string has some invalid chars")
        }
    }
}
