package com.haroldadmin.cnradapter

import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

internal class NetworkResponseCall<S : Any, E : Any>(
        private val backingCall: Call<S>,
        private val errorConverter: Converter<ResponseBody, E>
) : Call<NetworkResponse<S, E>> {

    override fun enqueue(callback: Callback<NetworkResponse<S, E>>) = synchronized(this) {
        backingCall.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val body = response.body()
                if (body != null) {
                    callback.onResponse(this@NetworkResponseCall, Response.success(NetworkResponse.Success(body, response.headers())))
                } else {
                    callback.onResponse(this@NetworkResponseCall, Response.success(NetworkResponse.ServerError(null, response.code(), response.headers())))
                }
            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {
                val networkResponse = throwable.extractNetworkResponse<S, E>(errorConverter)
                callback.onResponse(this@NetworkResponseCall, Response.success(networkResponse))
            }
        })
    }

    override fun isExecuted(): Boolean = synchronized(this) {
        backingCall.isExecuted
    }

    override fun clone(): Call<NetworkResponse<S, E>> = NetworkResponseCall(backingCall.clone(), errorConverter)

    override fun isCanceled(): Boolean = synchronized(this) {
        backingCall.isCanceled
    }

    override fun cancel() = synchronized(this) {
        backingCall.cancel()
    }

    override fun execute(): Response<NetworkResponse<S, E>> {
        throw UnsupportedOperationException("Network Response call does not support synchronous execution")
    }

    override fun request(): Request = backingCall.request()
}