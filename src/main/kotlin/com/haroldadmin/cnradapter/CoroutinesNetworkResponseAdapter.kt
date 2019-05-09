package com.haroldadmin.cnradapter

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.*
import java.io.IOException
import java.lang.reflect.Type

internal class CoroutinesNetworkResponseAdapter<T : Any, U : Any>(
        private val successBodyType: Type,
        private val errorConverter: Converter<ResponseBody, U>
) : CallAdapter<T, Deferred<NetworkResponse<T, U>>> {

    /**
     * This is used to determine the parameterize type of the object
     * being handled by this adapter. For example, the response type
     * in Call<Repo> is Repo.
     */
    override fun responseType(): Type = successBodyType

    override fun adapt(call: Call<T>): Deferred<NetworkResponse<T, U>> {
        val deferred = CompletableDeferred<NetworkResponse<T, U>>()

        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }

        call.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, throwable: Throwable) {
                when (throwable) {

                    is IOException -> deferred.complete(NetworkResponse.NetworkError(throwable))

                    is HttpException -> {
                        // Try to extract the error body
                        val error = throwable.response().errorBody()
                        val errorBody = when {
                            error == null -> null // No error content available
                            error.contentLength() == 0L -> null // Error content is empty
                            else -> {
                                // There is error content present, so we should try to extract it
                                try {
                                    // Try extraction
                                    errorConverter.convert(error)
                                } catch (e: Exception) {
                                    // If unable to extract content, return with a general error and don't parse further
                                    deferred.complete(
                                            NetworkResponse.NetworkError(
                                                    IOException("Couldn't deserialize error body: $error")
                                            )
                                    )
                                    return
                                }
                            }
                        }
                        // If the error extraction was successful, add it to the deferred object
                        deferred.complete(NetworkResponse.ServerError(errorBody, throwable.response().code()))
                    }

                    else -> {
                        // If there is some other kind of exception, propagate the exception
                        deferred.completeExceptionally(throwable)
                    }
                }
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                response.body()?.let {
                    deferred.complete(NetworkResponse.Success<T>(it))
                } ?: deferred.complete(NetworkResponse.ServerError(null, response.code()))
            }
        })

        return deferred
    }
}