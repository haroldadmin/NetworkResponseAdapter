package com.haroldadmin.cnradapter

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.*
import java.io.IOException
import java.lang.reflect.Type

/**
 * A Retrofit converter to return objects wrapped in [NetworkResponse] class
 *
 * @param T The type of the successful response model
 * @param U The type of the error response model
 * @param successBodyType The type of the successful response model in [NetworkResponse]
 * @param errorConverter The converter to extract error information from [ResponseBody]
 * @constructor Creates a CoroutinesNetworkResponseAdapter
 */

@Deprecated(
        message = "This class should not be used anymore. Pick DeferredNetworkResponseAdapter or NetworkResponseAdapter based on your needs",
        replaceWith = ReplaceWith(
                expression = "DeferredNetworkResponseAdapter",
                imports = ["com.haroldadmin.cnradapter.DeferredNetworkResponseAdapter"]
        ),
        level = DeprecationLevel.WARNING
)
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

    /**
     * Returns an instance of [T] by modifying a [Call] object
     *
     * @param call The call object to be converted
     * @return The T instance wrapped in a [NetworkResponse] class wrapped in [Deferred]
     */
    override fun adapt(call: Call<T>): Deferred<NetworkResponse<T, U>> {
        val deferred = CompletableDeferred<NetworkResponse<T, U>>()

        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }

        call.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, throwable: Throwable) {
                // TODO Use ErrorExtraction methods here
                when (throwable) {

                    is IOException -> deferred.complete(NetworkResponse.NetworkError(throwable))

                    is HttpException -> {
                        // Try to extract the error body
                        val error = throwable.response()?.errorBody()
                        val responseCode = throwable.response()?.code() ?: UNKNOWN_ERROR_RESPONSE_CODE
                        val headers = throwable.response()?.headers()
                        val errorBody = when {
                            error == null -> null // No error content available
                            error.contentLength() == 0L -> null // Error content is empty
                            else -> try {
                                // There is error content present, so we should try to extract it
                                errorConverter.convert(error)
                            } catch (e: Exception) {
                                // If unable to extract content, return with a null body and don't parse further
                                deferred.complete(
                                        NetworkResponse.ServerError(null, responseCode, headers)
                                )
                                return
                            }
                        }
                        // If the error extraction was successful, add it to the deferred object
                        deferred.complete(NetworkResponse.ServerError(errorBody, responseCode, headers))
                    }

                    else -> {
                        // If there is some other kind of exception, propagate the exception
                        deferred.completeExceptionally(throwable)
                    }
                }
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                val headers = response.headers()
                val responseCode = response.code()
                val body = response.body()
                body?.let {
                    deferred.complete(NetworkResponse.Success(it, headers, responseCode))
                } ?: deferred.complete(NetworkResponse.ServerError(null, responseCode, headers))
            }
        })

        return deferred
    }
}