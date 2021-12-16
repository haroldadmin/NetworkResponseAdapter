# NetworkResponse Retrofit adapter

[![Build Status](https://github.com/haroldadmin/networkresponseadapter/workflows/CI/badge.svg)](https://github.com/haroldadmin/networkresponseadapter/actions)

https://haroldadmin.github.io/NetworkResponseAdapter/

A call adapter that handles errors as a part of state

---

This library provides a Kotlin Coroutines based Retrofit call adapter for wrapping your API responses in
a `NetworkResponse` type.

## Network Response

`NetworkResponse<S, E>` is a Kotlin sealed interface with the following states:

1. Success: Represents successful network calls (2xx response codes)
2. Error: Represents unsuccessful network calls
    1. ServerError: Server errors (non 2xx responses)
    2. NetworkError: IO Errors, connectivity problems
    3. UnknownError: Any other errors, like serialization exceptions

It is generic on two types: a success response (`S`), and an error response (`E`).

- `S`: Kotlin representation of a successful API response
- `E`: Representation of an unsuccessful API response

## Usage

Suppose an API returns the following body for a successful response:

_Successful Response_

```json
{
  "name": "John doe",
  "age": 21
}
```

And this for an unsuccessful response:

_Error Response_

```json
{
  "message": "The requested person was not found"
}
```

You can create two data classes to model the these responses:

```kotlin
data class PersonResponse(val name: String, val age: Int)

data class ErrorResponse(val message: String)
```

Then modify your Retrofit service to return a `NetworkResponse`:

```kotlin
@GET("/person")
suspend fun getPerson(): NetworkResponse<PersonResponse, ErrorResponse>>

// You can also request for `Deferred` responses
@GET("/person")
fun getPersonAsync(): Deferred<NetworkResponse<PersonResponse, ErrorResponse>>
```

Finally, add this call adapter factory to your Retrofit instance:

```kotlin
Retrofit.Builder()
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .build()
```

And voila! You can now consume your API as:

```kotlin
// Repository.kt
suspend fun getPerson() {
    when (val person = apiService.getPerson()) {
        is NetworkResponse.Success -> {
            /* Successful response */
        }
        is NetworkResponse.Error -> {
            /* Handle error */
        }
    }

    // Or, if you care about the type of the error:
    when (val person = apiService.getPerson()) {
        is NetworkResponse.Success -> {
            /* ... */
        }
        is NetworkResponse.ServerError -> {
            /* ... */
        }
        is NetworkResponse.NetworkError -> {
            /* ... */
        }
        is NetworkResponse.UnknownError -> {
            /* ... */
        }
    }
}
```

## Utilities

**Retry Failed Network Calls**

Use the included utility function `executeWithRetry` to automatically retry your network requests if they result in
a `NetworkResponse.NetworkError`

```kotlin
suspend fun getPerson() {
    val response = executeWithRetry(times = 5) {
        apiService.getPerson()
    }
}
```

**Overloaded `invoke()` on `NetworkResponse`**

The `NetworkResponse` interface has an overloaded `invoke()` operator that returns the success body if the request was
successful, or null otherwise

```kotlin
val usersResponse = usersRepo.getUsers()
println(usersResponse() ?: "No users were found")
```

**Handle Empty Response Bodies**

Some API responses convey information through headers only and contain empty bodies. Use `Unit` to represent the success
response type of such network calls.

```kotlin
@DELETE("/person")
suspend fun deletePerson(): NetworkResponse<Unit, ErrorType>
```

---

## Benefits

This library helps you deal with scenarios where you can successfully recover from errors, and extract meaningful
information from them too!

- `NetworkResponseAdapter` provides a much cleaner solution than Retrofit's built in `Call` type for dealing with
  errors.`Call` throws an exception on any kind of error, leaving it up to you to catch it and parse it manually to
  figure out what went wrong. `NetworkResponseAdapter` does all of that for you and returns the result in an easily
  consumable `NetworkResponse` type.

- The RxJava retrofit adapter treats non 2xx response codes as errors, which seems silly in the context of Rx where
  errors terminate streams. Also, just like the `Call<T>` type, it makes you deal with all types of errors in
  an `onError` callback, where you have to manually parse it to find out exactly what went wrong.

- Using the `Response` class provided by Retrofit is cumbersome, as you have to manually parse error bodies with it.

## Installation

Add the Jitpack repository to your list of repositories:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

And then add the dependency in your gradle file:

```groovy
dependencies {
    implementation "com.github.haroldadmin:NetworkResponseAdapter:(latest-version)"
}
```

_This library uses OkHttp 4, which requires Android API version 21+ and Java 8+_

[![Release](https://jitpack.io/v/haroldadmin/NetworkResponseAdapter.svg)](https://jitpack.io/#haroldadmin/NetworkResponseAdapter)

## License

```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
