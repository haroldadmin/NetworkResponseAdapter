package com.haroldadmin.cnradapter

import kotlinx.coroutines.Deferred
import retrofit2.http.GET

interface Service {
    @GET("/")
    fun getText(): Deferred<NetworkResponse<String, String>>
}