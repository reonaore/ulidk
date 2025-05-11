package io.github.reonaore.ulidk.internal

actual fun getSecureRandomGenerator(): SecureRandomGenerator = InSecureRandomGenerator
