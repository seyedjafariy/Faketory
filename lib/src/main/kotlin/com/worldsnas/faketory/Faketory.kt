@file:OptIn(ExperimentalStdlibApi::class)

package com.worldsnas.faketory

import java.lang.reflect.Array as JavaArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random
import kotlin.random.nextUBytes
import kotlin.random.nextUInt
import kotlin.random.nextULong
import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

private val char = ','
private val byte = char.toByte()
private val uByte = byte.toUByte()

private val shortArray = shortArrayOf()
private val uShortArray = ushortArrayOf()
private val intArray = intArrayOf()
private val uIntArray = uintArrayOf()
private val longArray = longArrayOf()
private val uLongArray = ulongArrayOf()
private val doubleArray = doubleArrayOf()
private val floatArray = floatArrayOf()
private val charArray = charArrayOf()
private val booleanArray = booleanArrayOf()
private val byteArray = byteArrayOf()
private val uByteArray = ubyteArrayOf()

private val arrayList = ArrayList<Any>()
private val linkedList = LinkedList<Any>()
private val hashMap = HashMap<Any, Any>()

private val date = Date(1)
private val calendar = Calendar.getInstance().apply {
    time = date
}
private val locale = Locale.US

object Faketory {

    data class Config(
        val useDefaultConstructor: Boolean = true,
        val useDefaultValues: Boolean = true,
        val setNull: Boolean = false,
        val useSubObjectsForSealeds: Boolean = true,

        val booleanGenerator: (KParameter) -> Boolean = { rBoolean() },
        val shortGenerator: (KParameter) -> Short = { rShort() },
        val uShortGenerator: (KParameter) -> UShort = { rUShort() },
        val intGenerator: (KParameter) -> Int = { rInt() },
        val uIntGenerator: (KParameter) -> UInt = { rUInt() },
        val longGenerator: (KParameter) -> Long = { rLong() },
        val uLongGenerator: (KParameter) -> ULong = { rULong() },
        val doubleGenerator: (KParameter) -> Double = { rDouble() },
        val floatGenerator: (KParameter) -> Float = { rFloat() },
        val stringGenerator: (KParameter) -> String = { rString() },
        val charGenerator: (KParameter) -> Char = { rChar() },
        val byteGenerator: (KParameter) -> Byte = { rByte() },
        val uByteGenerator: (KParameter) -> UByte = { rUByte() },

        val byteArrayGenerator: (KParameter) -> ByteArray = { rBytes() },
        val uByteArrayGenerator: (KParameter) -> UByteArray = { rUBytes() },
        val shortArrayGenerator: (KParameter) -> ShortArray = { rShorts() },
        val uShortArrayGenerator: (KParameter) -> UShortArray = { rUShorts() },
        val intArrayGenerator: (KParameter) -> IntArray = { rInts() },
        val uIntArrayGenerator: (KParameter) -> UIntArray = { rUInts() },
        val longArrayGenerator: (KParameter) -> LongArray = { rLongs() },
        val uLongArrayGenerator: (KParameter) -> ULongArray = { rULongs() },
        val doubleArrayGenerator: (KParameter) -> DoubleArray = { rDoubles() },
        val floatArrayGenerator: (KParameter) -> FloatArray = { rFloats() },
        val charArrayGenerator: (KParameter) -> CharArray = { rChars() },
        val booleanArrayGenerator: (KParameter) -> BooleanArray = { rBooleans() },

        val arrayListGenerator: (KParameter, KType) -> ArrayList<*> = { _, _ ->
            ArrayList<Any>()
        },
        val linkedListGenerator: (KParameter, KType) -> LinkedList<*> = { _, _ ->
            LinkedList<Any>()
        },
        val hashMapGenerator: (KParameter, KType, KType) -> HashMap<*, *> = { _, _, _ ->
            HashMap<Any, Any>()
        },

        //sealed and enum
        val sealedClassResolver: (KType, Config) -> Any = ::resolveSealedClass,
        val enumClassResolver: (KType, Config) -> Any = ::resolveEnumClass,

        //special types
        val dateClassResolver: (KParameter) -> Date = { Date(System.currentTimeMillis()) },
        val calendarClassResolver: (KParameter) -> Calendar = { Calendar.getInstance() },
        val localeClassResolver: (KParameter) -> Locale = { Locale("en", "US") }
    )

    val randomFieldConfig = Config()
    val staticFieldConfig = randomFieldConfig.copy(
        booleanGenerator = { false },
        shortGenerator = { 1 },
        uShortGenerator = { 1.toUShort() },
        intGenerator = { 1 },
        uIntGenerator = { 1.toUInt() },
        longGenerator = { 1 },
        uLongGenerator = { 1.toULong() },
        doubleGenerator = { 1.0 },
        floatGenerator = { 1.0F },
        stringGenerator = { "" },
        charGenerator = { char },
        byteGenerator = { byte },
        uByteGenerator = { uByte },
        byteArrayGenerator = { byteArray },
        uByteArrayGenerator = { uByteArray },
        shortArrayGenerator = { shortArray },
        uShortArrayGenerator = { uShortArray },
        intArrayGenerator = { intArray },
        uIntArrayGenerator = { uIntArray },
        longArrayGenerator = { longArray },
        uLongArrayGenerator = { uLongArray },
        doubleArrayGenerator = { doubleArray },
        floatArrayGenerator = { floatArray },
        charArrayGenerator = { charArray },
        booleanArrayGenerator = { booleanArray },
        arrayListGenerator = { _, _ -> arrayList },
        linkedListGenerator = { _, _ -> linkedList },
        hashMapGenerator = { _, _, _ -> hashMap },
        dateClassResolver = { date },
        calendarClassResolver = { calendar },
        localeClassResolver = { locale }
    )

    var defaultConfig = staticFieldConfig

    fun <T : Any> create(reference : TypeReference<T>, configs: Config = defaultConfig) : T {
        return create(reference.kType, configs)
    }

    inline fun <reified T : Any> create(configs: Config = defaultConfig): T {
        return create(typeOf<T>(), configs)
    }

    fun <T : Any> create(type: KType, configs: Config = defaultConfig): T {
        return internalCreate(type, configs)
    }

    private fun <T : Any> internalCreate(type: KType, configs: Config): T {
        //we should check to see if the type is collection/list/map/ ...
        val clazz = type.jvmErasure
        val hasTypeArgs = type.arguments.isNotEmpty()
        val consts = clazz.constructors.toList()

        if (clazz.objectInstance != null) {
            //it's an object
            return clazz.objectInstance as T
        }

        if (clazz.isSealed) {
            return configs.sealedClassResolver(type, configs) as T
        }

        if (clazz.isSubclassOf(Enum::class)) {
            //it's an enum
            return configs.enumClassResolver(type, configs) as T
        }

        if (configs.useDefaultConstructor) {
            val creator = consts.find {
                it.parameters.all {
                    it.isOptional
                }
            }

            if (creator != null) {
                return creator.callBy(mapOf()) as T
            }
        }
        val creator = consts.first()

        return creator.callBy(
            mapOf(
                *creator
                    .parameters
                    .filter { parameter -> !(configs.useDefaultValues && parameter.isOptional) }
                    .map { parameter ->
                        if (configs.setNull && parameter.type.isMarkedNullable) {
                            parameter to null
                        } else {
                            parameter.resolveParameter(hasTypeArgs, type, configs)
                        }
                    }
                    .toTypedArray()
            )
        ) as T
    }

    private fun resolveSealedClass(type: KType, configs: Config): Any {
        val clazz = type.jvmErasure
        if (clazz.sealedSubclasses.isEmpty()) {
            //if subclasses are defined in file we reach here too
            // we might need to resolve them through classloader
            throw IllegalStateException("empty sealed")
        }
        if (configs.useSubObjectsForSealeds) {
            for (sealedSubclass in clazz.sealedSubclasses) {
                val sealedObject = sealedSubclass.objectInstance
                if (sealedObject != null) {
                    return sealedObject
                }
            }
        }

        val subClass = clazz
            .sealedSubclasses
            .find {
                it.typeParameters.isEmpty()
                        && it.objectInstance == null
            }
            ?: throw IllegalStateException("can not create sealed classes with no type parameter subsClass=$clazz")

        return internalCreate(subClass.createType(), configs)
    }

    private fun resolveEnumClass(type: KType, configs: Config): Any {
        return type.jvmErasure.java.enumConstants.first()
    }

    private fun KParameter.resolveParameter(
        hasTypeArgs: Boolean,
        classType: KType,
        configs: Config
    ): Pair<KParameter, Any> {
        var value: Any? = null

        if (hasTypeArgs && kind == KParameter.Kind.VALUE) {
            //if this type is generic we have to resolve it through parent class type args
            val index = classType.jvmErasure.typeParameters.indexOfFirst {
                it.name == type.toString()
            }

            if (index != -1) {
                value = classType.arguments[index].type!!.createType(this, configs)
            }
        }
        if (value != null) {
            return this to value
        }

        if (this.kind == KParameter.Kind.VALUE) {
            value = this.type.createType(this, configs)
        } else if (kind == KParameter.Kind.INSTANCE) {
            //this parameter is an implicit instance of the parent class (for inner class constructor)
            //we can only get the parent class type through java reflection (kotlin returns the inner class only)
            val parentType =
                classType.jvmErasure.java.constructors.first().parameters.first().type.kotlin
            //we need to reconstruct the type parameters since they are lost when converted to java Class
            val projections = classType
                .arguments
                //kotlin returns all of the types arguments (parent+inner class) in the same list
                // so we have to minus the inner class arguments to get the parent's
                .drop(classType.jvmErasure.typeParameters.size)
                .takeIf { it.isNotEmpty() }
                ?.map {
                    KTypeProjection(variance = KVariance.INVARIANT, type = it.type)
                }

            //we have to check if we need to recreate the parent class with projections
            val parentWithProjection =
                if (projections != null) {
                    parentType.createType(projections)
                } else {
                    parentType.createType()
                }

            value = parentWithProjection.createType(this, configs)
        }
        if (value != null) {
            return this to value
        }


        throw IllegalStateException(
            "parameter=$name with type=$type is not supported"
        )
    }

    private fun KType.createType(parameter: KParameter, configs: Config): Any {
        var obj: Any? = when (this) {
            shortType -> configs.shortGenerator(parameter)
            uShortType -> configs.uShortGenerator(parameter)
            intType -> configs.intGenerator(parameter)
            uIntType -> configs.uIntGenerator(parameter)
            longType -> configs.longGenerator(parameter)
            uLongType -> configs.uLongGenerator(parameter)
            doubleType -> configs.doubleGenerator(parameter)
            floatType -> configs.floatGenerator(parameter)
            stringType -> configs.stringGenerator(parameter)
            charType -> configs.charGenerator(parameter)
            booleanType -> configs.booleanGenerator(parameter)
            byteType -> configs.byteGenerator(parameter)
            uByteType -> configs.uByteGenerator(parameter)
            //primitive arrays
            shortArrayType -> configs.shortArrayGenerator(parameter)
            uShortArrayType -> configs.uShortArrayGenerator(parameter)
            intArrayType -> configs.intArrayGenerator(parameter)
            uIntArrayType -> configs.uIntArrayGenerator(parameter)
            longArrayType -> configs.longArrayGenerator(parameter)
            uLongArrayType -> configs.uLongArrayGenerator(parameter)
            doubleArrayType -> configs.doubleArrayGenerator(parameter)
            floatArrayType -> configs.floatArrayGenerator(parameter)
            charArrayType -> configs.charArrayGenerator(parameter)
            booleanArrayType -> configs.booleanArrayGenerator(parameter)
            byteArrayType -> configs.byteArrayGenerator(parameter)
            uByteArrayType -> configs.uByteArrayGenerator(parameter)

            //special types
            dateType -> configs.dateClassResolver(parameter)
            calendarType -> configs.calendarClassResolver(parameter)
            localeType -> configs.localeClassResolver(parameter)
            else -> null
        }

        if (obj != null) {
            return obj
        }

        val clazz = this.jvmErasure
        obj = when (clazz) {
            arrayListClass -> configs.arrayListGenerator(parameter, this.arguments.first().type!!)
            linkedListClass -> configs.linkedListGenerator(parameter, this.arguments.first().type!!)
            hashMapClass -> configs.hashMapGenerator(
                parameter,
                this.arguments[0].type!!,
                this.arguments[1].type!!
            )
            else -> null
        }
        if (obj != null) {
            return obj
        }

        if (clazz.constructors.isEmpty()) {
            //it's an interface
            return clazz.resolveListAndMap(parameter, arguments, configs)
        }

        //Array::class.java.isAssignableFrom() for arrays
        if (isSubtypeOf(Array<Any>::class.createType(arguments))) {
            //we can create array of Any using projection
            // but in Jvm we can not cast it to the array type we like it to
//            return arrayOf(arguments.first().type!!.jvmErasure.cast(arguments[0].type!!.createType(parameter, configs)))
            return JavaArray.newInstance(arguments[0].type!!.jvmErasure.java, 1)
        }

        return create(this, configs)
    }

    private fun KClass<*>.resolveListAndMap(
        parameter: KParameter,
        typeArguments: List<KTypeProjection>,
        configs: Config
    ): Any {
        //check for list/collection/map types
        if (this == List::class || this.isSuperclassOf(List::class)) {
            return configs.arrayListGenerator(parameter, typeArguments.first().type!!)
        }
        return when (this) {
            Map::class, MutableMap::class -> {
                configs.hashMapGenerator(
                    parameter,
                    typeArguments[0].type!!,
                    typeArguments[1].type!!
                )
            }
            Sequence::class -> {
                configs.arrayListGenerator(parameter, typeArguments.first().type!!).asSequence()
            }
            else ->
                throw IllegalStateException("generic interfaces are not supported= $this")
        }

    }
}

private fun rShort(): Short =
    Random.nextInt().toShort()

private fun rUShort(): UShort =
    Random.nextInt().toUShort()

private fun rInt(): Int =
    Random.nextInt()

private fun rUInt(): UInt =
    Random.nextUInt()

private fun rLong(): Long =
    Random.nextLong()

private fun rULong(): ULong =
    Random.nextULong()

private fun rDouble(): Double =
    Random.nextDouble()

private fun rFloat(): Float =
    Random.nextFloat()

private fun rString(): String =
    UUID.randomUUID().toString()

private fun rChar(): Char =
    rString()[0]

private fun rBoolean(): Boolean =
    Random.nextBoolean()

private fun rByte(): Byte =
    Random.nextBytes(1)[0]

private fun rUByte(): UByte =
    Random.nextBytes(1)[0].toUByte()

private fun rBytes(size: Int = 1): ByteArray =
    Random.nextBytes(size)

private fun rUBytes(size: Int = 1): UByteArray =
    Random.nextUBytes(size)

private fun rShorts(size: Int = 1, shortGenerator: ((Int) -> Short)? = null): ShortArray =
    ShortArray(size) {
        shortGenerator?.invoke(it) ?: rShort()
    }

private fun rUShorts(size: Int = 1, generator: ((Int) -> UShort)? = null): UShortArray =
    UShortArray(size) {
        generator?.invoke(it) ?: rUShort()
    }

private fun rInts(size: Int = 1, generator: ((Int) -> Int)? = null): IntArray =
    IntArray(size) {
        generator?.invoke(it) ?: rInt()
    }

private fun rUInts(size: Int = 1, generator: ((Int) -> UInt)? = null): UIntArray =
    UIntArray(size) {
        generator?.invoke(it) ?: rUInt()
    }

private fun rLongs(size: Int = 1, generator: ((Int) -> Long)? = null): LongArray =
    LongArray(size) {
        generator?.invoke(it) ?: rLong()
    }

private fun rULongs(size: Int = 1, generator: ((Int) -> ULong)? = null): ULongArray =
    ULongArray(size) {
        generator?.invoke(it) ?: rULong()
    }

private fun rDoubles(size: Int = 1, generator: ((Int) -> Double)? = null): DoubleArray =
    DoubleArray(size) {
        generator?.invoke(it) ?: rDouble()
    }

private fun rFloats(size: Int = 1, generator: ((Int) -> Float)? = null): FloatArray =
    FloatArray(size) {
        generator?.invoke(it) ?: rFloat()
    }

private fun rChars(): CharArray =
    rString().toCharArray()

private fun rBooleans(size: Int = 1, generator: ((Int) -> Boolean)? = null): BooleanArray =
    BooleanArray(size) {
        generator?.invoke(it) ?: rBoolean()
    }


private val shortType = typeOf<Short>()
private val uShortType = typeOf<UShort>()
private val intType = typeOf<Int>()
private val uIntType = typeOf<UInt>()
private val longType = typeOf<Long>()
private val uLongType = typeOf<ULong>()
private val doubleType = typeOf<Double>()
private val floatType = typeOf<Float>()
private val stringType = typeOf<String>()
private val charType = typeOf<Char>()
private val booleanType = typeOf<Boolean>()
private val byteType = typeOf<Byte>()
private val uByteType = typeOf<UByte>()

//primitive arrays
private val shortArrayType = typeOf<ShortArray>()
private val uShortArrayType = typeOf<UShortArray>()
private val intArrayType = typeOf<IntArray>()
private val uIntArrayType = typeOf<UIntArray>()
private val longArrayType = typeOf<LongArray>()
private val uLongArrayType = typeOf<ULongArray>()
private val doubleArrayType = typeOf<DoubleArray>()
private val floatArrayType = typeOf<FloatArray>()
private val charArrayType = typeOf<CharArray>()
private val booleanArrayType = typeOf<BooleanArray>()
private val byteArrayType = typeOf<ByteArray>()
private val uByteArrayType = typeOf<UByteArray>()

//collections
private val arrayListClass = ArrayList::class
private val linkedListClass = LinkedList::class
private val hashMapClass = HashMap::class

//special types
private val dateType = typeOf<Date>()
private val localeType = typeOf<Locale>()
private val calendarType = typeOf<Calendar>()
