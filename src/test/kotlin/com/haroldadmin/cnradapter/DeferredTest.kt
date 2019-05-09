package com.haroldadmin.cnradapter

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import kotlinx.coroutines.Deferred
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.Retrofit
import retrofit2.http.GET

private interface Service {
    @GET("/")
    fun getText(): Deferred<NetworkResponse<String, String>>
}

internal class DeferredTest : DescribeSpec({
    val server = MockWebServer()
    val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(StringConverterFactory())
            .addCallAdapterFactory(CoroutinesNetworkResponseAdapterFactory())
            .build()
    val service = retrofit.create(Service::class.java)

    describe("NetworkResponse types") {

        context("Successful response") {
            val responseBody = "Hi!"
            server.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))
            val response = service.getText().await()

            it("Should be of type NetworkResponse.Success") {
                (response is NetworkResponse.Success<String>) shouldBe true
            }

            it("Should have the same body content as the response") {
                (response as NetworkResponse.Success<String>).body shouldBe responseBody
            }
        }

        context("Empty Response") {
            val responseCode = 404
            server.enqueue(MockResponse().setResponseCode(responseCode))
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
        }

        context("IO error") {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

            it("Should be treated as NetworkResponse.NetworkError") {
                val response = service.getText().await()
                (response is NetworkResponse.NetworkError) shouldBe true
            }
        }
    }
})