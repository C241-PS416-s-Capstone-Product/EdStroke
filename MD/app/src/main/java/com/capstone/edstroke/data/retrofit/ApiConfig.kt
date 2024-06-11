package com.example.submissionintermediate.data.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private var token: String? = null

    fun setToken(newToken: String) {
        token = newToken
    }

    fun getUserApiService(): UserApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                token?.let { token ->
                    req.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(req.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://story-api.dicoding.dev/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(UserApiService::class.java)
    }

    fun getStoryApiService(): StoryApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                token?.let { token ->
                    req.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(req.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://story-api.dicoding.dev/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(StoryApiService::class.java)
    }
}
