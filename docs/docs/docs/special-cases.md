# Special Cases

## Handling empty response bodies

This library assumes that your server returns a parse-able response body even if the request fails. Empty bodies are treated as a server error. However, some operations rely solely on the returned response code. In such cases, the body is usually empty. Such endpoints must use `Unit` as the response type:

```kotlin
suspend fun updateStatusOnServer(): NetworkResponse<Unit, ErrorType>
```

## Handling primitive responses

The most common format for sending data over the wire is JSON. However, not all responses need JSON objects as sometimes primitive string suffice. To support a wide variety of response types, Retrofit supports adding custom converters. One such converter is the [Scalars Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/scalars) which can handle primitive response types.

To use it, use a primitive as your response type:

```kotlin
interface Api {
 @GET("/details")
 suspend fun details(): NetworkResponse<String, String>
}
```

And then make sure that the Scalars converter is added to Retrofit before the JSON converter:

```kotlin
val retrofit = Retrofit.Builder()
 .addCallAdapterFactory(NetworkResponseAdapterFactory())
 .addConverterFactory(ScalarsConverterFactory.create())
 .addConverterFactory(MoshiConverterFactory.create(moshi))
 .baseUrl("...")
 .build()
```

## Status code and Headers in `NetworkResponse.UnknownError`

Network requests that result in a `NetworkResponse.UnknownError` can still convey useful information through their headers and status code. Unfortunately, it is not always possible to extract these values from a failed request.

Therefore, the `NetworkResponse.UnknownError` class contains nullable fields for the status code and headers. These fields are populated if their values can be extract from a failed request.

```kotlin
data class UnknownError(
    val error: Throwable,
    val code: Int? = null,
    val headers: Headers? = null,
) : NetworkResponse<Nothing, Nothing>()
```

It is possible to extract this information from a failed request in most cases. However, if the server responds with a successful status code (200 <= code < 300) and an invalid body (which can not be parsed correctly), Retrofit assumes the network request failed. It forwards only the raised error and the original call to the registered call adapter, and thus all information about the response is lost resulting in a `NetworkResponse.UnknownError` with null `code` and `headers`.