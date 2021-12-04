package com.haroldadmin.cnradapter

import okhttp3.Headers
import retrofit2.Response
import java.io.IOException

/**
 * Represents the result of a network call made using Retrofit.
 *
 * A NetworkResponse can be either in a success state or an error state,
 * depending on the result of the network call.
 *
 * If the network call was successful, the [NetworkResponse] is [NetworkResponse.Success].
 * However, if the server responded with a 2xx status code WITHOUT a body, the success body type ([S])
 * must be set to [Unit] OR the response's status code must be 204.
 * If either of these conditions are true, then the response will contain [Unit] as the body.
 * Otherwise, the [NetworkResponse] will be [NetworkResponse.Error.ServerError].
 *
 * If the network call failed due to:
 * - Non-2xx response from the server, the [NetworkResponse] is [NetworkResponse.Error.ServerError]
 * - Internet connectivity problems, the [NetworkResponse] is [NetworkResponse.Error.NetworkError]
 * - Any other problems (e.g. Serialization errors), the [NetworkResponse] is [NetworkResponse.Error.UnknownError].
 */
public sealed interface NetworkResponse<S, E> {
    /**
     * The result of a successful network request.
     *
     * If you expect your server response to not contain a body, set the success body type ([S]) to [Unit],
     * or ensure that the response code is 204.
     *
     * @param body The parsed body of the successful response.
     * @param response The un-parsed [Response] from Retrofit
     */
    public class Success<S>(
        public val body: S,
        public val response: Response<*>
    ) : NetworkResponse<S, Nothing> {
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
     */
    public sealed interface Error<S, E> : NetworkResponse<S, E> {
        /**
         * The result of a non 2xx response to a network request
         */
        public class ServerError<E>(
            public val body: E?,
            public val response: Response<*>?,
        ) : Error<Nothing, E> {
            /**
             * The status code returned by the server.
             *
             * Alias for [Response.code] of the original response
             */
            public val code: Int?
                get() = response?.code()

            /**
             * The headers returned by the server.
             *
             * Alias for [Response.headers] of the original response
             */
            public val headers: Headers?
                get() = response?.headers()
        }

        /**
         * The result of a network connectivity error
         */
        public class NetworkError(
            public val error: IOException,
        ) : Error<Nothing, Nothing>

        /**
         * Result of an unknown error during a network request
         * (e.g. Serialization errors)
         */
        public class UnknownError(
            public val error: Throwable
        ) : Error<Nothing, Nothing>
    }
}
