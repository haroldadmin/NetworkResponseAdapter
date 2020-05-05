package com.haroldadmin.cnradapter

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import java.io.IOException

internal const val UNKNOWN_ERROR_RESPONSE_CODE = 520

internal fun <E : Any> HttpException.extractFromHttpException(
        errorConverter: Converter<ResponseBody, E>
): NetworkResponse.ServerError<E> {
    val error = response()?.errorBody()
    val responseCode = response()?.code() ?: UNKNOWN_ERROR_RESPONSE_CODE
    val headers = response()?.headers()
    val errorBody = when {
        error == null -> null // No error content available
        error.contentLength() == 0L -> null // Error content is empty
        else -> try {
            // There is error content present, so we should try to extract it
            errorConverter.convert(error)
        } catch (e: Exception) {
            // If unable to extract content, return with a null body and don't parse further
            return NetworkResponse.ServerError(null, responseCode, headers)
        }
    }
    return NetworkResponse.ServerError(errorBody, responseCode, headers)
}

internal fun <S : Any, E : Any> Throwable.extractNetworkResponse(
        errorConverter: Converter<ResponseBody, E>
): NetworkResponse<S, E> {
    return when (this) {
        is IOException -> NetworkResponse.NetworkError(this)
        is HttpException -> extractFromHttpException<E>(errorConverter)
        else -> NetworkResponse.UnknownError(this)
    }
}