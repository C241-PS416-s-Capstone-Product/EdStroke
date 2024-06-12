package com.capstone.edstroke.data.retrofit

import com.capstone.edstroke.data.response.PredictResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RiskApiService {
    @FormUrlEncoded
    @POST("")
    suspend fun riskPredict(
        @Field("gender") gender: String,
        @Field("age") age: Int,
        @Field("hypertension") hypertension: Int,
        @Field("heart_disease") heartDisease: Int,
        @Field("ever_married") everMarried: String,
        @Field("work_type") workType: String,
        @Field("Residence_type") residenceType: String,
        @Field("avg_glucose_level") avgGlucoseLevel: Int,
        @Field("bmi") bmi: Int,
        @Field("smoking_status") smokingStatus: String
    ): PredictResponse
}