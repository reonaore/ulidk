package io.github.reonaore.ulidk.internal


interface SecureRandomGenerator {
    fun nextBytes(length: Int): ByteArray
}

expect fun getSecureRandomGenerator(): SecureRandomGenerator
