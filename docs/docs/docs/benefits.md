# Benefits

Modelling errors as a part of your state is a recommended practice. This library helps you deal with scenarios where you can successfully recover from errors, and extract meaningful information from them too!

- `NetworkResponseAdapter` provides a much cleaner solution than Retrofit's built in `Call` type for dealing with errors.`Call` throws an exception on any kind of an error, leaving it up to you to catch it and parse it manually to figure out what went wrong. `NetworkResponseAdapter` does all of that for you and returns the result in an easily consumable `NetworkResponse` subtype.

- The RxJava retrofit adapter treats non 2xx response codes as errors, which seems silly in the context of Rx where errors terminate streams. Also, just like the `Call<T>` type, it makes you deal with all types of errors in an `onError` callback, where you have to manually parse it to find out exactly what went wrong.

- Using the `Response` class provided by Retrofit is cumbersome, as you have to manually parse error bodies with it.
