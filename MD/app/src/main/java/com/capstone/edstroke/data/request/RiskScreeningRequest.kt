package com.capstone.edstroke.data.request

import com.google.gson.annotations.SerializedName

data class RiskScreeningRequest(
    @field:SerializedName("Residence_type")
    val ResidenceType: String? = null,

    @field:SerializedName("age")
    val age: Int? = null,

    @field:SerializedName("avg_glucose_level")
    val avgGlucoseLevel: Int? = null,

    @field:SerializedName("bmi")
    val bmi: Int? = null,

    @field:SerializedName("ever_married")
    val everMarried: String? = null,

    @field:SerializedName("gender")
    val gender: String? = null,

    @field:SerializedName("heart_disease")
    val heartDisease: Int? = null,

    @field:SerializedName("hypertension")
    val hypertension: Int? = null,

    @field:SerializedName("smoking_status")
    val smokingStatus: String? = null,

    @field:SerializedName("work_type")
    val workType: String? = null
)