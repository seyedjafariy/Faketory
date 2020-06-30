package com.worldsnas.faketory

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

//import org.junit.Assert.*

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
    fun `successfully creates a class with primitives`() {
        val expected = ClassWithPrimitives()

        val actual = Faketory.create<ClassWithPrimitives>()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `successfully creates a class with default constructor`() {
        val expected = ClassWithDefault()

        val actual = Faketory.create<ClassWithDefault>(
            Faketory.defaultConfig.copy(useDefaultConstructor = true)
        )

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `creates class with primitive arrays`(){
        val expected = ClassWithPrimitiveArrays()

        val actual = Faketory.create<ClassWithPrimitiveArrays>()

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

data class ClassWithPrimitiveArrays(
    val i: ByteArray = byteArrayOf(),
    val j: IntArray = intArrayOf(),
    val k: ShortArray = shortArrayOf(),
    val l: LongArray = longArrayOf(),
    val m: DoubleArray = doubleArrayOf(),
    val n: FloatArray = floatArrayOf(),
    val o: CharArray = charArrayOf(),
    val p: BooleanArray = booleanArrayOf(),
    val r: UByteArray = ubyteArrayOf(),
    val s: UIntArray = uintArrayOf(),
    val t: ULongArray = ulongArrayOf(),
    val y: UShortArray = ushortArrayOf()
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
