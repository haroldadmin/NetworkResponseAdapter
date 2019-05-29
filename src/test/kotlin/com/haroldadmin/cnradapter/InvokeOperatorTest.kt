package com.haroldadmin.cnradapter

import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.io.IOException

private class MessageRepo {
    fun getMessage(): Deferred<NetworkResponse<String, String>> {
        return CompletableDeferred(NetworkResponse.Success("Hello!"))
    }

    fun getMessageError(): Deferred<NetworkResponse<String, String>> {
        return CompletableDeferred(NetworkResponse.NetworkError(IOException()))
    }
}

internal class InvokeOperatorTest : DescribeSpec({

    describe("Overloaded invoke operator") {
        val repo = MessageRepo()

        context("Successful response") {
            val messageResponse = repo.getMessage().await()

            it("Should return the successful body") {
                val response = messageResponse()
                response shouldBe "Hello!"
            }
        }

        context("Error response") {
            val messageResponse = repo.getMessageError().await()

            it("Should return null") {
                val response = messageResponse()
                response shouldBe null
            }
        }
    }
})