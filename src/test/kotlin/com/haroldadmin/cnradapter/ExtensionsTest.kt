package com.haroldadmin.cnradapter

import com.haroldadmin.cnradapter.utils.StringConverterFactory
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.IOException
import java.time.Duration

public class ExtensionsTest : DescribeSpec({
    context("Overloaded Invoke Operator") {
        it("should return the underlying body for NetworkResponse.Success") {
            val response = NetworkResponse.OK("Test Message", Response.success("Test Message"))
            val body = response()
            body shouldBe "Test Message"
        }

        it("should return null for NetworkResponse.ServerError") {
            val response = NetworkResponse.ServerError(null, null)
            val body = response()
            body shouldBe null
        }

        it("should return null for NetworkResponse.NetworkError") {
            val response = NetworkResponse.NetworkError(IOException())
            val body = response()
            body shouldBe null
        }

        it("should return null for NetworkResponse.UnknownError") {
            val response = NetworkResponse.UnknownError(Exception(), null)
            val body = response()
            body shouldBe null
        }
    }

    describe("Execute with retry") {
        val client = OkHttpClient.Builder().callTimeout(Duration.ofMillis(100)).build()

        val server = MockWebServer()

        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(StringConverterFactory())
            .client(client)
            .baseUrl(server.url("/"))
            .build()

        val service = retrofit.create(ExecuteWithRetryService::class.java)

        beforeContainer {
            @Suppress("BlockingMethodInNonBlockingContext")
            server.start()
        }

        afterContainer {
            @Suppress("BlockingMethodInNonBlockingContext")
            server.close()
        }

        it("should work correctly with deferred response") {
            repeat(9) {
                server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
            }

            server.enqueue(MockResponse().setBody("Hi!"))

            val response = executeWithRetry(times = 10, initialDelay = 100, maxDelay = 200) {
                service.getTextAsync().await()
            }

            response.shouldBeInstanceOf<NetworkResponse.Success<String>>()
            response.body shouldBe "Hi!"
        }

        it("should work correctly with suspending response") {
            repeat(9) {
                server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
            }

            server.enqueue(MockResponse().setBody("Hi!"))

            val response = executeWithRetry(times = 10, initialDelay = 100, maxDelay = 200) {
                service.getText()
            }

            response.shouldBeInstanceOf<NetworkResponse.Success<String>>()
            response.body shouldBe "Hi!"
        }
    }
})

private interface ExecuteWithRetryService {
    @GET("/")
    fun getTextAsync(): Deferred<NetworkResponse<String, String>>

    @GET("/suspend")
    suspend fun getText(): NetworkResponse<String, String>
}
