package com.capstone.edstroke.data.retrofit

import com.capstone.edstroke.data.request.LoginRequest
import com.capstone.edstroke.data.request.RegisterRequest
import com.capstone.edstroke.data.response.HistoryResponse
import com.capstone.edstroke.data.response.LoginResponse
import com.capstone.edstroke.data.response.RegisterResponse
import com.capstone.edstroke.data.response.User
import com.capstone.edstroke.data.response.UserResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApiService {
    @POST("register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @GET("user")
    suspend fun user(): User

    @GET("history")
    suspend fun history(): HistoryResponse

    @PUT("update-profile")
    suspend fun updateProfile(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse
}