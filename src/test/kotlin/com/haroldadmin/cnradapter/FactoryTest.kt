package com.haroldadmin.cnradapter

import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import kotlinx.coroutines.Deferred
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class FactoryTest : DescribeSpec() {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var callAdapterFactory: CoroutinesNetworkResponseAdapterFactory
    private lateinit var retrofit: Retrofit
    private lateinit var executor: ExecutorService

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        mockWebServer = MockWebServer()
        callAdapterFactory = CoroutinesNetworkResponseAdapterFactory()
        executor = Executors.newSingleThreadExecutor()
        retrofit = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(StringConverterFactory())
                .addCallAdapterFactory(callAdapterFactory)
                .callbackExecutor(executor)
                .build()
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        mockWebServer.close()
        executor.shutdownNow()
    }

    init {

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
    }
}