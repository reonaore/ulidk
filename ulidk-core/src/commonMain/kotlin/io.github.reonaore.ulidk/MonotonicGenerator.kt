package io.github.reonaore.ulidk

/**
 * MonotonicGenerator generates new monotonic ULIDs.
 * @constructor creates its instance from an ULID.
 * @property timestamp timestamp of the base ULID.
 * @property entropy entropy of the base ULID. This is updated when a new ULID is generated.
 */
class MonotonicGenerator(ulid: ULID = ULID.randomULID()) {
    private var timestamp = ulid.timestampInternal.value
    private var entropy = ulid.entropyInternal

    /**
     * Generate a new monotonic ULID.
     * @param timestamp timestamp of newly generated ULID.
     * @return monotonic ULID
     * @throws IllegalArgumentException when entropy is overflowed
     */
    operator fun invoke(timestamp: Long = this.timestamp): ULID {
        require(!entropy.isFull()) {
            throw IllegalStateException("Entropy will be overflowed")
        }
        return ULID(timestamp = Timestamp(timestamp), entropy = ++entropy)
    }
}
