package com.worldsnas.faketory

import kotlin.reflect.KType

abstract class TypeReference<T> protected constructor() :
    Comparable<TypeReference<T>?> {
    val kType : KType = this::class.supertypes.first().arguments.first().type!!

    override fun compareTo(other: TypeReference<T>?): Int {
        // just need an implementation, not a good one... hence:
        return 0
    }
}