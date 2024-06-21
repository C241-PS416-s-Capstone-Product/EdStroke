package com.capstone.edstroke.data.repository

import com.capstone.edstroke.data.pref.UserPreference
import com.capstone.edstroke.data.request.RiskScreeningRequest
import com.capstone.edstroke.data.response.PredictResponse
import com.capstone.edstroke.data.response.RegisterResponse
import com.capstone.edstroke.data.response.RiskScreeningResponse
import com.capstone.edstroke.data.retrofit.RiskApiService
import com.capstone.edstroke.data.retrofit.UserApiService
import retrofit2.HttpException

class RiskRepository  private constructor(
    private val userPreference: UserPreference,
    private var riskApiService: RiskApiService
) {
    suspend fun riskScreening(request: RiskScreeningRequest): PredictResponse {
        return try {
            riskApiService.riskScreening(request)
        } catch (e: HttpException) {
            PredictResponse(result = e.message())
        }
    }

    companion object {
        @Volatile
        private var instance: RiskRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            riskApiService: RiskApiService
        ): RiskRepository =
            instance ?: synchronized(this) {
                instance ?: RiskRepository(userPreference, riskApiService)
            }.also { instance = it }
    }
}