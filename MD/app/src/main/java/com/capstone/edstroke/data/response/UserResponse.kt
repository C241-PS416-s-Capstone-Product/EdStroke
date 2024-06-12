package com.capstone.edstroke.data.response

import com.google.gson.annotations.SerializedName

data class UserResponse(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
