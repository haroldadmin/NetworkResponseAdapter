package com.haroldadmin.cnradapter

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class SuspendTest: AnnotationSpec() {

    private lateinit var server: MockWebServer
    private lateinit var executor: ExecutorService
    private lateinit var retrofit: Retrofit
    private lateinit var service: Service

    @Before
    fun setup() {
        server = MockWebServer()
        executor = Executors.newSingleThreadExecutor()
        retrofit = Retrofit.Builder()
                .baseUrl(server.url("/suspend/"))
                .addConverterFactory(StringConverterFactory())
                .addCallAdapterFactory(NetworkResponseAdapterFactory())
                .callbackExecutor(executor)
                .build()
        service = retrofit.create(Service::class.java)
    }

    @Test
    fun `successful response test`() {
        val responseBody = "Hi!"
        server.enqueue(
                MockResponse()
                        .setBody(responseBody)
                        .setResponseCode(200)
                        .setHeader("TEST","test")
        )

        val response = runBlocking {
            service.getTextSuspend()
        }

        with(response) {
            shouldBeTypeOf<NetworkResponse.Success<String>>()
            this as NetworkResponse.Success
            body shouldBe responseBody
            headers shouldNotBe null
            headers!!["TEST"] shouldBe "test"
            code shouldBe 200
        }
    }

    @Test
    fun `empty response test`() {
        val responseCode = 404
        server.enqueue(
                MockResponse()
                        .setResponseCode(responseCode)
                        .setHeader("TEST", "test")
        )
        val response = runBlocking { service.getTextSuspend() }

        with (response) {
            shouldBeTypeOf<NetworkResponse.ServerError<String>>()
            this as NetworkResponse.ServerError
            code shouldBe responseCode
            body.isNullOrBlank() shouldBe true
            headers!!["TEST"] shouldBe "test"
        }
    }

    @Test
    fun `network error test`() {
        server.enqueue(MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST })
        val response = runBlocking { service.getTextSuspend() }
        response.shouldBeTypeOf<NetworkResponse.NetworkError>()
    }

    @Test
    fun `successful response with empty body`() {
        val successResponseCode = 204
        server.enqueue(MockResponse().apply {
            setResponseCode(successResponseCode)
        })

        val response = runBlocking { service.getEmptyBodySuspend() }

        with(response) {
            shouldBeTypeOf<NetworkResponse.Success<Unit>>()
            this as NetworkResponse.Success
            body shouldBe Unit
            code shouldBe 204
        }
    }

    @Test
    fun `unsuccessful response with empty body`() {
        val successResponseCode = 400
        server.enqueue(MockResponse().apply {
            setResponseCode(successResponseCode)
        })

        val response = runBlocking { service.getEmptyBodySuspend() }

        with(response) {
            shouldBeTypeOf<NetworkResponse.ServerError<String>>()
            this as NetworkResponse.ServerError
            body shouldBe ""
            code shouldBe 400
        }
    }

    @After
    fun cleanup() {
        server.close()
        executor.shutdown()
    }
}