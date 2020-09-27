# Extensions

## Overloaded Invoke operator

`NetworkResponse` also has an overloaded `invoke` operator. It returns the underlying data if the response is `NetworkResponse.Success`, or null otherwise.

```kotlin
val usersResponse = usersRepo.getUsers().await()
println(usersResponse() ?: "No users were found")
```

## Retrying network requests

The `executeWithRetry` method shipped with this library can help you retry a network request without any boilerplate:

```kotlin
suspend fun fetchDetails() {
 val response = executeWithRetry(times = 5) {
   api.getDetails()
 }
}
```
