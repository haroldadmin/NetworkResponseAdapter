package com.haroldadmin.cnradapter

import okhttp3.Headers
import java.io.IOException

sealed interface NetworkResponse<out T : Any, out U : Any> {
    /**
     * A request that resulted in a response with a 2xx status code that has a body.
     */
    data class Success<T : Any>(
        val body: T,
        val headers: Headers? = null,
        val code: Int
    ) : NetworkResponse<T, Nothing>

    /**
     * Describe an error without a specific type.
     * Makes it easier to deal with the case where you just want to know that an error occurred,
     * without knowing the type
     *
     * @example
     * val response = someNetworkAction()
     *
     * when (response) {
     *    is NetworkResponse.Success -> // Action Succeeded do something with body
     *
     *    is NetworkResponse.Error -> // Action failed do something with error
     * }
     */
    sealed interface Error<out U : Any> : NetworkResponse<Nothing, U> {
        val error: Throwable
    }

    /**
     * A request that resulted in a response with a non-2xx status code.
     */
    data class ServerError<U : Any>(
        val body: U?,
        val code: Int,
        val headers: Headers? = null
    ) : Error<U> {
        override val error = IOException("Network server error: $code \n$body")
    }

    /**
     * A request that didn't result in a response.
     */
    data class NetworkError(override val error: IOException) : Error<Nothing>

    /**
     * A request that resulted in an error different from an IO or Server error.
     *
     * An example of such an error is JSON parsing exception thrown by a serialization library.
     */
    data class UnknownError(
        override val error: Throwable,
        val code: Int? = null,
        val headers: Headers? = null,
    ) : Error<Nothing>
}
