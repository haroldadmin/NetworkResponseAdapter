package com.haroldadmin.cnradapter.sample

import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class Post(
    val id: Int,
    val title: String,
    val body: String,
    val userId: Int,
)

@Serializable
data class CreatePostParams(
    val title: String,
    val body: String,
    val userId: Int,
)

@Serializable
class ErrorResponse

interface PostsService {
    @GET("posts/{postId}")
    suspend fun getPost(@Path("postId") postId: Int): NetworkResponse<Post, ErrorResponse>

    @GET("posts/{postId}")
    suspend fun getPostWithError(@Path("postId") postId: Int): NetworkResponse<String, ErrorResponse>

    @GET("posts")
    suspend fun getPosts(): NetworkResponse<List<Post>, ErrorResponse>

    @POST("posts")
    suspend fun createPost(@Body params: CreatePostParams): NetworkResponse<Post, ErrorResponse>
}

