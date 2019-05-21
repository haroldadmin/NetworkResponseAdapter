package com.haroldadmin.cnradapter

import kotlinx.coroutines.Deferred
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.IllegalStateException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A Factory class to create instances of [CoroutinesNetworkResponseAdapter]
 */
class CoroutinesNetworkResponseAdapterFactory private constructor() : CallAdapter.Factory() {

    companion object {
        @JvmStatic
        @JvmName("create")
        operator fun invoke() = CoroutinesNetworkResponseAdapterFactory()
    }

    /**
     * Returns the Network Response call adapter if it is appropriate, or null otherwise
     */
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val rawType = getRawType(returnType)
        if (Deferred::class.java != rawType) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                    "Deferred return must be parameterized as Deferred<Foo> or Deferred<out Foo>"
            )
        }

        val containerType = getParameterUpperBound(0, returnType)
        if (getRawType(containerType) != NetworkResponse::class.java) {
            return null
        }

        if (containerType !is ParameterizedType) {
            throw IllegalStateException(
                    "NetworkResponse must be parameterized as NetworkResponse<SuccessBody, ErrorBody>"
            )
        }

        val successBodyType = getParameterUpperBound(0, containerType)
        val errorBodyType = getParameterUpperBound(1, containerType)
        val errorBodyConverter = retrofit.nextResponseBodyConverter<Any>(
                null,
                errorBodyType,
                annotations
        )
        return CoroutinesNetworkResponseAdapter<Any, Any>(successBodyType, errorBodyConverter)
    }
}