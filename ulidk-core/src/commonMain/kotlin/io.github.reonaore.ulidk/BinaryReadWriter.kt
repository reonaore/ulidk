package io.github.reonaore.ulidk

import kotlinx.io.Sink


@Suppress("MagicNumber")
internal interface BinaryReadWriter {
    val bitSize: Int

    val startBits get() = bitSize - 8

    fun readBinary(bin: ByteArray): Long {
        var res = 0L
        (startBits downTo 0 step 8).forEachIndexed { index, shiftBits ->
            res = res or ((bin[index].toLong() and 0xFF) shl shiftBits)
        }
        return res
    }

    fun Sink.writeBinary(value: Long) {
        for (shiftBites in startBits downTo 0 step 8) {
            writeByte(((value ushr shiftBites and 0xFF).toByte()))
        }
    }
}
