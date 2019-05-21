### NetworkResponse Retrofit adapter
A call adapter that handles errors as a part of state

---
This library provides a Retrofit call adapter for wrapping your API responses in a `NetworkResponse` class using Coroutines.

#### Network Response
Network response is a Kotlin sealed class with the following three states:

1. Success: Used to represent successful responses (2xx response codes, non empty response bodies)
1. ServerError: Used to represent Server errors
1. NetworkError: Used to represent connectivity errors

The sealed class is generic on two types: The response type, and an error type. The response type is your Java/Kotlin representation of the API response, while the error type represents the error response sent by the API server.
The response type represents the Java/Kotlin representation of your API network response. The error type represents your API's error response.


#### Usage

Suppose you have an API that returns the following response if the request is successful:

*Successful Response*
```json
{
    "name": "John doe",
    "age": 21
}
```

And here's the response when the request was unsuccessful:

*Error Response*
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

You may then write your API service as:


```kotlin
// APIService.kt

@GET("<api-url>")
fun getPerson(): Deferred<NetworkResponse<PersonResponse, ErrorResponse>

```

Make sure to add this call adapter factory when building your Retrofit instance:
```kotlin
Retrofit.Builder()
    .addCallAdapterFactory(CoroutinesNetworkResponseAdapterFactory())
    .build()
```

Then consume the API as follows:

```kotlin
// Repository.kt

suspend fun getPerson() {
    val person = apiService.getPerson().await()
    when (person) {
        is NetworkResponse.Success -> {
            // Handle Success
        }
        is NetworkResponse.ServerError -> { 
            // Handle Server Error 
        }
        is NetworkResponse.NetworkError -> {
            // Handle Network Error
        }
    }
}
```

You can also use the included utility function `executeWithRetry` to automatically retry your network requests if they result in `NetworkResponse.NetworkError`
```kotlin
suspend fun getPerson() {
    val response = executeWithRetry(times = 5) {
        apiService.getPerson.await()
    }
    
    // Then handle the response
}
```
---

#### Why?
Modelling errors as a part of your state is a recommended practice. This library helps you deal with scenarios where you can successfully recover from errors. Server errors and Connectivity problems can be easily dealt with.
For any other unexpected situation, you probably want to crash your application so that you can take a look at what's going on.

The `NetworkResponse` adapter provides a much cleaner solution than Retrofit's built in `Call` type, because it models errors as a sealed class and does not force you to think in terms of callbacks.
It is built on top of coroutines support, so asynchronous network requests become a lot easier too!

The RxJava retrofit adapter treats non 2xx error codes as errors too, which seems silly. An error in an Rx stream should be something from which it is difficult to recover.
This is not the best way to deal with errors from an API response, because they can contain meaningful information too. They should not be specifically dealt with in `onError` blocks.

However, because a lot of things are treated as errors in the Rx Adapter, retrying becomes as easy as dropping the `retry()` operator in the middle of the stream.
While the solution provided by this library is convenient as that, you can take a look at the `executeWithRetry` utility method.
It is a higher order function which can retry your network requests if they fail.

#### Installation

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
  implementation 'com.github.haroldadmin:NetworkResponse-Retrofit-Call-Adapter-Coroutines:v1.0.1'
}
```

#### License
```
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