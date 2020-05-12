package com.haroldadmin.cnradapter

import kotlinx.coroutines.Deferred
import retrofit2.http.GET

interface Service {
    @GET("/")
    fun getText(): Deferred<NetworkResponse<String, String>>

    @GET("/suspend")
    suspend fun getTextSuspend(): NetworkResponse<String, String>

    @GET("/suspend-empty-body")
    suspend fun getEmptyBodySuspend(): NetworkResponse<Unit, String>
}