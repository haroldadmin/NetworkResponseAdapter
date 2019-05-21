package com.haroldadmin.cnradapter

import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.DescribeSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class DeferredTest : DescribeSpec() {

    private lateinit var server: MockWebServer
    private lateinit var retrofit: Retrofit
    private lateinit var service: Service
    private lateinit var executor: ExecutorService

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        server = MockWebServer()
        executor = Executors.newSingleThreadExecutor()
        retrofit = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(StringConverterFactory())
                .addCallAdapterFactory(CoroutinesNetworkResponseAdapterFactory())
                .callbackExecutor(executor)
                .build()
        service = retrofit.create(Service::class.java)
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        server.close()
        executor.shutdownNow()
    }

    init {

        describe("NetworkResponse types") {

            context("Successful response") {
                val responseBody = "Hi!"
                server.enqueue(
                        MockResponse()
                                .setBody(responseBody)
                                .setResponseCode(200)
                                .setHeader("TEST", "test")
                )
                val response = service.getText().await()

                it("Should be of type NetworkResponse.Success") {
                    (response is NetworkResponse.Success<String>) shouldBe true
                }

                it("Should have the same body content as the response") {
                    with(response as NetworkResponse.Success<String>) {
                        body shouldBe responseBody
                        headers shouldNotBe null
                        headers!!.get("TEST") shouldBe "test"
                    }
                }
            }

            context("Empty Response") {
                val responseCode = 404
                server.enqueue(
                        MockResponse()
                                .setResponseCode(responseCode)
                                .setHeader("TEST", "test")
                )
                val response = service.getText().await()

                it("Should be treated as a server error") {
                    (response is NetworkResponse.ServerError<String>) shouldBe true
                }

                it("Should have the same response code as the response") {
                    (response as NetworkResponse.ServerError<String>).code shouldBe 404
                }

                it("Should have a null body") {
                    (response as NetworkResponse.ServerError<String>).body shouldBe null
                }

                it("Should have headers") {
                    with((response as NetworkResponse.ServerError<String>)) {
                        headers shouldNotBe null
                        headers!!.get("TEST") shouldBe "test"
                    }
                }
            }

            context("IO error") {
                server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

                it("Should be treated as NetworkResponse.NetworkError") {
                    val response = service.getText().await()
                    (response is NetworkResponse.NetworkError) shouldBe true
                }
            }
        }
    }
}