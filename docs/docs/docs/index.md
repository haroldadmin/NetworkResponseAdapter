# NetworkResponseAdapter

[![Release Version](https://jitpack.io/v/haroldadmin/NetworkResponseAdapter.svg)](https://jitpack.io/#haroldadmin/NetworkResponseAdapter)
[![Build Status](https://github.com/haroldadmin/networkresponseadapter/workflows/CI/badge.svg)](https://github.com/haroldadmin/networkresponseadapter/actions)

## Introduction

This library provides a Retrofit call adapter to handle errors as a part of state. It helps you write cleaner code for network requests by treating errors as values, instead of exceptions.

## Network Response

`NetworkResponse<S, E>` is a Kotlin sealed class with the following states:

1. Success: Represents successful responses (2xx response codes)
2. ServerError: Represents Server errors
3. NetworkError: Represents connectivity errors
4. UnknownError: Represents every other kind of error which can not be classified as an API error or a network problem (eg JSON deserialization exceptions)

It is generic on two types: a response (`S`), and an error (`E`). The response type is your Java/Kotlin representation of the API response, while the error type represents the error response sent by the API.

## Example

```kotlin
data class DetailsResponse(
  val details: String
)

data class DetailsError(
  val errorMessage: String
)

interface Api {
  @Get("/details)
  suspend fun details(): NetworkResponse<DetailsResponse, DetailsError>
}

class ViewModel {
  suspend fun fetchDetails() {
    when (val response = api.details()) {
      is NetworkResponse.Success -> handleSuccess(response.body)
      is NetworkResponse.ServerError -> handleServerError(response.code)
      is NetworkResponse.NetworkError -> handleNetworkError(response.error)
      is NetworkResponse.UnknownError -> handleUnknownError(response.error)
    }
  }
}
```

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

<!-- prettier-ignore-start -->
!!! note
    This library uses OkHttp 4, which requires Android API version 21+ and Java 8+.
<!-- prettier-ignore-end -->

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
