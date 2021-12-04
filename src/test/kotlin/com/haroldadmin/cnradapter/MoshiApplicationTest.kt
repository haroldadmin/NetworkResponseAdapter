package com.haroldadmin.cnradapter

import com.haroldadmin.cnradapter.utils.resourceFileContents
import com.squareup.moshi.Json
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

internal data class Launch(@Json(name = "mission_name") val name: String)

internal data class LaunchInvalid(@Json(name = "misssssiioonn__nammee") val name: String)

internal data class GenericErrorResponse(val error: String)

internal data class GenericErrorResponseInvalid(@Json(name = "errrorrrr") val error: String)

@Suppress("DeferredIsResult")
internal interface LaunchesService {
    @GET("launches/{flightNumber}")
    suspend fun launchForFlightNumber(
        @Path("flightNumber") flightNumber: Long
    ): NetworkResponse<Launch, GenericErrorResponse>

    @GET("launches/{flightNumber}")
    suspend fun launchForFlightNumberInvalid(
        @Path("flightNumber") flightNumber: Long
    ): NetworkResponse<LaunchInvalid, GenericErrorResponseInvalid>

    @GET("/launches/{flightNumber}")
    fun launchForFlightNumberAsync(
        @Path("flightNumber") flightNumber: Long
    ): Deferred<NetworkResponse<Launch, GenericErrorResponse>>

    @GET("/launches/{flightNumber}")
    fun launchForFlightNumberAsyncInvalid(
        @Path("flightNumber") flightNumber: Long
    ): Deferred<NetworkResponse<LaunchInvalid, GenericErrorResponseInvalid>>

    @GET("/health")
    suspend fun healthCheck(): NetworkResponse<Unit, GenericErrorResponse>

    @GET("/health")
    fun deferredHealthCheck(): Deferred<NetworkResponse<Unit, GenericErrorResponse>>
}

internal class TestApplication(
    private val launchesService: LaunchesService
) {

    fun getLaunch(flightNumber: Long): NetworkResponse<Launch, GenericErrorResponse> = runBlocking {
        launchesService.launchForFlightNumber(flightNumber)
    }

    fun getLaunchWithFailure(
        flightNumber: Long
    ): NetworkResponse<LaunchInvalid, GenericErrorResponseInvalid> = runBlocking {
        launchesService.launchForFlightNumberInvalid(flightNumber)
    }

    fun getLaunchAsync(
        flightNumber: Long
    ): NetworkResponse<Launch, GenericErrorResponse> = runBlocking {
        launchesService.launchForFlightNumberAsync(flightNumber).await()
    }

    fun getLaunchAsyncInvalid(
        flightNumber: Long
    ): NetworkResponse<LaunchInvalid, GenericErrorResponseInvalid> = runBlocking {
        launchesService.launchForFlightNumberAsyncInvalid(flightNumber).await()
    }

    fun healthCheck(): NetworkResponse<Unit, GenericErrorResponse> = runBlocking {
        launchesService.healthCheck()
    }

    fun deferredHealthCheck(): NetworkResponse<Unit, GenericErrorResponse> = runBlocking {
        launchesService.deferredHealthCheck().await()
    }
}

class MoshiApplicationTest : DescribeSpec({
    val server = MockWebServer()

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit = Retrofit.Builder()
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(server.url("/"))
        .build()

    val service = retrofit.create(LaunchesService::class.java)

    val validFlightNumber = 1L
    val invalidFlightNumber = -1L

    beforeContainer {
        server.start()
    }

    afterContainer {
        server.close()
    }

    it("should parse success response successfully") {
        val app = TestApplication(service)
        server.enqueue(
            MockResponse().apply {
                setBody(resourceFileContents("/falconsat_launch.json"))
                setResponseCode(200)
            }
        )
        val response = app.getLaunch(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Success<Launch>>()
        response.body.name shouldContain "FalconSat"
        response.code shouldBe 200
    }

    it("should parse error response successfully") {
        val app = TestApplication(service)
        server.enqueue(
            MockResponse().apply {
                setBody(resourceFileContents("/error_response.json"))
                setResponseCode(404)
            }
        )
        val response = app.getLaunch(invalidFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Error.ServerError<GenericErrorResponse>>()
        response.body!!.error shouldContain "Not Found"
        response.code shouldBe 404
    }

    it("should fail on parsing exceptions of success response") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setBody(resourceFileContents("/falconsat_launch.json"))
                setResponseCode(200)
                setHeader("test", "true")
            }
        )

        val response = app.getLaunchWithFailure(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Error.UnknownError>()
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    it("should parse response code and headers of unsuccessful request with invalid body correctly") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setBody("""{ "message": "Too many requests!" }""")
                setResponseCode(429)
                setHeader("test", "true")
            }
        )
        val response = app.getLaunchWithFailure(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Error.UnknownError>()
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    it("should fail on parsing exceptions of ErrorResponse") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setBody(resourceFileContents("/error_response.json"))
                setResponseCode(404)
            }
        )

        val response = app.getLaunchWithFailure(invalidFlightNumber)
        response.shouldBeInstanceOf<NetworkResponse.Error.UnknownError>()
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    it("should parse successful response successfully when using deferred") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setBody(resourceFileContents("/falconsat_launch.json"))
                setResponseCode(200)
            }
        )

        val response = app.getLaunchAsync(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Success<Launch>>()
        response.body.name shouldContain "FalconSat"
        response.code shouldBe 200
    }

    it("should parse error response successfully when using deferred") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setBody(resourceFileContents("/error_response.json"))
                setResponseCode(404)
            }
        )

        val response = app.getLaunchAsync(invalidFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Error.ServerError<GenericErrorResponse>>()
        response.body?.error shouldContain "Not Found"
        response.code shouldBe 404
    }

    it("should convert parsing errors for success response to NetworkResponse-UnknownError when using deferred") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setBody(resourceFileContents("/falconsat_launch.json"))
                setResponseCode(200)
            }
        )

        val response = app.getLaunchAsyncInvalid(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Error.UnknownError>()
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    it("should convert parsing errors for error response to NetworkResponse-UnknownError when using deferred") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setBody(resourceFileContents("/error_response.json"))
                setResponseCode(200)
            }
        )

        val response = app.getLaunchAsyncInvalid(invalidFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Error.UnknownError>()
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    it("should parse empty body as Unit") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setResponseCode(204)
            }
        )

        val response = app.healthCheck()

        response.shouldBeInstanceOf<NetworkResponse.Success<Unit>>()
        response.body shouldBe Unit
        response.code shouldBe 204
    }

    it("should parse empty body as Unit for deferred methods too") {
        val app = TestApplication(service)

        server.enqueue(
            MockResponse().apply {
                setResponseCode(204)
            }
        )

        val response = app.deferredHealthCheck()

        response.shouldBeInstanceOf<NetworkResponse.Success<Unit>>()
        response.body shouldBe Unit
        response.code shouldBe 204
    }
})
