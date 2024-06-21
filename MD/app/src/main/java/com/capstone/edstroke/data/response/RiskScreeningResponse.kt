package com.capstone.edstroke.data.response


import com.google.gson.annotations.SerializedName

data class RiskScreeningResponse(
    @field:SerializedName("probability")
    val probability: Double,
    @field:SerializedName("result")
    val result: String
)