package ulidk

import io.github.reonaore.ulidk.Entropy
import io.github.reonaore.ulidk.Timestamp
import io.github.reonaore.ulidk.ULID
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
class ULIDTest : FunSpec({

    test("generate") {
        val ulid = ULID.Companion.randomULID().toString()
        val got = ULID.Companion.fromString(ulid).toString()
        ulid shouldBe got
    }

    test("sortable") {
        val first = ULID.Companion.randomULID(timestamp = 0)
        val second = ULID.Companion.randomULID(timestamp = 1)
        val testee = mutableListOf<ULID>()

        testee.add(second)
        testee.add(first)
        val got = testee.sorted()
        got.first() shouldBe first
        got[1] shouldBe second
    }
    test("decode: success") {
        val testee = ULID.Companion.fromString("01H7PN3EH10123456789ABCDEF")
        testee.timestamp() shouldBe 1691903703585L
        testee.entropy() shouldBe listOf(
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
    }

    test("decode: max value") {
        val input = "7ZZZZZZZZZZZZZZZZZZZZZZZZZ"
        val got = ULID.fromString(input).toString()

        got shouldBe input
    }
    test("decode: overflow value") {
        val input = "8ZZZZZZZZZZZZZZZZZZZZZZZZZ"
        val got = ULID.Companion.fromString(input).toString()

        got shouldBe "0ZZZZZZZZZZZZZZZZZZZZZZZZZ"
    }
    test("decode: invalid string length") {
        shouldThrow<IllegalArgumentException> {
            ULID.Companion.fromString("1")
        }
    }
    test("decode: string has invalid characters") {
        shouldThrow<IllegalArgumentException> {
            ULID.Companion.fromString("??????????????????????????")
        }
    }


    test("binary order") {
        val entropy = ByteArray(10) {
            (it + 7).toByte()
        }.let { Entropy.Companion.fromBinary(it) }
        val timestamp = Timestamp(0x010203040506)
        val testee = ULID(timestamp, entropy)
        testee.binary.readByteArray() shouldBe (1..16).map { it.toByte() }.toByteArray()
    }
    test("monotonic: normal") {
        val input = ULID.Companion.fromString("01BX5ZZKBKACTAV9WEVGEMMVRY")
        val ulidGen = ULID.MonotonicGenerator(input)
        ulidGen().toString() shouldBe "01BX5ZZKBKACTAV9WEVGEMMVRZ"
        ulidGen().toString() shouldBe "01BX5ZZKBKACTAV9WEVGEMMVS0"
    }
    test("monotinoc edge case") {
        val ulidGen = ULID.MonotonicGenerator(ULID.Companion.fromString("01BX5ZZKBKZZZZZZZZZZZZZZZY"))
        ulidGen().toString() shouldBe "01BX5ZZKBKZZZZZZZZZZZZZZZZ"
        runCatching {
            ulidGen().toString()
        }.isFailure.shouldBeTrue()
    }

    test("uuid") {
        val testUUID = Uuid.parse("0189F43F-638C-E637-8CAE-422318BD567E")
        val testULID = ULID.fromString("01H7T3YRWCWRVRSBJ24CCBTNKY")
        testULID.toUUID() shouldBe testUUID
        ULID.fromUUID(testUUID) shouldBe testULID
    }
    test("timestamp") {
        val timestamp = Clock.System.now()
        val testULID = ULID.randomULID(timestamp = timestamp.toEpochMilliseconds())
        testULID.instant().toEpochMilliseconds() shouldBe timestamp.toEpochMilliseconds()
    }

    test("serializer") {
        @Serializable
        data class TestObject(val v: ULID)

        val ulid = ULID.randomULID()
        val testee = TestObject(ulid)
        val json = """{"v":"$ulid"}"""
        Json.encodeToString(testee) shouldBe json
        Json.decodeFromString<TestObject>(json) shouldBe testee

        ULID.serializer().descriptor.serialName shouldBe "io.github.reonaore.ulidk.ULID"
    }
    test("equality") {
        val ulid = ULID.randomULID()
        (ulid == ulid) shouldBe true
        (ulid == ULID.randomULID()) shouldBe false
        (ulid == ULID.fromString(ulid.toString())).shouldBeTrue()
        // hash
        (ulid.hashCode() == ULID.fromString(ulid.toString()).hashCode()).shouldBeTrue()
        (ulid.hashCode() == ULID.hashCode()) shouldBe false
    }
})
