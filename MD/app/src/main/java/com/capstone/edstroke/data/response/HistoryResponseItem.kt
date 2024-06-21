package com.capstone.edstroke.data.response


import com.google.gson.annotations.SerializedName

data class HistoryResponseItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("probability")
    val probability: Double,
    @SerializedName("user_id")
    val userId: Int
)