package com.haroldadmin.cnradapter

import com.haroldadmin.cnradapter.utils.CompletableCall
import com.haroldadmin.cnradapter.utils.CrashyObjectConverterFactory
import com.haroldadmin.cnradapter.utils.StringConverterFactory
import com.haroldadmin.cnradapter.utils.typeOf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.IOException
import java.time.Duration

public class DeferredNetworkResponseAdapterTest : DescribeSpec({
    describe(DeferredNetworkResponseAdapter::class.java.simpleName) {
        it("should return success type correctly") {
            val converterFactory = StringConverterFactory()
            val callAdapterFactory = NetworkResponseAdapterFactory()
            val returnType = typeOf<Deferred<NetworkResponse<String, String>>>()
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(callAdapterFactory)
                .addConverterFactory(converterFactory)
                .baseUrl("http://localhost")
                .build()
            val callAdapter = callAdapterFactory.get(returnType, emptyArray(), retrofit)

            callAdapter shouldNotBe null
            callAdapter?.responseType() shouldBe String::class.java
        }

        it("should adapt a call correctly") {
            val converterFactory = StringConverterFactory()
            val callAdapterFactory = NetworkResponseAdapterFactory()
            val returnType = typeOf<Deferred<NetworkResponse<String, String>>>()
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(callAdapterFactory)
                .addConverterFactory(converterFactory)
                .baseUrl("http://localhost")
                .build()
            val retrofitCall = CompletableCall<String>()
            val callAdapter = callAdapterFactory.get(returnType, emptyArray(), retrofit)

            callAdapter shouldNotBe null
            callAdapter.shouldBeInstanceOf<CallAdapter<String, Deferred<NetworkResponse<String, String>>>>()

            val adaptedCall = callAdapter.adapt(retrofitCall)
            @Suppress("DeferredResultUnused")
            adaptedCall.shouldBeInstanceOf<Deferred<NetworkResponse<String, String>>>()
        }
    }

    describe("E2E: ${DeferredNetworkResponseAdapter::class.java.simpleName}") {
        val client = OkHttpClient.Builder().callTimeout(Duration.ofMillis(100)).build()

        val server = MockWebServer()

        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(StringConverterFactory())
            .addConverterFactory(CrashyObjectConverterFactory())
            .client(client)
            .baseUrl(server.url("/"))
            .build()

        val service = retrofit.create(DeferredNetworkResponseService::class.java)

        beforeContainer {
            @Suppress("BlockingMethodInNonBlockingContext")
            server.start()
        }

        afterContainer {
            @Suppress("BlockingMethodInNonBlockingContext")
            server.close()
        }

        it("should return successful response as NetworkResponse.Success") {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("Test Message")
                    .setHeader("Content-Type", "text/plain")
            )

            val response = service.getTextAsync().await()
            response.shouldBeInstanceOf<NetworkResponse.Success<String>>()
            response.body shouldBe "Test Message"
        }

        it("should return server error response as NetworkResponse.ServerError") {
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody("Not Found")
                    .setHeader("Content-Type", "text/plain")
            )

            val response = service.getTextAsync().await()
            response.shouldBeInstanceOf<NetworkResponse.ServerError<String>>()
            response.body shouldBe "Not Found"
        }

        it("should handle 200 (with body) and 204 (no body) responses correctly") {
            server.enqueue(MockResponse().setBody("Test Message").setResponseCode(200))
            val response = service.getTextAsync().await()
            response.shouldBeInstanceOf<NetworkResponse.OK<String>>()
            response.body shouldBe "Test Message"

            server.enqueue(MockResponse().setResponseCode(204))
            val noBodyResponse = service.getTextAsync().await()
            noBodyResponse.shouldBeInstanceOf<NetworkResponse.NoContent>()
        }

        it("should return network error response as NetworkResponse.NetworkError") {
            server.enqueue(
                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
            )

            val response = service.getTextAsync().await()
            response.shouldBeInstanceOf<NetworkResponse.NetworkError>()
            response.error.shouldBeInstanceOf<IOException>()
        }
    }
})

private interface DeferredNetworkResponseService {
    @GET("/")
    fun getTextAsync(): Deferred<NetworkResponse<String, String>>
}
