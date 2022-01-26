# NetworkResponseAdapter

[![Release Version](https://jitpack.io/v/haroldadmin/NetworkResponseAdapter.svg)](https://jitpack.io/#haroldadmin/NetworkResponseAdapter)
[![Build Status](https://github.com/haroldadmin/networkresponseadapter/workflows/CI/badge.svg)](https://github.com/haroldadmin/networkresponseadapter/actions)

## Introduction

This library provides a Kotlin Coroutines based Retrofit call adapter for wrapping your API responses in
a `NetworkResponse` type.

See [Installation](#installation) for setup instructions.

## Network Response

`NetworkResponse<S, E>` is a Kotlin sealed interface with the following states:

- `Success`: Represents successful network calls (2xx response codes)
- `Error`: Represents unsuccessful network calls
  - `ServerError`: Server errors (non 2xx responses)
  - `NetworkError`: IO Errors, connectivity problems
  - `UnknownError`: Any other errors, like serialization exceptions

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

See [Special Cases](./special-cases.md) for dealing with more complicated scenarios.

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

[![Release](https://jitpack.io/v/haroldadmin/NetworkResponseAdapter.svg)](https://jitpack.io/#haroldadmin/NetworkResponseAdapter)

_This library uses OkHttp 4, which requires Android API version 21+ and Java 8+_

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
