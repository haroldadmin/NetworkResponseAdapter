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
