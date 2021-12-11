package com.haroldadmin.cnradapter.sample

import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

val url = "https://jsonplaceholder.typicode.com"
val contentType = "application/json".toMediaType()

val loggingInterceptor = { chain: Interceptor.Chain ->
    val method = chain.request().method
    val url = chain.request().url
    println("$method: $url")
    chain.proceed(chain.request())
}

val okHttp = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

@OptIn(ExperimentalSerializationApi::class)
val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(url)
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .addConverterFactory(Json.asConverterFactory(contentType))
    .client(okHttp)
    .build()

val service: PostsService = retrofit.create(PostsService::class.java)

fun main() = runBlocking {
    for (i in -1..5) {
        println("Fetching Post $i")
        when (val postResponse = service.getPost(i)) {
            is NetworkResponse.Error -> println("Failed to get post: $postResponse")
            is NetworkResponse.Success -> {
                println(Json.encodeToString(postResponse.body))
            }
        }
    }

    when (val postsResponse = service.getPosts()) {
        is NetworkResponse.Error -> {
            println("Failed to get posts: $postsResponse")
        }
        is NetworkResponse.Success ->  {
            println("Fetched ${postsResponse.body.size} posts")
        }
    }

    for (i in -1..5) {
        println("Creating Post $i")
        val createPostParams = CreatePostParams("CNR Post $i", "Test Post", i)
        when (val createResponse = service.createPost(createPostParams)) {
            is NetworkResponse.NetworkError -> {
                println("Network connectivity error: ${createResponse.error.message}")
            }
            is NetworkResponse.ServerError -> {
                println("Server error: ${createResponse.code}")
            }
            is NetworkResponse.UnknownError -> {
                println("Unknown error: ${createResponse.error}")
            }
            is NetworkResponse.Success -> {
                println("Post created successfully")
                println(Json.encodeToString(createResponse.body))
            }
        }
    }
}