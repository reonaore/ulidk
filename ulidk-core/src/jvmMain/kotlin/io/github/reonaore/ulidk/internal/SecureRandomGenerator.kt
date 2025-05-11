package io.github.reonaore.ulidk.internal

import java.security.SecureRandom

actual fun getSecureRandomGenerator(): SecureRandomGenerator = SecureRandomGeneratorImpl

object SecureRandomGeneratorImpl : SecureRandomGenerator {
    private val random = SecureRandom()
    override fun nextBytes(length: Int): ByteArray {
        return ByteArray(length).also { random.nextBytes(it) }
    }
}
