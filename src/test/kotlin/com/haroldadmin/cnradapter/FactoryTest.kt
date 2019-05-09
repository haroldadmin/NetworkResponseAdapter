package com.haroldadmin.cnradapter

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import kotlinx.coroutines.Deferred
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit

internal class FactoryTest : DescribeSpec({

    val mockWebServer = MockWebServer()
    val callAdapterFactory = CoroutinesNetworkResponseAdapterFactory()
    val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(StringConverterFactory())
            .addCallAdapterFactory(callAdapterFactory)
            .build()

    describe("Factory") {
        context("Request type is not Deffered<NetworkResponse>") {
            val bodyClass = typeOf<Deferred<String>>()

            it("Should return null") {
                callAdapterFactory.get(bodyClass, emptyArray(), retrofit) shouldBe null
            }
        }
        context("Request type if Deferred<NetworkResponse>") {
            val bodyClass = typeOf<Deferred<NetworkResponse<String, String>>>()
            it("Should return correct type") {
                callAdapterFactory.get(bodyClass, emptyArray(), retrofit)!!.responseType() shouldBe String::class.java
            }
        }
    }
})