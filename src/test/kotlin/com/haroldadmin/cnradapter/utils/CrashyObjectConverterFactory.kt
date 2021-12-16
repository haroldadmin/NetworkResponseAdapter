package com.haroldadmin.cnradapter.utils

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * A Retrofit converter factory that returns a converter that always
 * fails to parse a request/response body.
 *
 * Use [CrashyObject] as the return type from your Retrofit service
 * to invoke this factory.
 */
internal class CrashyObjectConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, CrashyObject>? {
        if (type !== CrashyObject::class.java) {
            return null
        }

        return Converter<ResponseBody, CrashyObject> {
            throw CrashObjectConversionError()
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        if (type !== CrashyObject::class.java) {
            return null
        }

        return Converter<CrashyObject, RequestBody> {
            throw CrashObjectConversionError()
        }
    }
}

/**
 * A marker object to invoke the [CrashyObjectConverterFactory]
 * in Retrofit.
 *
 * Example:
 * ```kt
 * interface Service {
 *   suspend fun getCrash(): CrashyObject
 * }
 * ```
 */
internal class CrashyObject

/**
 * The special error thrown by [Converter]s returned by [CrashyObjectConverterFactory]
 */
internal class CrashObjectConversionError : Error() {
    override val message: String = "Intentional error thrown by ${CrashyObjectConverterFactory::class.java.simpleName}"
}
