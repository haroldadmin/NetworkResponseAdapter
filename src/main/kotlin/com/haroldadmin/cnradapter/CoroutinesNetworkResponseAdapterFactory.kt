package com.haroldadmin.cnradapter

import kotlinx.coroutines.Deferred
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A Factory class to create instances of [CoroutinesNetworkResponseAdapter]
 */
@Deprecated(
        message = "This class should not be used anymore. Replace with NetworkResponseAdapterFactory",
        replaceWith = ReplaceWith(
                expression = "NetworkResponseAdapterFactory",
                imports = ["com.haroldadmin.cnradapter.NetworkResponseAdapterFactory"]
        ),
        level = DeprecationLevel.WARNING
)
class CoroutinesNetworkResponseAdapterFactory private constructor() : CallAdapter.Factory() {

    companion object {
        @JvmStatic
        @JvmName("create")
        @Suppress("DEPRECATION")
        operator fun invoke(): CoroutinesNetworkResponseAdapterFactory {
            throw UnsupportedOperationException("Use NetworkResponseAdapterFactory instead of this class")
        }
    }

    /**
     * Returns the Network Response call adapter if it is appropriate, or null otherwise
     */
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val rawType = getRawType(returnType)
        if (Deferred::class.java != rawType) {
            return null
        }

        check(returnType is ParameterizedType) { "Deferred return must be parameterized as Deferred<Foo> or Deferred<out Foo>" }

        val containerType = getParameterUpperBound(0, returnType)
        if (getRawType(containerType) != NetworkResponse::class.java) {
            return null
        }

        check(containerType is ParameterizedType) { "NetworkResponse must be parameterized as NetworkResponse<SuccessBody, ErrorBody>" }

        val successBodyType = getParameterUpperBound(0, containerType)
        val errorBodyType = getParameterUpperBound(1, containerType)
        val errorBodyConverter = retrofit.nextResponseBodyConverter<Any>(
                null,
                errorBodyType,
                annotations
        )
        @Suppress("DEPRECATION")
        return CoroutinesNetworkResponseAdapter<Any, Any>(successBodyType, errorBodyConverter)
    }
}