package com.haroldadmin.cnradapter

import com.haroldadmin.cnradapter.utils.CompletableCall
import com.haroldadmin.cnradapter.utils.StringConverterFactory
import com.haroldadmin.cnradapter.utils.typeOf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.IOException

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
        val server = MockWebServer()
        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(StringConverterFactory())
            .baseUrl(server.url("/"))
            .build()
        val service = retrofit.create(NetworkResponseAdapterService::class.java)

        beforeContainer {
            server.start()
        }

        afterContainer {
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
            response.shouldBeInstanceOf<NetworkResponse.Success<String>>()
            response.body shouldBe "Test Message"
        }

        it("should return server error response as NetworkResponse.Error.ServerError") {
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody("Not Found")
                    .setHeader("Content-Type", "text/plain")
            )

            val response = service.getText()
            response.shouldBeInstanceOf<NetworkResponse.Error.ServerError<String>>()
            response.body shouldBe "Not Found"
        }

        it("should return network error response as NetworkResponse.Error.NetworkError") {
            server.enqueue(
                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
            )

            val response = service.getText()
            response.shouldBeInstanceOf<NetworkResponse.Error.NetworkError>()
            response.error.shouldBeInstanceOf<IOException>()
        }
    }
})

private interface NetworkResponseAdapterService {
    @GET("/")
    suspend fun getText(): NetworkResponse<String, String>
}
