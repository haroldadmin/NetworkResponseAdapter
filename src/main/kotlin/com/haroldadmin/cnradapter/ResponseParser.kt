package com.haroldadmin.cnradapter

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

/**
 * Maps a [Response] to a [NetworkResponse].
 *
 *
 * @param errorConverter Retrofit provided body converter to parse the error body of the response
 * @return A subtype of [NetworkResponse] based on the response of the network request
 */
internal fun <S, E> Response<S>.asNetworkResponse(
    successType: Type,
    errorConverter: Converter<ResponseBody, E>
): NetworkResponse<S, E> {
    return if (!isSuccessful) {
        parseUnsuccessfulResponse(this, errorConverter)
    } else {
        parseSuccessfulResponse(this)
    }
}

/**
 * Maps an unsuccessful [Response] to [NetworkResponse.Error].
 *
 * Control flow:
 * 1 Try to parse the error body using [errorConverter].
 * 2. If error body is parsed successfully, return it as [NetworkResponse.ServerError]
 * 3 Otherwise, assume we ran into an unknown error (probably related to serialization)
 * and return [NetworkResponse.UnknownError]
 *
 * @param response Unsuccessful response
 * @param errorConverter Retrofit [Converter] to parse the error body
 * @return A subtype of [NetworkResponse.Error]
 */
private fun <S, E> parseUnsuccessfulResponse(
    response: Response<S>,
    errorConverter: Converter<ResponseBody, E>
): NetworkResponse.Error<E> {
    val errorBody: ResponseBody =
        response.errorBody() ?: return NetworkResponse.ServerError(null, response)

    return try {
        val convertedBody = errorConverter.convert(errorBody)
        NetworkResponse.ServerError(convertedBody, response)
    } catch (error: Throwable) {
        NetworkResponse.UnknownError(error, response)
    }
}

/**
 * Maps a successful [Response] to [NetworkResponse]
 *
 * Control flow:
 *
 * - If [response] body is null:
 *      - If [successType] is [Unit], return [NetworkResponse.Success] with [Unit] as the body
 *      - Else return a [NetworkResponse.ServerError] with a null body
 * - If response body is not null, return [NetworkResponse.Success] with the parsed body
 */
private fun <S, E> parseSuccessfulResponse(response: Response<S>): NetworkResponse<S, E> {
    return when (val responseBody: S? = response.body()) {
        null -> when (response.code()) {
            204 -> NetworkResponse.NoContent(response)
            else -> NetworkResponse.ServerError(null, response)
        }
        else -> NetworkResponse.OK(responseBody, response)
    }
}

/**
 * Maps a [Throwable] to a [NetworkResponse].
 *
 * - If the error is [IOException], return [NetworkResponse.NetworkError].
 * - If the error is [HttpException], attempt to parse the underlying response and return the result
 * - Else return [NetworkResponse.UnknownError] that wraps the original error
 */
internal fun <S, E> Throwable.asNetworkResponse(
    successType: Type,
    errorConverter: Converter<ResponseBody, E>,
): NetworkResponse<S, E> {
    return when (this) {
        is IOException -> NetworkResponse.NetworkError(this)
        is HttpException -> {
            val response = response()
            if (response == null) {
                NetworkResponse.ServerError(null, null)
            } else {
                @Suppress("UNCHECKED_CAST")
                response.asNetworkResponse(successType, errorConverter) as NetworkResponse<S, E>
            }
        }
        else -> NetworkResponse.UnknownError(this, null)
    }
}
