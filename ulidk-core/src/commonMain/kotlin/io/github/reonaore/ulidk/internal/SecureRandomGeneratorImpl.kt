package io.github.reonaore.ulidk.internal

import org.kotlincrypto.random.CryptoRand

class SecureRandomGeneratorImpl : SecureRandomGenerator {
    private val random = CryptoRand.Default

    override fun nextBytes(length: Int): ByteArray {
        return ByteArray(length).also { random.nextBytes(it) }
    }
}
