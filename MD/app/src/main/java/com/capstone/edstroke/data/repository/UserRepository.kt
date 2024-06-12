package com.capstone.edstroke.data.repository

import com.capstone.edstroke.data.pref.UserModel
import com.capstone.edstroke.data.pref.UserPreference
import com.capstone.edstroke.data.request.LoginRequest
import com.capstone.edstroke.data.request.RegisterRequest
import com.capstone.edstroke.data.response.LoginResponse
import com.capstone.edstroke.data.response.RegisterResponse
import com.capstone.edstroke.data.retrofit.UserApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private var userApiService: UserApiService
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun register(request: RegisterRequest): RegisterResponse {
        return try {
            userApiService.register(request)
        } catch (e: HttpException) {
            RegisterResponse(msg = e.message())
        }
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        return try {
            userApiService.login(request)
        } catch (e: HttpException) {
            LoginResponse(msg = e.message())
        }
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun updateApiService(newApiService: UserApiService) {
        userApiService = newApiService
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            userApiService: UserApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, userApiService)
            }.also { instance = it }
    }
}