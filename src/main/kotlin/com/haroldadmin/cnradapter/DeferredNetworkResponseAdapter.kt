package com.haroldadmin.cnradapter

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.*
import java.lang.reflect.Type

/**
 * A Retrofit [CallAdapter] for `Deferred<NetworkResponse<S, E>>`.
 *
 * @param S The type of the successful response model
 * @param E The type of the error response model
 * @param successType Type of the successful (same as [S])
 * @param errorConverter Retrofit body converter to parse [Response.errorBody] for
 * unsuccessful responses.
 * @constructor Creates a DeferredNetworkResponseAdapter
 */
internal class DeferredNetworkResponseAdapter<S, E>(
    private val successType: Type,
    private val errorConverter: Converter<ResponseBody, E>
) : CallAdapter<S, Deferred<NetworkResponse<S, E>>> {
    override fun responseType(): Type {
        return successType
    }

    @Suppress("DeferredIsResult")
    override fun adapt(call: Call<S>): Deferred<NetworkResponse<S, E>> {
        val deferred = CompletableDeferred<NetworkResponse<S, E>>().apply {
            invokeOnCompletion {
                if (isCancelled) {
                    call.cancel()
                }
            }
        }

        call.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val networkResponse = response.asNetworkResponse(successType, errorConverter)
                deferred.complete(networkResponse)
            }

            override fun onFailure(call: Call<S>, t: Throwable) {
                val networkResponse = t.asNetworkResponse<S, E>(successType, errorConverter)
                deferred.complete(networkResponse)
            }
        })

        return deferred
    }
}
