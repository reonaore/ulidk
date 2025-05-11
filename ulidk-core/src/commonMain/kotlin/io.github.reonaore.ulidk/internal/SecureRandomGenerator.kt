package io.github.reonaore.ulidk.internal

import kotlin.random.Random

interface SecureRandomGenerator {
    fun nextBytes(length: Int): ByteArray
}

expect fun getSecureRandomGenerator(): SecureRandomGenerator

object InSecureRandomGenerator : SecureRandomGenerator {
    override fun nextBytes(length: Int): ByteArray {
        return ByteArray(length) { Random.nextInt(0, 256).toByte() }
    }
}
