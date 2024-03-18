# Changelog

## (Upcoming)

**Kotlin 1.9.23**

- Updated the language level to 1.9.23
- Minor source changes to satisfy _ktlint_
- Minor changes due to new language level and library versions

**Version Catalogs**
- Adopted _version catalog_ dependency management.

## Version 5.0.0 (6 March, 2022)

A new stable release for your favourite Retrofit call adapter. v5 is a culmination of a lot of community contributions and an overhaul of the internals of the library.

**Upgrade Guide**

https://haroldadmin.github.io/NetworkResponseAdapter/upgrade-guides/v5/

**Breaking Changes**:

- The constructors for `NetworkResponse` subclasses have changed ([#59](https://github.com/haroldadmin/NetworkResponseAdapter/issues/59))

`NetworkResponse.Success`, `NetworkResponse.ServerError` and `NetworkResponse.UnknownError` no longer accept status code, headers or body as constructor parameters. Instead, you must supply a correctly configured instance of a Retrofit `Response`.

Here's an example with Mockito:

In v4:

```kt
whenever(apiService.signOut(any()))
	.thenReturn(NetworkResponse.Success(code = 200, body = Unit))
```

In v5:

```kt
// NetworkResponse.Success
whenever(apiService.signOut(any()))
	.thenReturn(NetworkResponse.Success(response = Response.success(200), body = Unit))

// NetworkResponse.Error
val errorResponse = Response.error<String>(
	500,
	"{\"error\": \"boom\"}"
		.toResponseBody("application/json")
		.toMediaTypeOrNull(),
)

whenever(apiService.signOut(any()))
	.thenReturn(NetworkResponse.ServerError(response = errorResponse, body = ErrorResponse("boom")))
```

**New Features**

- **Sealed Interfaces**

The `NetworkResponse` class is now based on sealed interfaces. This allows for more concise `when` expressions when you don't care about the specific type of the error:

```kt
when (networkResponse) {
  is NetworkResponse.Success -> ...
  is NetworkResponse.Error -> ...
}
```

- **Raw Retrofit Responses**

`NetworkResponse.Success`, `NetworkResponse.ServerError` and `NetworkResponse.UnknownError` now bundle the raw retrofit `Response<S>` object to allow for greater access to the response of the network request.

```kt
when (networkResponse) {
  is NetworkResponse.Success -> {
    val statusCode = networkResponse.response.code()
  }
}
```

- **Handling Empty Response Bodies**

In the current version of the library you have to use the `Unit` response type if you expected your server to respond without a body. This is fine when the API never returns a body, but causes problems if it sometimes returns a body and sometimes doesn't (200 vs 204 status code).

The bundled raw Retrofit responses provide a [better way](https://github.com/haroldadmin/NetworkResponseAdapter/issues/44#issuecomment-986171843) to handle this situation.

```kt
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

- **Tests Overhaul & Migration to Kotest**

This PR gets refactors the library's test suite to get rid of redundant and obscure tests, and replaces them with a streamlined test suite focused on publicly exposed functionality.

We've also finally moved away from the deprecated `kotlintest` artifacts to the new `kotest` libraries.

- **Remove Deprecated Classes**

We've removed deprecated ways to instantiate adapter factory. The existing classes had been marked as deprecated for a long period, and I hope everyone has moved away from them.

- **Kotlin 1.6.0**

Updated the language level to 1.6.0

- **Sample App**

Add a module showing sample usage of the library using the `kotlinx.serialization` library.

_Huge thanks to @argenkiwi and @gilgoldzweig for their contributions to this release._

---

## Version 5.0.0-beta01 (26 Jan, 2022)

This release brings the v5 beta for `NetworkResponseAdapter`!

**Changes**

- `NetworkResponse.UnknownError` now bundles the raw Retrofit response when available
- Updated documentation site for changes in the v5 release
- Added upgrade guide to v5 to the docs
- Added a projects and endorsements page

---

## Version 5.0.0-alpha01 (16 December, 2021)

The next version of NetworkResponseAdapter is here! Here's a list of all the changes:

**Sealed Interfaces**

The `NetworkResponse` class is now based on sealed interfaces. This allows for more concise `when` expressions when you don't care about the specific type of the error:

```kt
when (networkResponse) {
  is NetworkResponse.Success -> ...
  is NetworkResponse.Error -> ...
}
```

**Raw Retrofit Responses**

`NetworkResponse.Success` and `NetworkResponse.ServerError` now bundle the raw retrofit `Response<S>` object to allow for greater access to the response of the network request.

```kt
when (networkResponse) {
  is NetworkResponse.Success -> {
    val statusCode = networkResponse.response.code()
  }
}
```

We still supply `code` and `headers` properties as before to retain familiarity with the existing API design.

**Handling Empty Response Bodies**

In the current version of the library you have to use the `Unit` response type if you expected your server to respond without a body. This is fine when the API never returns a body, but causes problems if it sometimes returns a body and sometimes doesn't (200 vs 204 status code).

The bundled raw Retrofit responses provide a [better way](https://github.com/haroldadmin/NetworkResponseAdapter/issues/44#issuecomment-986171843) to handle this situation.

```kt
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

**Tests Overhaul & Migration to Kotest**

This PR gets refactors the library's test suite to get rid of redundant and obscure tests, and replaces them with a streamlined test suite focused on publicly exposed functionality.

We've also finally moved away from the deprecated `kotlintest` artifacts to the new `kotest` libraries.

**Remove Deprecated Classes**

We've removed deprecated ways to instantiate adapter factory. The existing classes had been marked as deprecated for a long period, and I hope everyone has moved away from them.

**Kotlin 1.6.0**

Updated the language level to 1.6.0

**Sample App**

Add a module showing sample usage of the library using the `kotlinx.serialization` library.

---

## Version 4.2.2 (4 July, 2021)

**New**

- Gracefully handle the case when a service interface method's return type can not be handled by `NetworkResponseAdapterFactory`

**Misc**

- Upgrade to Kotlin 1.5.20 and Gradle 7.1
- Migrate to Maven publish plugin

---

## Version 4.2.1 (5 May, 2021)

Fixes the Jitpack build issue that plagued v4.2.0.

---

## Version 4.2.0 (3 May, 2021)

_This version is not available for download due to Jitpack build issues. Use v4.2.1 instead_

- Build issues on Jitpack mean that JAR files for v4.2.0 are not available for download yet.
- Please continue using v4.1.0, or wait for the next release that fixes this problem.\*\*

**New**:

- Introduce a new generic `Error` interface for when you don't about the specific type of error in a `NetworkResponse` (thanks @gilgoldzweig!)
- Update Kotlin to v1.4.31 and Dokka to v1.4.32
- Migrate to Pipenv for the documentation website

---

## Version 4.1.0 (13 December, 2020)

- Add nullable status code and headers fields to `NetworkResponse.UnknownError`
- Update to Kotlin 1.4.21, Coroutines 1.4.2, OkHttp 4.9.0
- Make kotlin, coroutines, retrofit and okhttp as `api` dependencies

---

## Version 4.0.1 (13 May, 2020)

This release adds a couple of new features:

- A special case of successful responses with empty bodies
  can now be handled by declaring the success type to be Unit
- NetworkResponse.Success class now contains a field representing
  the response code

---

## Version 3.0.1 (18 October, 2019)

Fixes:

- This release fixes a bug in `NetworkResponseCall` class, which caused Retrofit's suspending functions to always receive `null` as the ErrorType.

---

## Version 3.0.0 (15 September, 2019)

This release adds a big new feature to this library: Support for suspending functions in Retrofit's service interfaces!

New:

- Support for suspending functions
- Kotlin 1.3.50
- Coroutines 1.3 stable

Changes:

- Deprecate `CoroutinesNetworkResponseAdapter` and `CoroutinesNetworkResponseAdapterFactory` classes. Replaced with `NetworkResponseAdapter` and `NetworkResponseAdapterFactory`. Quickfix suggestion should be available within the IDE.

Huge thanks to @JavierSegoviaCordoba for helping out with this release.

---

## Version 2.1.0 (27 July, 2019)

- Upgrade Kotlin to 1.3.41
- Upgrade Coroutines to 1.3.0-RC
- Upgrade to OkHTTP 4 and Retrofit 2.6.0
- Null response codes in HttpExceptions are now returned as code 520

---

## Version 2.0.2 (29 May, 2019)

- Fixes:
  - Fix null headers when server response body is empty.

---

## Version 2.0.1 (29 May, 2019)

- New features: - Added overloaded invoke operator function the `NetworkResponse` class.

---

## Version 2.0.0 (21 May, 2019)

This release adds support for response headers in the `NetworkResponse` class. A new `executeWithRetry` method has also been added which automatically retries the network requests in case they result in `NetworkResponse.NetworkError`. You can customize the number of retries, and exponential delay parameters too.

- New Features:

  - Headers support
  - ExecuteWithRetry utility function

- Fixes
  - Fixed retrofit executor used in tests

---

## Version 1.0.0 (10 May 2019)

- First public release of the library

---
