package com.haroldadmin.cnradapter

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SuspendTest {

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
            body shouldBe null
            headers!!["TEST"] shouldBe "test"
        }
    }

    @Test
    fun `network error test`() {
        server.enqueue(MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST })
        val response = runBlocking { service.getTextSuspend() }
        response.shouldBeTypeOf<NetworkResponse.NetworkError>()
    }

    @After
    fun cleanup() {
        server.close()
        executor.shutdown()
    }
}