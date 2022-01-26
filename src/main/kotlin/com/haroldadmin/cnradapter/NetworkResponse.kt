package com.haroldadmin.cnradapter

import okhttp3.Headers
import retrofit2.Response
import java.io.IOException

/**
 * Represents the result of a network request made using Retrofit. It can be either in a success
 * state or an error state, depending on the result of the request.
 *
 * [S] represents the deserialized body of a successful response.
 * [E] represents the deserialized body of an unsuccessful response.
 *
 * A network request is considered to be successful if it received a 2xx response code, and unsuccessful
 * otherwise.
 *
 * If the network request was successful and Retrofit successfully deserialized the body, the [NetworkResponse]
 * is [NetworkResponse.Success]. If you do not expect a successful response to contain a body, you must specify
 * [S] as [Unit] or use [CompletableResponse].
 *
 * If the network request failed due to:
 * - Non-2xx response from the server, the [NetworkResponse] is [NetworkResponse.ServerError] containing the
 * deserialized body of the response ([E])
 * - Internet connectivity problems, the [NetworkResponse] is [NetworkResponse.NetworkError]
 * - Any other problems (e.g. Serialization errors), the [NetworkResponse] is [NetworkResponse.UnknownError].
 */
public sealed interface NetworkResponse<S, E> {
    /**
     * The result of a successful network request.
     *
     * If you expect your server response to not contain a body, set the success body type ([S]) to [Unit].
     * If you expect your server response to sometimes not contain a body (e.g. for response code 204), set
     * [S] to [Unit] and deserialize the raw [response] manually when needed.
     *
     * @param body The parsed body of the successful response.
     * @param response The original [Response] from Retrofit
     */
    public data class Success<S, E>(
        public val body: S,
        public val response: Response<*>
    ) : NetworkResponse<S, E> {
        /**
         * The status code returned by the server.
         *
         * Alias for [Response.code] of the original response
         */
        public val code: Int
            get() = response.code()

        /**
         * The headers returned by the server.
         *
         * Alias for [Response.headers] of the original response
         */
        public val headers: Headers
            get() = response.headers()
    }

    /**
     * The result of a failed network request.
     *
     * A failed network request can either be due to a non-2xx response code and contain an error
     * body ([ServerError]), or due to a connectivity error ([NetworkError]), or due to an unknown
     * error ([UnknownError]).
     */
    public sealed interface Error<S, E> : NetworkResponse<S, E> {
        /**
         * The body of the failed network request, if available.
         */
        public val body: E?

        /**
         * The underlying error of the failed network request, if available.
         */
        public val error: Throwable?
    }

    /**
     * The result of a non 2xx response to a network request.
     *
     * This result may or may not contain a [body], depending on the body
     * supplied by the server.
     */
    public data class ServerError<S, E>(
        public override val body: E?,
        public val response: Response<*>?,
    ) : Error<S, E> {
        /**
         * The status code returned by the server.
         *
         * Alias for [Response.code] of the original response
         */
        public val code: Int? = response?.code()

        /**
         * The headers returned by the server.
         *
         * Alias for [Response.headers] of the original response
         */
        public val headers: Headers? = response?.headers()

        /**
         * Always `null` for a [ServerError].
         */
        override val error: Throwable? = null
    }

    /**
     * The result of a network connectivity error
     */
    public data class NetworkError<S, E>(
        public override val error: IOException,
    ) : Error<S, E> {

        /**
         * Always `null` for a [NetworkError]
         */
        override val body: E? = null
    }

    /**
     * Result of an unknown error during a network request
     * (e.g. Serialization errors)
     */
    public data class UnknownError<S, E>(
        public override val error: Throwable,
        public val response: Response<*>?
    ) : Error<S, E> {
        /**
         * Always `null` for an [UnknownError]
         */
        override val body: E? = null

        /**
         * The status code returned by the server.
         *
         * Alias for [Response.code] of the original response
         */
        public val code: Int? = response?.code()

        /**
         * The headers returned by the server.
         *
         * Alias for [Response.headers] of the original response
         */
        public val headers: Headers? = response?.headers()
    }
}

/**
 * An alias for a [NetworkResponse] with no expected response body ([Unit]).
 *
 * Useful for specifying return types of API calls that do not return a useful value.
 */
public typealias CompletableResponse<E> = NetworkResponse<Unit, E>
