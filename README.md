# NetworkResponse Retrofit adapter

[![Build Status](https://github.com/haroldadmin/networkresponseadapter/workflows/CI/badge.svg)](https://github.com/haroldadmin/networkresponseadapter/actions)

https://haroldadmin.github.io/NetworkResponseAdapter/

A call adapter that handles errors as a part of state

---

This library provides a Retrofit call adapter for wrapping your API responses in a `NetworkResponse` class using Coroutines.

## Network Response

`NetworkResponse<S, E>` is a Kotlin sealed class with the following states:

1. Success: Represents successful responses (2xx response codes)
2. ServerError: Represents Server errors
3. NetworkError: Represents connectivity errors
4. UnknownError: Represents every other kind of error which can not be classified as an API error or a network problem (eg JSON deserialization exceptions)

It is generic on two types: a response (`S`), and an error (`E`). The response type is your Java/Kotlin representation of the API response, while the error type represents the error response sent by the API.

## Usage

- Suppose you have an API that returns the following response if the request is successful:

  _Successful Response_

  ```json
  {
    "name": "John doe",
    "age": 21
  }
  ```

- And here's the response when the request was unsuccessful:

  _Error Response_

  ```json
  {
    "message": "The requested person was not found"
  }
  ```

- You can create two data classes to model the these responses:

  ```kotlin
  data class PersonResponse(val name: String, val age: Int)
  data class ErrorResponse(val message: String)
  ```

- You may then write your API service as:

  ```kotlin
  // APIService.kt

  @GET("/person")
  fun getPerson(): Deferred<NetworkResponse<PersonResponse, ErrorResponse>>

  // or if you want to use Suspending functions
  @GET("/person")
  suspend fun getPerson(): NetworkResponse<PersonResponse, ErrorResponse>>
  ```

- Make sure to add this call adapter factory when building your Retrofit instance:

  ```kotlin
  Retrofit.Builder()
      .addCallAdapterFactory(NetworkResponseAdapterFactory())
      .build()
  ```

- Then consume the API:

  ```kotlin
  // Repository.kt

  suspend fun getPerson() {
      val person = apiService.getPerson().await()
      // or if you use suspending functions,
      val person = apiService.getPerson()

      when (person) {
          is NetworkResponse.Success -> {
              // Handle successful response
          }
          is NetworkResponse.Error -> {
              // Handle error
          }
      }
  
      // If you care about what type of error
      when (person) {
          is NetworkResponse.Success -> {
              // Handle successful response
          }
          is NetworkResponse.ServerError -> {
              // Handle server error
          }
          is NetworkResponse.NetworkError -> {
              // Handle network error
          }
          is NetworkResponse.UnknownError -> {
              // Handle other errors
          }
      }
  }
  ```

- You can also use the included utility function `executeWithRetry` to automatically retry your network requests if they result in `NetworkResponse.NetworkError`

  ```kotlin
  suspend fun getPerson() {
      val response = executeWithRetry(times = 5) {
          apiService.getPerson.await()
      }

      // or with suspending functions
      val response = executeWithRetry(times = 5) {
          apiService.getPerson()
      }

      // Then handle the response
  }
  ```

- There's also an overloaded invoke operator on the NetworkResponse class which returns the success body if the request was successful, or null otherwise

  ```kotlin
  val usersResponse = usersRepo.getUsers().await()
  println(usersResponse() ?: "No users were found")
  ```

- Some API responses convey information through headers only, and contain empty bodies. Such endpoints must be used with `Unit` as their success type.

  ```kotlin
  @GET("/empty-body-endpoint")
  suspend fun getEmptyBodyResponse(): NetworkResponse<Unit, ErrorType>
  ```

---

## Benefits

Modelling errors as a part of your state is a recommended practice. This library helps you deal with scenarios where you can successfully recover from errors, and extract meaningful information from them too!

- `NetworkResponseAdapter` provides a much cleaner solution than Retrofit's built in `Call` type for dealing with errors.`Call` throws an exception on any kind of an error, leaving it up to you to catch it and parse it manually to figure out what went wrong. `NetworkResponseAdapter` does all of that for you and returns the result in an easily consumable `NetworkResponse` subtype.

- The RxJava retrofit adapter treats non 2xx response codes as errors, which seems silly in the context of Rx where errors terminate streams. Also, just like the `Call<T>` type, it makes you deal with all types of errors in an `onError` callback, where you have to manually parse it to find out exactly what went wrong.

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
