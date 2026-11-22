package io.github.reonaore.ulidk

import kotlinx.io.Sink

@Suppress("MagicNumber")
internal fun readBinary(bytes: ByteArray, byteCount: Int): Long {
    require(bytes.size == byteCount) { "Binary length must be $byteCount" }
    var result = 0L
    for (byte in bytes) {
        result = (result shl 8) or (byte.toLong() and 0xFF)
    }
    return result
}

@Suppress("MagicNumber")
internal fun Sink.writeBinary(value: Long, byteCount: Int) {
    for (shift in (byteCount - 1) * 8 downTo 0 step 8) {
        writeByte((value ushr shift and 0xFF).toByte())
    }
}
