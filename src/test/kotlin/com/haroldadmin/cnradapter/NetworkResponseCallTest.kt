package com.haroldadmin.cnradapter

import com.haroldadmin.cnradapter.utils.CompletableCall
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.CompletableDeferred
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.*
import java.io.IOException

class NetworkResponseCallTest : DescribeSpec({
    describe(NetworkResponseCall::class.java.simpleName) {
        it("should return a response asynchronously when using 'enqueue'") {
            val errorConverter = Converter<ResponseBody, String> { it.string() }
            val retrofitCall = CompletableCall<String>()
            val networkResponseCall = NetworkResponseCall(retrofitCall, errorConverter, String::class.java)
            val completable = CompletableDeferred<NetworkResponse<String, String>>()

            networkResponseCall.enqueue(object : Callback<NetworkResponse<String, String>> {
                override fun onResponse(
                    call: Call<NetworkResponse<String, String>>,
                    response: Response<NetworkResponse<String, String>>
                ) {
                    val body = response.body()
                    if (body == null) {
                        completable.completeExceptionally(Error("Received null body"))
                    } else {
                        completable.complete(body)
                    }
                }

                override fun onFailure(call: Call<NetworkResponse<String, String>>, t: Throwable) {
                    completable.completeExceptionally(t)
                }
            })

            retrofitCall.complete("Test Message")

            val networkResponse = completable.await()
            networkResponse.shouldBeTypeOf<NetworkResponse.Success<String>>()
            (networkResponse as NetworkResponse.Success).body shouldBe "Test Message"
        }

        it("should return a response synchronously when using `execute`") {
            val errorConverter = Converter<ResponseBody, String> { it.string() }
            val retrofitCall = CompletableCall<String>()
            val networkResponseCall = NetworkResponseCall(retrofitCall, errorConverter, String::class.java)

            retrofitCall.complete("Test Message")
            val response = networkResponseCall.execute()

            response.isSuccessful shouldBe true
            response.body().shouldBeInstanceOf<NetworkResponse.Success<String>>()

            val networkResponse = response.body() as NetworkResponse.Success
            networkResponse.body shouldBe "Test Message"
        }

        it("should cancel backing call when cancelled") {
            val errorConverter = Converter<ResponseBody, String> { it.string() }
            val retrofitCall = CompletableCall<String>()
            val networkResponseCall = NetworkResponseCall(retrofitCall, errorConverter, String::class.java)

            networkResponseCall.isCanceled shouldBe false

            networkResponseCall.cancel()
            networkResponseCall.isCanceled shouldBe true
            retrofitCall.isCanceled shouldBe true
        }

        it("should parse a successful response as NetworkResponse.Success") {
            val errorConverter = Converter<ResponseBody, String> { it.string() }
            val retrofitCall = CompletableCall<String>()
            val networkResponseCall = NetworkResponseCall(retrofitCall, errorConverter, String::class.java)

            retrofitCall.complete("Test Message")
            val networkResponse = networkResponseCall.awaitResponse().body()

            networkResponse shouldNotBe null
            networkResponse.shouldBeInstanceOf<NetworkResponse.Success<String>>()
            networkResponse.body shouldBe "Test Message"
        }

        it("should parse an HTTPException as NetworkResponse.ServerError") {
            val errorConverter = Converter<ResponseBody, String> { it.string() }
            val retrofitCall = CompletableCall<String>()
            val networkResponseCall = NetworkResponseCall(retrofitCall, errorConverter, String::class.java)

            retrofitCall.completeWithException(
                HttpException(
                    Response.error<String>(
                        404,
                        "Test Message".toResponseBody()
                    )
                )
            )
            val networkResponse = networkResponseCall.awaitResponse().body()

            networkResponse shouldNotBe null
            networkResponse.shouldBeInstanceOf<NetworkResponse.Error.ServerError<String>>()
            networkResponse.body shouldBe "Test Message"
        }

        it("should parse an IOException as NetworkResponse.NetworkError") {
            val errorConverter = Converter<ResponseBody, String> { it.string() }
            val retrofitCall = CompletableCall<String>()
            val networkResponseCall = NetworkResponseCall(retrofitCall, errorConverter, String::class.java)

            retrofitCall.completeWithException(IOException())
            val networkResponse = networkResponseCall.awaitResponse().body()

            networkResponse shouldNotBe null
            networkResponse.shouldBeInstanceOf<NetworkResponse.Error.NetworkError>()
        }

        it("should parse an unknown exception as NetworkResponse.UnknownError") {
            val errorConverter = Converter<ResponseBody, String> { it.string() }
            val retrofitCall = CompletableCall<String>()
            val networkResponseCall = NetworkResponseCall(retrofitCall, errorConverter, String::class.java)

            retrofitCall.completeWithException(Exception())
            val networkResponse = networkResponseCall.awaitResponse().body()

            networkResponse shouldNotBe null
            networkResponse.shouldBeInstanceOf<NetworkResponse.Error.UnknownError>()
        }
    }
})
