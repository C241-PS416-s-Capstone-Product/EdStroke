package com.capstone.edstroke.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
@kotlinx.parcelize.Parcelize
data class PredictResponse(

	@field:SerializedName("result")
	val result: String? = null,

	@field:SerializedName("probability")
	val probability: Double? = null


) : Parcelable
