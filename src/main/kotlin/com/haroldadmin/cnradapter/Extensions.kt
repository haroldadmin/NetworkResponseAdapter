package com.haroldadmin.cnradapter

import kotlinx.coroutines.delay

/**
 * Retries the given [block] for the specified number of times in the case of [NetworkResponse.NetworkError]
 *
 * @param T The success body type of [NetworkResponse]
 * @param U The error body type of [NetworkResponse]
 * @param times The number of times this request should be retried
 * @param initialDelay The initial amount of time to wait before retrying
 * @param maxDelay The max amount of time to wait before retrying
 * @param factor Multiply current delay time with this on each retry
 * @param block The suspending function to be retried
 * @return The NetworkResponse value whether it be successful or failed after retrying
 */
public suspend inline fun <T : Any, U : Any> executeWithRetry(
    times: Int = 10,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000, // 1 second
    factor: Double = 2.0,
    @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
    block: suspend () -> NetworkResponse<T, U>
): NetworkResponse<T, U> {
    var currentDelay = initialDelay
    repeat(times - 1) {
        when (val response = block()) {
            is NetworkResponse.NetworkError -> {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
            else -> return response
        }
    }
    return block() // last attempt
}

/**
 * Overloaded invoke operator to get the successful body or null in NetworkResponse class
 *
 * @param T the success body type of [NetworkResponse]
 * @param U the error body type of [NetworkResponse]
 *
 * Example:
 * val usersResponse = executeWithRetry { getUsers() }
 *
 * println(usersResponse() ?: "No users found")
 */
public operator fun <T : Any, U : Any> NetworkResponse<T, U>.invoke(): T? {
    return if (this is NetworkResponse.Success) body else null
}
