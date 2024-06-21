package com.capstone.edstroke.data.response

import com.google.gson.annotations.SerializedName

data class AdviceResponse(

	@field:SerializedName("advice")
	val advice: String? = null,

	@field:SerializedName("errorMessage")
	val errorMessage: String? = null
)
