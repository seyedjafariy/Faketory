package com.worldsnas.faketory

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

class FaketoryTest {

    @Before
    fun setUp() {
        Faketory.defaultConfig = Faketory.defaultConfig.copy(
            useDefaultConstructor = false,
            setNull = false,
            useSubObjectsForSealeds = false
        )
    }

    @Test
    fun `creates a class with primitives`() {
        val expected = ClassWithPrimitives()

        val actual = Faketory.create<ClassWithPrimitives>()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `creates a class with default constructor`() {
        val expected = ClassWithDefault()

        val actual = Faketory.create<ClassWithDefault>(
            Faketory.defaultConfig.copy(useDefaultConstructor = true)
        )

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `creates class with Date`() {
        val expected = ClassWithDate(Date(1))

        val actual = Faketory.create<ClassWithDate>()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `creates class with Locale`() {
        val expected = ClassWithLocale(Locale.US)

        val actual = Faketory.create<ClassWithLocale>()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `creates class with Calendar`() {
        val calendar = Calendar.getInstance().apply {
            time = Date(1)
        }
        val expected = ClassWithCalendar(calendar)

        val actual = Faketory.create<ClassWithCalendar>()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `creates class with type parameters`() {
        val expected = ClassWithTypeParameter<Short, Int, String>(1, "")

        val actual = Faketory.create<ClassWithTypeParameter<Short, Int, String>>()

        assertThat(actual).isEqualTo(expected)

    }

    @Test
    fun `creates parameterized class  with java TypeReference`() {
        val expected = ClassWithTypeParameter<Short, Int, String>(1, "")

        val reference = object : TypeReference<ClassWithTypeParameter<Short, Int, String>>() {}


        val actual = Faketory.create(reference)

        assertThat(actual).isEqualTo(expected)

    }

}

@Suppress("ArrayInDataClass")
data class ClassWithPrimitives(
    val a: Int = 1,
    val b: Long = 1,
    val c: Double = 1.0,
    val d: Float = 1.0F,
    val e: Short = 1,
    val f: Char = ',',
    val g: Boolean = false,
    val h: Byte = ','.toByte(),
    val q: UByte = ','.toByte().toUByte(),
    val u: UInt = 1.toUInt(),
    val v: ULong = 1L.toULong(),
    val w: UShort = 1.toUShort(),
    val x: Short = 1.toShort(),
    val z: String = ""
)

class ClassWithDefault {
    val a: Int

    constructor(block: () -> Int = { 1 }) {
        a = block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassWithDefault

        if (a != other.a) return false

        return true
    }

    override fun hashCode(): Int {
        return a
    }


}

data class ClassWithTypeParameter<T, K, B>(
    val k: K,
    val b: B
)

data class ClassWithDate(
    val date: Date
)

data class ClassWithLocale(
    val date: Locale
)

data class ClassWithCalendar(
    val date: Calendar
)
