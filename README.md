# NetworkResponse Retrofit adapter

[![Build Status](https://github.com/haroldadmin/networkresponseadapter/workflows/CI/badge.svg)](https://github.com/haroldadmin/networkresponseadapter/actions)
![Downloads](https://img.shields.io/endpoint?color=%2364c462&url=https%3A%2F%2Fshields-io-jitpack.haroldadmin.workers.dev%2Fmonth)

This library provides a Kotlin Coroutines based Retrofit call adapter for wrapping your API responses in
a `NetworkResponse` sealed type.

## Documentation

[**https://haroldadmin.github.io/NetworkResponseAdapter**](https://haroldadmin.github.io/NetworkResponseAdapter)

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

Find complete documentation at [**https://haroldadmin.github.io/NetworkResponseAdapter**](https://haroldadmin.github.io/NetworkResponseAdapter).

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
