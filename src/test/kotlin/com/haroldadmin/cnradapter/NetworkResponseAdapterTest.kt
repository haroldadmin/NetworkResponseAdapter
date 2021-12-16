package com.haroldadmin.cnradapter

import com.haroldadmin.cnradapter.utils.*
import com.haroldadmin.cnradapter.utils.CrashyObject
import com.haroldadmin.cnradapter.utils.CrashyObjectConverterFactory
import com.haroldadmin.cnradapter.utils.StringConverterFactory
import com.haroldadmin.cnradapter.utils.typeOf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.IOException
import java.time.Duration

class NetworkResponseAdapterTest : DescribeSpec({
    describe(NetworkResponseAdapter::class.java.simpleName) {
        it("should return success type correctly") {
            val converterFactory = StringConverterFactory()
            val callAdapterFactory = NetworkResponseAdapterFactory()
            val returnType = typeOf<Call<NetworkResponse<String, String>>>()
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
            val returnType = typeOf<Call<NetworkResponse<String, String>>>()
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(callAdapterFactory)
                .addConverterFactory(converterFactory)
                .baseUrl("http://localhost")
                .build()
            val retrofitCall = CompletableCall<String>()
            val callAdapter = callAdapterFactory.get(returnType, emptyArray(), retrofit)

            callAdapter shouldNotBe null
            callAdapter.shouldBeInstanceOf<CallAdapter<String, Call<NetworkResponse<String, String>>>>()

            val adaptedCall = callAdapter.adapt(retrofitCall)
            adaptedCall.shouldBeInstanceOf<Call<NetworkResponse<String, String>>>()
        }
    }

    describe("E2E: ${NetworkResponseAdapter::class.java.simpleName}") {
        val client = OkHttpClient.Builder().callTimeout(Duration.ofMillis(100)).build()

        val server = MockWebServer()

        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(StringConverterFactory())
            .addConverterFactory(CrashyObjectConverterFactory())
            .client(client)
            .baseUrl(server.url("/"))
            .build()

        val service = retrofit.create(NetworkResponseAdapterService::class.java)

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

            val response = service.getText()
            response.shouldBeInstanceOf<NetworkResponse.Success<String, String>>()
            response.body shouldBe "Test Message"
        }

        it("should return server error response as NetworkResponse.ServerError") {
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody("Not Found")
                    .setHeader("Content-Type", "text/plain")
            )

            val response = service.getText()
            response.shouldBeInstanceOf<NetworkResponse.ServerError<String, String>>()
            response.body shouldBe "Not Found"
        }

        it("should handle 200 (with body) and 204 (no body) responses correctly") {
            server.enqueue(MockResponse().setBody("Test Message").setResponseCode(200))
            val response = service.getText()
            response.shouldBeInstanceOf<NetworkResponse.Success<String, String>>()
            response.body shouldBe "Test Message"

            server.enqueue(MockResponse().setResponseCode(204))
            val noBodyResponse = service.getText()
            noBodyResponse.shouldBeInstanceOf<NetworkResponse.ServerError<String, String>>()
            noBodyResponse.body shouldBe null
        }

        it("should return network error response as NetworkResponse.NetworkError") {
            server.enqueue(
                MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
            )

            val response = service.getText()
            response.shouldBeInstanceOf<NetworkResponse.NetworkError<String, String>>()
            response.error.shouldBeInstanceOf<IOException>()
        }

        it("should return NetworkResponse.UnknownError when it fails to parse the body of a successful response") {
            server.enqueue(MockResponse().setBody("Ignore").setResponseCode(200))

            val response = service.getCrashyObject()
            response.shouldBeInstanceOf<NetworkResponse.UnknownError<*, *>>()
            response.error.shouldBeInstanceOf<CrashObjectConversionError>()
        }
    }
})

private interface NetworkResponseAdapterService {
    @GET("/")
    suspend fun getText(): NetworkResponse<String, String>

    @GET("/crash")
    suspend fun getCrashyObject(): NetworkResponse<CrashyObject, String>
}
