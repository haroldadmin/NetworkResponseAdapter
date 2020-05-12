package com.haroldadmin.cnradapter

import com.squareup.moshi.Json
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
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

internal class MoshiApplicationTest: AnnotationSpec() {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val server = MockWebServer()

    private val retrofit = Retrofit.Builder()
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(server.url("/"))
        .build()

    private val service = retrofit.create(LaunchesService::class.java)

    private val validFlightNumber = 1L
    private val invalidFlightNumber = -1L

    @Test
    fun shouldParseSuccessResponseSuccessfully() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setBody(resourceFileContents("/falconsat_launch.json"))
            setResponseCode(200)
        })
        val response = app.getLaunch(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Success<Launch>>()
        response as NetworkResponse.Success
        response.body.name shouldContain "FalconSat"
    }

    @Test
    fun shouldParseErrorResponseSuccessfully() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setBody(resourceFileContents("/error_response.json"))
            setResponseCode(404)
        })
        val response = app.getLaunch(invalidFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.ServerError<GenericErrorResponse>>()
        response as NetworkResponse.ServerError
        response.body!!.error shouldContain "Not Found"
    }

    @Test
    fun shouldFailOnParsingExceptionsOfSuccessResponse() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setBody(resourceFileContents("/falconsat_launch.json"))
            setResponseCode(200)
        })
        val response = app.getLaunchWithFailure(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.UnknownError>()
        response as NetworkResponse.UnknownError
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    @Test
    fun shouldFailOnParsingExceptionsOfErrorResponse() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setBody(resourceFileContents("/error_response.json"))
            setResponseCode(404)
        })
        val response = app.getLaunchWithFailure(invalidFlightNumber)
        response.shouldBeInstanceOf<NetworkResponse.UnknownError>()
        response as NetworkResponse.UnknownError
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    @Test
    fun `should parse successful response successfully when using deferred`() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setBody(resourceFileContents("/falconsat_launch.json"))
            setResponseCode(200)
        })
        val response = app.getLaunchAsync(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.Success<Launch>>()
        response as NetworkResponse.Success
        response.body.name shouldContain "FalconSat"
    }

    @Test
    fun `should parse error response successfully when using deferred`() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setBody(resourceFileContents("/error_response.json"))
            setResponseCode(404)
        })
        val response = app.getLaunchAsync(invalidFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.ServerError<GenericErrorResponse>>()
        response as NetworkResponse.ServerError
        response.body!!.error shouldContain "Not Found"
    }

    @Test
    fun `should convert parsing errors for success response to NetworkResponse-UnknownError when using deferred`() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setBody(resourceFileContents("/falconsat_launch.json"))
            setResponseCode(200)
        })
        val response = app.getLaunchAsyncInvalid(validFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.UnknownError>()
        response as NetworkResponse.UnknownError
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    @Test
    fun `should convert parsing errors for error response to NetworkResponse-UnknownError when using deferred`() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setBody(resourceFileContents("/error_response.json"))
            setResponseCode(200)
        })
        val response = app.getLaunchAsyncInvalid(invalidFlightNumber)

        response.shouldBeInstanceOf<NetworkResponse.UnknownError>()
        response as NetworkResponse.UnknownError
        response.error.shouldBeInstanceOf<JsonDataException>()
    }

    @Test
    fun `should parse empty body as Unit`() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setResponseCode(204)
        })
        val response = app.healthCheck()

        response.shouldBeInstanceOf<NetworkResponse.Success<Unit>>()
        response as NetworkResponse.Success
        response.body shouldBe Unit
    }

    @Test
    fun `should parse empty body as Unit for deferred methods too`() {
        val app = TestApplication(service)
        server.enqueue(MockResponse().apply {
            setResponseCode(204)
        })
        val response = app.deferredHealthCheck()

        response.shouldBeInstanceOf<NetworkResponse.Success<Unit>>()
        response as NetworkResponse.Success
        response.body shouldBe Unit
    }
}