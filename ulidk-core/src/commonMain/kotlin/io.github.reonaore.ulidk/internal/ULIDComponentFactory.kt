package io.github.reonaore.ulidk.internal

/**
 * Factory interface that provides ULID component constants per concrete component.
 */
internal interface ULIDComponentFactory {
    val bitMask: Long
    val bitSize: Int
    val base32StringLength: Int

    @Suppress("MagicNumber")
    fun parseBinary(binary: ByteArray, bitSize: Int): Long {
        val expectedSize = bitSize / 8
        require(binary.size == expectedSize) { "Binary size must be $expectedSize for $bitSize bits" }
        var res = 0L
        val startBits = bitSize - 8
        (startBits downTo 0 step 8).forEachIndexed { index, shiftBits ->
            res = res or ((binary[index].toLong() and 0xFF) shl shiftBits)
        }
        return res
    }

    fun fromDecodedBytes(bytes: List<Long>, base32StringLength: Int): Long =
        decodeBase32Bytes(bytes, base32StringLength)
}
