package io.github.reonaore.ulidk.internal


interface SecureRandomGenerator {
    fun nextBytes(length: Int): ByteArray
}

fun getSecureRandomGenerator(): SecureRandomGenerator {
    return SecureRandomGeneratorImpl()
}
