package com.capstone.edstroke.data.response

import com.google.gson.annotations.SerializedName

data class PredictResponse(

	@field:SerializedName("result")
	val result: String? = null,

	@field:SerializedName("probability")
	val probability: Any? = null
)
