package io.github.reonaore.ulidk

import kotlinx.io.Sink

private const val BITS_PER_DIGIT = 5
private const val DIGIT_MASK = 0x1fL
private const val BASE32_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
private val encodedDigits = BASE32_ALPHABET.map { it.code.toByte() }
private val decodedDigits = BASE32_ALPHABET.withIndex().associateBy({ it.value }, { it.index.toLong() })

internal fun Sink.writeBase32(value: Long, digitCount: Int) {
    for (shift in (digitCount - 1) * BITS_PER_DIGIT downTo 0 step BITS_PER_DIGIT) {
        writeByte(encodedDigits[((value ushr shift) and DIGIT_MASK).toInt()])
    }
}

internal fun decodeBase32Digits(digits: List<Long>, digitCount: Int): Long {
    require(digits.size == digitCount) { "Expected $digitCount Base32 digits" }
    var result = 0L
    for (digit in digits) {
        result = (result shl BITS_PER_DIGIT) or (digit and DIGIT_MASK)
    }
    return result
}

internal fun decodeBase32(str: String): List<Long> {
    return str.map {
        decodedDigits[it.uppercaseChar()]
            ?: throw IllegalArgumentException("Input string has some invalid chars")
    }
}
