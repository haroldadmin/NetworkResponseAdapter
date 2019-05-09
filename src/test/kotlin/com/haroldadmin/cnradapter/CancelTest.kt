package com.haroldadmin.cnradapter

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import kotlinx.coroutines.Deferred
import okhttp3.mockwebserver.MockWebServer
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.io.IOException

internal class CancelTest : DescribeSpec({

    val mockWebServer = MockWebServer()
    val factory = CoroutinesNetworkResponseAdapterFactory()
    val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(StringConverterFactory())
            .addCallAdapterFactory(factory)
            .build()

    describe("Cancellation") {
        val deferredStringType = typeOf<Deferred<NetworkResponse<String, String>>>()
        val adapter = factory.get(deferredStringType, emptyArray(), retrofit)!! as CallAdapter<String, Deferred<NetworkResponse<String, String>>>

        context("Successfull call") {
            val call = CompletableCall<String>()
            val deferred = adapter.adapt(call)
            call.complete("Hey")

            it("Should not be cancelled when response has been received") {
                call.isCanceled shouldBe false
            }

            it("Should have deferred value completed") {
                deferred.isCompleted shouldBe true
            }
        }

        context("Call with error") {
            val call = CompletableCall<String>()
            val deferred = adapter.adapt(call)
            call.completeWithException(IOException())

            it ("Should be cancelled") {
                call.isCanceled shouldBe false
            }
        }

        context("Cancelled call") {
            val call = CompletableCall<String>()
            val deferred = adapter.adapt(call)

            it("Should not be cancelled before cancellation") {
                call.isCanceled shouldBe false
            }

            it("Should be cancelled after cancellation") {
                deferred.cancel()
                call.isCanceled shouldBe true
            }
        }
    }

})