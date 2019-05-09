## Coroutines Network Response Call Adapter for Retrofit

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
```
{
    "name": "John doe"
    "age": 21
}
```

And here's the response when the request was unsuccessful:

*Error Response*
```
{
    "message": "The requested person was not found"
}
```

You can create two data classes to model the these responses:

```
data class PersonResponse(val name: String, val age: Int)
data class ErrorResponse(val message: String)
```

You may then write your API service as:


```
// APIService.kt

@GET("<api-url>")
fun getPerson(): Deferred<NetworkResponse<PersonResponse, ErrorResponse>

```

This can be consumed in the following way:

```
// Repository.kt

suspend fun getPerson() = repositoryScope.launch {
    val person = apiService.getPerson().await()
    when (person) {
        is NetworkResponse.Success -> // Handle Success
        is NetworkResponse.ServerError -> // Handle Server Error
        is NetworkResponse.NetworkError -> // Handle Network Error
    }
}
```

*Note that since `NetworkResponse` is a sealed class, you do not need to specify an `else` case in the `when` expression.*

#### Why?
Modelling errors as a part of your state is a recommended practice. This library helps you deal with scenarios where you can successfully recover from errors. Server and Connectivity errors can be easily dealt with.
For any other types of unexpected errors, you probably want to crash your application so that you can take a look at what's going on.

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