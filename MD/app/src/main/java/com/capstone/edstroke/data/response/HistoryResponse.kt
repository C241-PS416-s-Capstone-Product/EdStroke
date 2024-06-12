package com.capstone.edstroke.data.response

import com.google.gson.annotations.SerializedName

data class HistoryResponse(

	@field:SerializedName("HistoryResponse")
	val historyResponse: List<HistoryResponseItem?>? = null
)

data class HistoryResponseItem(

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("probability")
	val probability: Any? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
