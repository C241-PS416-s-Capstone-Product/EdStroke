package com.capstone.edstroke.data.retrofit

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
                    req.addHeader("X-auth-token", token)
//                                    req.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(req.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://backend-server-esmhw7j32a-et.a.run.app/api/auth/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(UserApiService::class.java)
    }

    fun getRiskApiService(): RiskApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                token?.let { token ->
                    req.addHeader("X-auth-token", token)
                }
                chain.proceed(req.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://backend-server-esmhw7j32a-et.a.run.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(RiskApiService::class.java)
    }
}
