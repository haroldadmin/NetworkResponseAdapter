package com.haroldadmin.cnradapter

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

internal const val STATUS_NO_CONTENT = 204

/**
 * Maps a [Response] to a [NetworkResponse].
 *
 * A [Response] can either be successful or unsuccessful.
 *
 * For unsuccessful responses:
 * - Try to parse the error body using [errorConverter].
 * - If error body is parsed successfully, return it as [NetworkResponse.Error.ServerError]
 * - Otherwise, assume we ran into an unknown error (probably related to serialization)
 * and return [NetworkResponse.Error.UnknownError]
 *
 * For successful responses:
 *
 * If response body is null:
 * - If [successType] is [Unit], return [NetworkResponse.Success] with [Unit] as the body
 * - If [Response.code] is [STATUS_NO_CONTENT], return [NetworkResponse.Success] with [Unit] as the body
 * - Else return a [NetworkResponse.Error.ServerError] with a null body
 *
 * If response body is not null:
 * - Return [NetworkResponse.Success] with the parsed body
 *
 * @param errorConverter Retrofit provided body converter to parse the error body of the response
 * @return A subtype of [NetworkResponse] based on the response of the network request
 */
@Suppress("UNCHECKED_CAST")
internal fun <S, E> Response<S>.toNetworkResponse(
    successType: Type,
    errorConverter: Converter<ResponseBody, E>
): NetworkResponse<S, E> {

    if (!isSuccessful) {
        val errorBody: ResponseBody? = errorBody()
        @Suppress("FoldInitializerAndIfToElvis")
        if (errorBody == null) {
            return NetworkResponse.Error.ServerError(null, this)
        }

        return try {
            val convertedBody = errorConverter.convert(errorBody)
            NetworkResponse.Error.ServerError(convertedBody, this)
        } catch (error: Throwable) {
            NetworkResponse.Error.UnknownError(error)
        }
    }

    val responseBody: S? = body()
    if (responseBody == null) {
        if (successType === Unit::class.java) {
            return NetworkResponse.Success<Unit, E>(Unit, this) as NetworkResponse<S, E>
        }

        if (code() == STATUS_NO_CONTENT) {
            return NetworkResponse.Success<Unit, E>(Unit, this) as NetworkResponse<S, E>
        }

        return NetworkResponse.Error.ServerError(null, this)
    }

    return NetworkResponse.Success(responseBody, this)
}

/**
 * Maps a [Throwable] to a [NetworkResponse].
 *
 * - If the error is [IOException], return [NetworkResponse.Error.NetworkError].
 * - If the error is [HttpException], attempt to parse the underlying response and return the result
 * - Else return [NetworkResponse.Error.UnknownError] that wraps the original error
 */
@Suppress("UNCHECKED_CAST")
internal fun <S, E> Throwable.toNetworkResponse(
    successType: Type,
    errorConverter: Converter<ResponseBody, E>,
): NetworkResponse<S, E> {
    return when (this) {
        is IOException -> NetworkResponse.Error.NetworkError(this)
        is HttpException -> {
            val response = response()
            @Suppress("SENSELESS_COMPARISON")
            if (response == null) {
                NetworkResponse.Error.ServerError(null, null)
            } else {
                response.toNetworkResponse(successType, errorConverter) as NetworkResponse<S, E>
            }
        }
        else -> NetworkResponse.Error.UnknownError(this)
    }
}
