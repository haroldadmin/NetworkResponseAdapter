# Special Cases

## Handling empty response bodies

Some operations rely solely on the returned response code. In such cases, the body is usually empty. Use `Unit` as the response type for such APIs:

```kotlin
suspend fun updateStatusOnServer(): NetworkResponse<Unit, ErrorType>
```

If your server sometimes returns a body and sometimes doesn't (200 vs 204 status code), then consider using the bundled raw Retrofit response.

```kotlin
interface PostsService {
  @GET("/")
  suspend fun getPost(): NetworkResponse<Unit, ErrorResponse>
}

when (val postResponse  = service.getPost()) {
  is NetworkResponse.Success -> {
    if (postResponse.code != 204) {
      val rawBody = postResponse.response.rawBody()
      // Manually parse the raw body to access the response
    }
  }
  is NetworkResponse.Error -> { ... }
}
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

## Raw Retrofit Responses

Responses of type `NetworkResponse.Success`, `NetworkResponse.ServerError` and `NetworkResponse.UnknownError` are bundled with the raw Retrofit `Response` object (if available). This allows you to interact with raw response in case you ever need it:

```kotlin
when (networkResponse) {
  is NetworkResponse.Success -> {
    val statusCode = networkResponse.response.code()
  }
}
```
