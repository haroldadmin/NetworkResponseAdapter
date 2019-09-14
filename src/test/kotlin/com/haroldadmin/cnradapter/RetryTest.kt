package com.haroldadmin.cnradapter

import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.DescribeSpec
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ExtensionsTest : DescribeSpec() {

    private lateinit var server: MockWebServer
    private lateinit var retrofit: Retrofit
    private lateinit var executor: ExecutorService
    private lateinit var service: Service

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        server = MockWebServer()
        executor = Executors.newSingleThreadExecutor()
        retrofit = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .callbackExecutor(executor)
                .addCallAdapterFactory(NetworkResponseAdapterFactory())
                .addConverterFactory(StringConverterFactory())
                .build()
        service = retrofit.create(Service::class.java)
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        server.close()
        executor.shutdownNow()
    }

    init {
        describe("Execute with retry") {

            context("Deferred response") {
                repeat(9) {
                    val mockResponse = MockResponse()
                    server.enqueue(mockResponse.apply { mockResponse.socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST })
                }

                server.enqueue(MockResponse().setBody("Hi!"))

                val response = executeWithRetry(times = 10) {
                    service.getText().await()
                }

                it("Should end up with NetworkResponse.Success after 10 retries") {
                    (response is NetworkResponse.Success) shouldBe true
                    with(response as NetworkResponse.Success) {
                        body shouldBe "Hi!"
                        headers shouldNotBe null
                    }
                }
            }

            context("Suspending response") {
                repeat(9) {
                    val mockResponse = MockResponse()
                    server.enqueue(mockResponse.apply { mockResponse.socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST })
                }

                server.enqueue(MockResponse().setBody("Hi!"))

                val response = executeWithRetry(times = 10) {
                    service.getTextSuspend()
                }

                it("Should end up with NetworkResponse.Success after 10 retries") {
                    (response is NetworkResponse.Success) shouldBe true
                    with(response as NetworkResponse.Success) {
                        body shouldBe "Hi!"
                        headers shouldNotBe null
                    }
                }
            }
        }
    }
}