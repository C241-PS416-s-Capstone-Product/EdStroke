package com.capstone.edstroke.data.repository

import com.capstone.edstroke.data.pref.UserModel
import com.capstone.edstroke.data.pref.UserPreference
import com.capstone.edstroke.data.response.LoginResponse
import com.capstone.edstroke.data.response.RegisterResponse
import com.example.submissionintermediate.data.retrofit.UserApiService
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

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return try {
            userApiService.register(name, email, password)
        } catch (e: HttpException) {
            RegisterResponse(error = true, message = e.message())
        }
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            userApiService.login(email, password)
        } catch (e: HttpException) {
            LoginResponse(error = true, message = e.message())
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