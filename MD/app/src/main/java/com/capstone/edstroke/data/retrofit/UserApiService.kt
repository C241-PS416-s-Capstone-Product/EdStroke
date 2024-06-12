package com.capstone.edstroke.data.retrofit

import com.capstone.edstroke.data.response.HistoryResponse
import com.capstone.edstroke.data.response.LoginResponse
import com.capstone.edstroke.data.response.RegisterResponse
import com.capstone.edstroke.data.response.UserResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET

interface UserApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("email") email: String,
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("user")
    suspend fun user(): UserResponse

    @GET("history")
    suspend fun history(): HistoryResponse
}