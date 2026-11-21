package io.github.reonaore.ulidk

import io.github.reonaore.ulidk.internal.SecureRandomGenerator
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class ULIDTest {

    private class FakeSecureRandomGenerator(
        private vararg val values: ByteArray,
    ) : SecureRandomGenerator {
        private var index = 0

        override fun nextBytes(length: Int): ByteArray {
            return values[index++].also {
                require(it.size == length)
            }
        }
    }

    @Test
    fun generate() {
        val ulid = ULID.randomULID().toString()
        val got = ULID.fromString(ulid).toString()
        assertEquals(ulid, got)
    }

    @Test
    fun sortable() {
        val first = ULID.randomULID(timestamp = 0)
        val second = ULID.randomULID(timestamp = 1)
        val testee = mutableListOf<ULID>()

        testee.add(second)
        testee.add(first)
        val got = testee.sorted()
        assertEquals(first, got[0])
        assertEquals(second, got[1])
    }

    @Test
    fun decodeSuccess() {
        val testee = ULID.fromString("01H7PN3EH10123456789ABCDEF")
        assertEquals(1691903703585L, testee.timestamp())
        val wantEntropy = listOf(
            0x00,
            0x44,
            0x32,
            0x14,
            0xc7,
            0x42,
            0x54,
            0xB6,
            0x35,
            0xcf,
        ).map { it.toByte() }
        assertEquals(wantEntropy, testee.entropy().toList())

    }

    @Test
    fun decodeMaxValue() {
        val input = "7ZZZZZZZZZZZZZZZZZZZZZZZZZ"
        val got = ULID.fromString(input).toString()

        assertEquals(input, got)
    }

    @Test
    fun decodeOverflowValue() {
        val input = "8ZZZZZZZZZZZZZZZZZZZZZZZZZ"
        assertFailsWith<IllegalArgumentException> {
            ULID.fromString(input)
        }
    }

    @Test
    fun decodeIsCaseInsensitive() {
        val expected = "01H7PN3EH10123456789ABCDEF"

        assertEquals(expected, ULID.fromString(expected.lowercase()).toString())
        assertEquals(expected, ULID.fromString("01h7Pn3eH10123456789aBcDeF").toString())
    }

    @Test
    fun decodeInvalidStringLength() {
        assertFailsWith<IllegalArgumentException> {
            ULID.fromString("1")
        }
    }

    @Test
    fun decodeStringHasInvalidCharacters() {
        assertFailsWith<IllegalArgumentException> {
            ULID.fromString("??????????????????????????")
        }
    }

    @Test
    fun binaryOrder() {
        val entropy = ByteArray(10) {
            (it + 7).toByte()
        }.let { Entropy.fromBinary(it) }
        val timestamp = Timestamp(0x010203040506)
        val testee = ULID(timestamp, entropy)
        val want = (1..16).map { it.toByte() }
        assertEquals(want, testee.binary.readByteArray().toList())
    }

    @Test
    fun binaryCanBeReadRepeatedly() {
        val testee = ULID.fromString("01H7T3YRWCWRVRSBJ24CCBTNKY")
        val expected = testee.binary.readByteArray().toList()

        assertEquals(expected, testee.binary.readByteArray().toList())
        assertEquals(expected, testee.binaryBytes.toList())
        assertEquals(testee.toUUID(), testee.toUUID())
        assertEquals(expected, testee.binary.readByteArray().toList())

        val mutableCopy = testee.binaryBytes
        mutableCopy.fill(0)
        assertEquals(expected, testee.binaryBytes.toList())
        assertEquals(expected, testee.binary.readByteArray().toList())
    }

    @Test
    fun monotonicNormal() {
        val input = ULID.fromString("01BX5ZZKBKACTAV9WEVGEMMVRY")
        val ulidGen = ULID.MonotonicGenerator(input)
        assertEquals("01BX5ZZKBKACTAV9WEVGEMMVRZ", ulidGen().toString())
        assertEquals("01BX5ZZKBKACTAV9WEVGEMMVS0", ulidGen().toString())
    }

    @Test
    fun monotonicEdgeCase() {
        val ulidGen = ULID.MonotonicGenerator(ULID.fromString("01BX5ZZKBKZZZZZZZZZZZZZZZY"))
        assertEquals("01BX5ZZKBKZZZZZZZZZZZZZZZZ", ulidGen().toString())
        assertFailsWith<IllegalStateException> {
            ulidGen().toString()
        }
    }

    @Test
    fun monotonicPreservesOrderWhenTimeRegresses() {
        val input = ULID.fromString("01BX5ZZKBKACTAV9WEVGEMMVRY")
        val ulidGen = ULID.MonotonicGenerator.withRandom(
            input,
            FakeSecureRandomGenerator(ByteArray(Entropy.BYTE_SIZE)),
        )

        val sameTimestamp = ulidGen()
        val lowerTimestamp = ulidGen(input.timestamp() - 1)

        assertEquals(input.timestamp(), sameTimestamp.timestamp())
        assertEquals(input.timestamp(), lowerTimestamp.timestamp())
        assertTrue(sameTimestamp < lowerTimestamp)
    }

    @Test
    fun monotonicReseedsAndTracksNewTimestamp() {
        val input = ULID.fromString("01BX5ZZKBKACTAV9WEVGEMMVRY")
        val newEntropy = ByteArray(Entropy.BYTE_SIZE)
        val ulidGen = ULID.MonotonicGenerator.withRandom(
            input,
            FakeSecureRandomGenerator(newEntropy),
        )

        val advanced = ulidGen(input.timestamp() + 1)
        val following = ulidGen()

        assertEquals(input.timestamp() + 1, advanced.timestamp())
        assertEquals(newEntropy.toList(), advanced.entropy().toList())
        assertEquals(advanced.timestamp(), following.timestamp())
        assertTrue(advanced < following)
    }

    @Test
    fun monotonicCanAdvanceAfterEntropyIsFull() {
        val input = ULID.fromString("01BX5ZZKBKZZZZZZZZZZZZZZZZ")
        val newEntropy = ByteArray(Entropy.BYTE_SIZE)
        val ulidGen = ULID.MonotonicGenerator.withRandom(
            input,
            FakeSecureRandomGenerator(newEntropy),
        )

        val got = ulidGen(input.timestamp() + 1)

        assertEquals(input.timestamp() + 1, got.timestamp())
        assertEquals(newEntropy.toList(), got.entropy().toList())
    }

    @Test
    fun invalidTimestampIsRejected() {
        val random = FakeSecureRandomGenerator(ByteArray(Entropy.BYTE_SIZE))

        assertFailsWith<IllegalArgumentException> {
            ULID.randomULID(timestamp = -1, random = random)
        }
        assertFailsWith<IllegalArgumentException> {
            ULID.randomULID(timestamp = 0x1000000000000, random = random)
        }
        assertFailsWith<IllegalArgumentException> {
            ULID.MonotonicGenerator(ULID.fromString("00000000000000000000000000"))(-1)
        }
    }

    @Test
    fun uuid() {
        val testUUID = Uuid.parse("0189F43F-638C-E637-8CAE-422318BD567E")
        val testULID = ULID.fromString("01H7T3YRWCWRVRSBJ24CCBTNKY")
        assertEquals(testUUID, testULID.toUUID())
        assertEquals(testULID, ULID.fromUUID(testUUID))
    }

    @Test
    fun timestamp() {
        val timestamp = Clock.System.now()
        val testULID = ULID.randomULID(timestamp = timestamp.toEpochMilliseconds())
        assertEquals(timestamp.toEpochMilliseconds(), testULID.instant().toEpochMilliseconds())
    }

    @Test
    fun serializer() {
        @Serializable
        data class TestObject(val v: ULID)

        val ulid = ULID.randomULID()
        val testee = TestObject(ulid)
        val json = """{"v":"$ulid"}"""
        assertEquals(json, Json.encodeToString(testee))
        assertEquals(testee, Json.decodeFromString<TestObject>(json))

        assertEquals("io.github.reonaore.ulidk.ULID", ULID.serializer().descriptor.serialName)
    }

    @Test
    fun equality() {
        val ulid = ULID.randomULID()
        assertTrue { ulid == ulid }
        assertFalse { ulid == ULID.randomULID() }
        assertTrue { ulid == ULID.fromString(ulid.toString()) }
        // hash
        assertTrue { ulid.hashCode() == ULID.fromString(ulid.toString()).hashCode() }
        assertFalse { ulid.hashCode() == ULID.hashCode() }
    }
}
