package com.haroldadmin.cnradapter

import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NetworkResponseAdapterFactory : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {

        if (returnType !is ParameterizedType) {
            // returnType must be parameterized. Raw types are not supported
            return null
        }

        val containerType = getParameterUpperBound(0, returnType)
        if (getRawType(containerType) != NetworkResponse::class.java) {
            return null
        }

        if (containerType !is ParameterizedType) {
            // containerType must be parameterized. Raw types are not supported
            return null
        }

        val (successBodyType, errorBodyType) = containerType.getBodyTypes()
        val errorBodyConverter = retrofit.nextResponseBodyConverter<Any>(null, errorBodyType, annotations)

        return when (getRawType(returnType)) {
            Deferred::class.java -> {
                DeferredNetworkResponseAdapter<Any, Any>(successBodyType, errorBodyConverter)
            }

            Call::class.java -> {
                NetworkResponseAdapter<Any, Any>(successBodyType, errorBodyConverter)
            }
            else -> null
        }
    }

    private fun ParameterizedType.getBodyTypes(): Pair<Type, Type> {
        val successType = getParameterUpperBound(0, this)
        val errorType = getParameterUpperBound(1, this)
        return successType to errorType
    }
}