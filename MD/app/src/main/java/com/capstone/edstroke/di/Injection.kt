package com.capstone.edstroke.di

import android.content.Context
import com.capstone.edstroke.data.pref.UserPreference
import com.capstone.edstroke.data.pref.dataStore
import com.capstone.edstroke.data.repository.UserRepository
import com.capstone.edstroke.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        ApiConfig.setToken(user.token) // set token before creating ApiService
        val apiService = ApiConfig.getUserApiService()
        return UserRepository.getInstance(pref, apiService)
    }
}
